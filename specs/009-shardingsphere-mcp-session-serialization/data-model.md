# Data Model: ShardingSphere MCP Same-Session Execution Serialization

## Core Runtime Entities

### SessionExecutionContext

- **Purpose**: 表示一个 MCP session 的执行期上下文，统一承载 session lifecycle 与 same-session 串行化 guard。
- **Fields**:
  - `sessionId`
  - `executionGuard`
  - `lifecycleState`
- **Validation rules**:
  - 一个 live session 必须且只能对应一个 `SessionExecutionContext`。
  - `executionGuard` 必须在该 session 生命周期内保持稳定，不能每次请求临时创建。
  - `lifecycleState` 至少需要表达 `OPEN`、`CLOSING`、`CLOSED` 的语义等价状态。

### SessionExecutionGuard

- **Purpose**: 保护同一 session 的执行关键区，确保同一时刻只有一个 guarded operation 能触达 session-bound transaction state。
- **Fields**:
  - `fair`
  - `ownerThread` (conceptual only)
  - `queuedOperations` (conceptual only)
- **Validation rules**:
  - guard 的粒度必须是 per-session。
  - guard 只保护同一 session 的关键区，不影响其他 session。
  - guard 的持有范围必须覆盖 statement classification 后的所有 transaction/resource access，
    以及 close cleanup。

### GuardedSessionOperation

- **Purpose**: 需要在 session guard 内执行的操作模型。
- **Kinds**:
  - `EXECUTE_QUERY`
  - `CLOSE_SESSION`
  - `CLOSE_ALL_SESSION_MEMBER`
- **Validation rules**:
  - `EXECUTE_QUERY` 与 `CLOSE_SESSION` 对同一 session 不能并发进入关键区。
  - 一个 session 在 `CLOSING` 或 `CLOSED` 后，不得再接受新的成功执行。

### TransactionResourceContext

- **Purpose**: 当前已存在的事务资源上下文，绑定一个 session 的数据库名、连接和保存点集合。
- **Fields**:
  - `databaseName`
  - `connection`
  - `savepoints`
- **Validation rules**:
  - 一个 session 同时最多只有一个 active `TransactionResourceContext`。
  - 只有在持有对应 `SessionExecutionGuard` 的情况下，才能安全修改该上下文。

## Relationships

- 一个 `MCPSessionManager` 管理多个 `SessionExecutionContext`。
- 一个 `SessionExecutionContext` 保护零个或一个 active `TransactionResourceContext`。
- 一个 `GuardedSessionOperation` 必须先获取对应 `SessionExecutionContext.executionGuard`，
  才能进入 `MCPSQLExecutionFacade` 或 session cleanup 路径。
- `closeAllSessions()` 会把每个 live session 展开成一个独立的 `CLOSE_ALL_SESSION_MEMBER`
  guarded operation。

## State Transitions

### Session lifecycle

- `created -> open`
  - `createSession(sessionId)` 成功后，session 注册为可执行状态。
- `open -> executing`
  - 同一 session 的某个 `EXECUTE_QUERY` 获取 guard。
- `executing -> open`
  - 该次 `EXECUTE_QUERY` 完成并释放 guard。
- `open -> closing`
  - `closeSession()` 获取同一 guard，并开始 cleanup。
- `executing -> closing`
  - close 请求已到达，但必须等待当前执行完成后才能真正进入 cleanup。
- `closing -> closed`
  - transaction cleanup 完成，session 从 registry 中移除。

### Transaction resource ownership

- `none -> active`
  - 在 `BEGIN` / `START TRANSACTION` 且无 active transaction 时建立 transaction resource。
- `active -> active`
  - query / DML / savepoint 操作在同一 transaction resource 上顺序执行。
- `active -> none`
  - `COMMIT`、`ROLLBACK` 或 session close cleanup 成功后释放 transaction resource。

## Canonical Guarded Flow

```text
execute_query(session-1)
  -> lookup SessionExecutionContext
  -> acquire executionGuard
  -> re-check session still registered/open
  -> run MCPSQLExecutionFacade logic
  -> release executionGuard

closeSession(session-1)
  -> lookup SessionExecutionContext
  -> acquire executionGuard
  -> mark lifecycle as closing (or equivalent protected state)
  -> cleanup transaction resources
  -> remove session from registry
  -> release executionGuard
```

## Invariants

- 同一 `session` 上的两个 `GuardedSessionOperation` 不能并发触达 transaction-bound connection。
- 不同 `session` 的 `GuardedSessionOperation` 不能因为本轮设计被错误地全局串行化。
- session 从 registry 中移除后，后续同 `sessionId` 的 follow-up 请求必须失败。
- session close cleanup 必须与 in-flight `execute_query` 共用同一 guard，而不是靠外层 transport 假定顺序。
