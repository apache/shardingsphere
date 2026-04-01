# Feature Specification: ShardingSphere MCP Same-Session Execution Serialization

**Feature Branch**: `[009-shardingsphere-mcp-session-serialization]`  
**Created**: 2026-04-01  
**Status**: Design draft  
**Input**: User description:
"好的，基于这些总结，给我总结出来现有设计的问题，也就是说，同一个session不能串行的去执行事务。你需要把这个问题解决到，并且给我去先设计解决方案" and "使用speckit设计"

## Scope Statement

本 follow-up 修复当前 MCP runtime 在同一 `session` 上的执行并发缺口：

- 把 `execute_query` 的执行语义明确收敛为 **same-session strict serialization**
- 把 `closeSession()` / transport shutdown cleanup 也纳入同一 session 的串行边界
- 保持不同 `session` 之间仍可并发
- 通过 core 层统一承接该语义，而不是在 HTTP / STDIO transport 各自重复加锁
- 保持现有 MCP public contract、tool surface、HTTP header contract 和 STDIO API surface 不变

本特性的核心不是把 STDIO 改成 multi-session transport，也不是把 HTTP 改成无状态请求模型，
而是把 `001` 已定义的 stateful session / transaction contract 真正落实到运行时并发语义上。

## Problem Statement

当前实现已经把 transaction resource 设计成 `sessionId -> TransactionResourceContext` 的单映射，
但没有把“同一 session 的事务相关请求必须串行”落实到代码路径中。

已知风险包括：

- 同一 `session` 并发 `BEGIN` 时可能双开连接，但只有一个 transaction context 被 map 跟踪
- 同一 `session` 的 query 与 `COMMIT` / `ROLLBACK` / `closeSession()` 可能并发踩同一条连接
- `SAVEPOINT` / `ROLLBACK TO SAVEPOINT` / `RELEASE SAVEPOINT` 缺少稳定顺序
- 该问题不局限于 STDIO；HTTP 同 `MCP-Session-Id` 并发请求更容易触发

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 同一 session 的 SQL 与事务命令按顺序执行 (Priority: P1)

作为 MCP runtime 维护者，我希望同一 `session` 的 `execute_query` 请求无论来自 HTTP 还是 STDIO，
都严格按顺序执行，这样事务状态、保存点和绑定连接不会因为并发访问而失真。

**Why this priority**: 这是当前缺陷的根因；如果 same-session 仍允许并发进入 transaction path，
其他修补都只是局部规避。

**Independent Test**: core 并发测试可验证同一 `session` 上两个并发 `execute_query`
不会同时进入 transaction resource 访问区，且事务状态转移保持一致。

**Acceptance Scenarios**:

1. **Given** 同一 `session` 上两个并发 `execute_query`，**When** 二者同时进入 runtime，
   **Then** 系统必须保证它们按顺序访问该 session 的事务状态与连接资源。
2. **Given** 同一 `session` 已在事务中，**When** 查询与 `COMMIT` 并发到达，
   **Then** 它们必须串行执行，而不是同时操作同一条事务连接。
3. **Given** 同一 `session` 上并发发送 `BEGIN`，**When** 第一条请求已建立事务，
   **Then** 后续请求只允许看到串行后的状态结果，而不能因竞态造成未跟踪连接或双重事务建立。

---

### User Story 2 - session 关闭与执行路径之间没有竞态窗口 (Priority: P1)

作为运行方，我希望 `DELETE /mcp`、STDIO disconnect 和 runtime shutdown
在关闭 session 时能够等待当前同 session 执行完成，再做回滚与资源释放，
这样不会出现“请求还在跑，session 已被关掉”的连接竞态。

**Why this priority**: `closeSession()` 当前也会直接触达 transaction resource cleanup；
如果它与 in-flight query 并发，问题与事务命令并发同样严重。

**Independent Test**: core 并发测试可验证同一 `session` 的执行与 `closeSession()`
不会并发操作同一事务连接，关闭后 follow-up 请求继续按既有 contract 失败。

**Acceptance Scenarios**:

1. **Given** 同一 `session` 有 in-flight `execute_query`，**When** transport 触发 `closeSession()`，
   **Then** close 必须等待当前执行区结束，再做 rollback/cleanup/remove。
2. **Given** `closeSession()` 已完成，**When** 客户端继续用该 `session` 发 follow-up 请求，
   **Then** 系统继续按现有 contract 把该 session 视为无效或已关闭。
3. **Given** runtime 执行 `closeAllSessions()`，**When** 某个 session 仍有正在执行的请求，
   **Then** cleanup 仍必须遵守该 session 的串行化边界，而不是粗暴并发关闭。

---

### User Story 3 - 不同 session 继续可并发，transport contract 不漂移 (Priority: P1)

作为 MCP server 维护者，我希望这次修复只把串行化粒度限定在单个 `session`，
而不是把整个 runtime 变成全局单线程，这样多个独立 session 仍可并发工作。

**Why this priority**: 当前需求是修复 same-session race，不是降低多 session 吞吐。

**Independent Test**: 并发测试可验证两个不同 `session` 的请求不会被全局串行化；
HTTP / STDIO transport 的 public contract 继续不变。

**Acceptance Scenarios**:

1. **Given** 两个不同 `session` 同时执行查询，**When** 二者进入 runtime，
   **Then** 系统应允许它们并发，而不是被一个全局锁串行化。
2. **Given** 现有 HTTP `MCP-Session-Id` contract 与 STDIO API surface 已固定，
   **When** 修复完成，**Then** transport 不新增用户可见 header、参数或 close 语义变更。
3. **Given** metadata / capability tools 不依赖 session-bound transaction state，
   **When** 本轮修复完成，**Then** 它们不应被不必要地纳入同一串行化热点。

### Edge Cases

- 不允许通过 transport-specific locking 让 HTTP 与 STDIO 分别演化出不同的并发语义。
- 不允许用一个全局运行锁替代 per-session guard。
- 不允许在同一 `session` 上因为并发冲突而泄漏未跟踪 JDBC 连接。
- 不允许 `closeSession()` 在 session 仍有 in-flight 执行时提前移除 session 记录。
- 不允许本轮修复改变 metadata tools、capability tools 的 public 参数和返回形态。
- 本轮不把 STDIO 从 single-session transport 改成 multi-session transport。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在 core 层对同一 `session` 的 `execute_query` 执行路径提供严格串行化语义。
- **FR-002**: 系统 MUST 对同一 `session` 的 `closeSession()` 与 in-flight `execute_query`
  使用同一串行化边界，防止并发清理同一事务资源。
- **FR-003**: 串行化粒度 MUST 是 per-session，而不是全局运行时级别。
- **FR-004**: 不同 `session` 的执行路径 MUST 继续允许并发。
- **FR-005**: same-session 串行化 MUST 由 `mcp/core` 或紧邻 core 的会话/执行层承接，
  而不是由 HTTP / STDIO transport 各自维护一套独立锁语义。
- **FR-006**: `execute_query` 的事务控制、保存点和普通 SQL 执行 MUST 在进入
  `MCPJdbcTransactionResourceManager` 之前已经处于该 session 的串行化保护内。
- **FR-007**: `closeSession()` MUST 在完成 rollback/cleanup/remove 之前持有与执行路径一致的 session guard。
- **FR-008**: 当 `closeSession()` 完成后，后续对该 `session` 的 follow-up 请求 MUST 继续返回现有 contract
  所定义的无效 session 行为。
- **FR-009**: 本轮修复 MUST NOT 改变现有 MCP public tool names、tool arguments、
  HTTP headers、HTTP status contract 或 STDIO API surface。
- **FR-010**: metadata / capability tools SHOULD NOT 被纳入 same-session 执行锁，
  除非它们直接访问 session-bound transaction state。
- **FR-011**: 系统 SHOULD 使用公平的 per-session 执行锁或等价串行机制，
  以避免同一 session 上长时间饥饿。
- **FR-012**: 本特性 MUST 提供至少一组 core 并发测试，覆盖 same-session 并发 `BEGIN`、
  query/commit 竞态、query/close 竞态和 cross-session 并发。
- **FR-013**: 本特性 MUST 提供至少一条 transport 侧回归验证，证明 HTTP 或 STDIO
  经过 core 串行化后行为保持兼容。

### Key Entities *(include if feature involves data)*

- **SessionExecutionContext**: runtime 中某个 MCP session 的执行上下文，承载 session lifecycle 与串行化 guard。
- **SessionExecutionGuard**: 用于保护同一 session 执行区的锁或等价串行机制。
- **GuardedSessionOperation**: 必须在 session guard 内执行的操作，当前至少包括 `execute_query` 和 `closeSession()`。
- **TransactionResourceContext**: 当前已存在的 session-bound transaction resource，包含 database、connection 与 savepoints。

### Assumptions

- `execute_query` 是当前唯一直接触达 session-bound transaction state 的 public tool。
- `MCPSQLExecutionFacade` 继续作为 `execute_query` 进入实际 SQL / transaction path 的统一入口。
- `closeSession()` 既可能来自 HTTP `DELETE /mcp`，也可能来自 STDIO disconnect 或 shutdown cleanup。
- 当前修复目标是线程安全和事务语义正确性，不是改变 transport 的部署模式。

## Non-Goals

- 不把 STDIO 改造成 multi-session transport。
- 不引入新的 public error code、busy code 或新的 client coordination 协议。
- 不把同一 session 的请求改造成异步消息总线或 actor framework。
- 不在本轮引入 distributed session lock、跨节点 session 协调或 failover replay。
- 不把 metadata tools、capability tools 和 session-bound execution path 重新混成一个统一全局执行队列。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的 same-session 并发 `BEGIN`、query/commit、query/close 测试都不会再出现竞态失败。
- **SC-002**: 100% 的不同 session 并发测试都证明不存在全局串行化。
- **SC-003**: `closeSession()` 与 `closeAllSessions()` 在并发场景下都不会再产生未跟踪连接或同连接并发关闭问题。
- **SC-004**: HTTP / STDIO 的 public contract 在本轮修复后保持零漂移。
- **SC-005**: same-session 串行语义仅在 core 层定义一次，transport 层不出现第二份并发规则实现。
