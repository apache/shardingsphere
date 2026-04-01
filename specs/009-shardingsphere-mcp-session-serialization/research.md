# Research: ShardingSphere MCP Same-Session Execution Serialization

## Decision 1: 把 same-session 串行化放在 core，而不是 transport

- **Decision**: 由 `mcp/core` 的 session / execution 边界统一承接 same-session guard，
  不在 HTTP servlet 或 STDIO transport provider 分别加锁。
- **Rationale**:
  - 当前缺陷的根因在 transaction resource 与 session lifecycle 的共享状态，而不是某个 transport 特有逻辑。
  - HTTP 与 STDIO 都会进入同一 `execute_query -> MCPSQLExecutionFacade` 路径；
    transport 分别加锁会制造两套并发语义。
  - core 统一承接后，后续新增 transport 也能自动继承相同行为。
- **Alternatives considered**:
  - 只在 HTTP servlet 上串行化: rejected，因为 STDIO 与其他潜在入口仍会绕过保护。
  - 只在 STDIO provider 上串行化: rejected，因为 HTTP 的 same-session 并发才是更容易触发问题的路径。

## Decision 2: 使用 per-session blocking lock，而不是 busy-fail 或全局锁

- **Decision**: 首轮修复采用 per-session blocking serialization；
  同一 `session` 的第二个请求等待前一个完成，而不是立刻返回新的 busy / conflict 语义。
- **Rationale**:
  - 用户目标是 “同一个 session 串行执行事务”，不是 “增加新的并发冲突协议”。
  - 当前 public contract 已经固定；引入新 busy 错误会扩大行为面。
  - per-session lock 足以解决当前竞态，且实现范围最小。
- **Alternatives considered**:
  - `tryLock` 失败即返回 `conflict`: rejected，因为会引入新的用户可见行为分支。
  - 全局运行锁: rejected，因为它会错误牺牲不同 session 间的并发。
  - per-session serial executor: deferred，长期可行，但当前同步调用模型下没有 blocking lock 简洁。

## Decision 3: `execute_query` 由 `MCPSQLExecutionFacade` 统一进入 session guard

- **Decision**: `MCPSQLExecutionFacade.execute()` 作为 `execute_query` 的统一 guard 入口，
  在进入 statement classification、transaction command execution 和 JDBC statement execution 前先持有 session guard。
- **Rationale**:
  - facade 已经是 execution 的稳定总入口，能同时覆盖 transaction-control、
    savepoint、query、DML、DDL、DCL 这些分支。
  - 把 guard 下沉到 facade，可以避免在 `MCPJdbcTransactionStatementExecutor`
    和 `MCPJdbcStatementExecutor` 分别重复实现并发规则。
- **Alternatives considered**:
  - 在 `MCPToolPayloadResolver` 加 guard: rejected，因为它更靠近 transport-neutral payload 层，
    而不是 execution domain 层。
  - 在各 statement executor 内部分别加 guard: rejected，因为规则分散且容易遗漏。

## Decision 4: `closeSession()` 必须与执行路径复用同一把 session guard

- **Decision**: `MCPSessionManager.closeSession()` 使用和 `execute_query` 相同的 session guard，
  在锁内完成 transaction cleanup 与 session 移除。
- **Rationale**:
  - 当前另一个主要竞态点不是“并发两个 query”，而是“query 还在跑时 session 被关掉”。
  - 如果 close 使用另一套锁或不加锁，就仍然可能与 in-flight operation 并发踩连接。
- **Alternatives considered**:
  - 先 remove session 再 cleanup: rejected，因为等待中的 in-flight 请求可能仍持有旧上下文引用。
  - cleanup 完全放在 transport 层: rejected，因为会再次拆散 session 语义。

## Decision 5: `MCPSessionManager` 从 session-id set 升级为 session context registry

- **Decision**: `MCPSessionManager` 不再只保存 `Set<String>`；
  它需要拥有 per-session `SessionExecutionContext`，其中包含 session lifecycle 状态和 guard。
- **Rationale**:
  - 仅靠 `hasSession(sessionId)` 无法表达 session 级串行保护。
  - 把 execution context 和 session lifecycle 收口到 session manager，边界最清楚。
  - 这样也能保证 `closeSession()`、`closeAllSessions()` 与 `executeWithSessionLock(...)`
    都由同一处统一管理。
- **Alternatives considered**:
  - 把锁 map 放到 `MCPSQLExecutionFacade`: rejected，因为 close path 不在 facade 内。
  - 把锁 map 放到 transaction resource manager: rejected，因为 session lifecycle 不应由 JDBC resource owner 反向主导。

## Decision 6: metadata / capability tools 不纳入本轮 session guard

- **Decision**: 本轮只把 `execute_query` 和 session close cleanup 纳入 same-session guard；
  metadata / capability tools 保持当前独立路径。
- **Rationale**:
  - 当前已知竞态都围绕 transaction-bound connection、savepoints 和 session cleanup。
  - metadata / capability tools 不访问 session-bound transaction resource，
    没必要引入新的热点。
  - 最小修复范围更符合 smallest safe change 原则。
- **Alternatives considered**:
  - 把所有 tool 调用都纳入 session guard: rejected，因为会制造不必要串行化。

## Decision 7: 不改变当前 STDIO single-session transport 定位

- **Decision**: 本轮不修改 STDIO transport 的 single-session 定位；
  只修复 same-session 执行语义。
- **Rationale**:
  - 当前缺陷是 same-session race，不是 transport capability 缺失。
  - 把 STDIO 改为 multi-session 是独立产品决定，会扩大 scope 到 transport API surface 和 host 使用方式。
- **Alternatives considered**:
  - 同轮把 STDIO 改成 multi-session: rejected，因为与当前 bug fix 无直接耦合，且风险过大。
