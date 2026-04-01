# Tasks: ShardingSphere MCP Same-Session Execution Serialization

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: Add core concurrency tests for same-session race scenarios and one targeted transport regression.

**Organization**: Tasks are grouped by user story so same-session serialization, close coordination,
and cross-session compatibility can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the same-session serialization scope and guard boundaries before code changes.

- [X] T001 Add same-session serialization scope and acceptance rules to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/spec.md`
- [X] T002 [P] Record guard-placement and locking decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/research.md`
- [X] T003 [P] Capture session execution context and guard relationships in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/data-model.md`
- [X] T004 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/plan.md`

---

## Phase 2: Foundational (Session Guard Infrastructure)

**Purpose**: Introduce the minimum core infrastructure needed to serialize same-session execution.

**CRITICAL**: No execution-path refactor should start before the session guard model exists.

- [ ] T005 Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java`
  to own per-session execution contexts instead of only a session-id set
- [ ] T006 [P] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/MCPSessionManagerTest.java`
  to cover guarded close semantics and post-close invalidation

**Checkpoint**: Core can create sessions with stable per-session guards and close them safely.

---

## Phase 3: User Story 1 - 同一 session 的 SQL 与事务命令按顺序执行 (Priority: P1)

**Goal**: Serialize all `execute_query` access to same-session transaction state.

**Independent Test**: Concurrency tests prove same-session `BEGIN`, query, and `COMMIT` no longer race.

### Tests for User Story 1

- [ ] T007 [P] [US1] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacadeConcurrencyTest.java`
  to cover same-session concurrent `BEGIN`, query, and `COMMIT`

### Implementation for User Story 1

- [ ] T008 [US1] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacade.java`
  to run `execute_query` inside the per-session execution guard
- [ ] T009 [P] [US1] Verify transaction-control and plain JDBC execution stay inside the same guarded path in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacade.java`

**Checkpoint**: Same-session `execute_query` requests are serialized through one core-owned guard.

---

## Phase 4: User Story 2 - session 关闭与执行路径之间没有竞态窗口 (Priority: P1)

**Goal**: Make `closeSession()` and `closeAllSessions()` wait for in-flight same-session execution before cleanup.

**Independent Test**: Core tests prove query/close races are serialized and post-close requests fail as before.

### Tests for User Story 2

- [ ] T010 [P] [US2] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/MCPSessionManagerTest.java`
  to cover same-session query/close coordination

### Implementation for User Story 2

- [ ] T011 [US2] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java`
  so `closeSession()` uses the same per-session guard as execution
- [ ] T012 [P] [US2] Ensure `closeAllSessions()` inherits guarded session-close semantics in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java`

**Checkpoint**: Session cleanup no longer races with in-flight execution on the same session.

---

## Phase 5: User Story 3 - 不同 session 继续可并发，transport contract 不漂移 (Priority: P1)

**Goal**: Preserve cross-session concurrency and external transport compatibility while fixing same-session races.

**Independent Test**: Cross-session requests still run concurrently and one transport regression confirms unchanged public behavior.

### Tests for User Story 3

- [ ] T013 [P] [US3] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacadeConcurrencyTest.java`
  to prove different sessions are not globally serialized
- [ ] T014 [P] [US3] Add targeted concurrent-session regression coverage in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpRuntimeIntegrationTest.java`

### Implementation for User Story 3

- [ ] T015 [US3] Keep transport cleanup entry points unchanged in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
  and
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/SessionManagedStdioTransportProvider.java`
- [ ] T016 [P] [US3] Ensure metadata and capability tool paths do not inherit unnecessary same-session hotspots in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResolver.java`

**Checkpoint**: Same-session race is fixed without global serialization or transport contract drift.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency and quality closure.

- [ ] T017 [P] Run scoped core verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T018 [P] Run scoped bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T019 [P] Align final implementation notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed with this design round.
- **Foundational (Phase 2)**: Blocks all execution-path refactors because the session guard must exist first.
- **User Stories (Phase 3-5)**: Depend on the per-session guard from Phase 2.
- **Polish (Phase 6)**: Runs after the selected implementation slice is complete.

### User Story Dependencies

- **User Story 1 (P1)**: First delivery slice because same-session execution ordering is the primary defect.
- **User Story 2 (P1)**: Depends on US1 guard placement so close uses the same boundary.
- **User Story 3 (P1)**: Depends on US1/US2 so compatibility checks reflect final semantics.

### Parallel Opportunities

- `T006` and `T007` can run in parallel once the guard API shape is fixed.
- `T009` and `T010` can run in parallel while the core guard implementation stabilizes.
- `T013` and `T014` can run in parallel as cross-session and transport regression verification slices.
- `T017` and `T018` can run in parallel as final verification commands.

## Implementation Strategy

### MVP First

1. Introduce per-session execution guards in `MCPSessionManager`.
2. Wrap `MCPSQLExecutionFacade.execute()` with the guard.
3. Make `closeSession()` reuse the same guard.
4. Verify same-session race fixes and cross-session compatibility.

### Incremental Delivery

1. Freeze design and guard boundaries.
2. Land the session manager infrastructure.
3. Serialize `execute_query`.
4. Serialize `closeSession()`.
5. Reconfirm transport behavior and non-global concurrency.

## Notes

- This feature fixes same-session correctness first; it does not attempt to raise single-session throughput.
- If future work wants to move from blocking guards to per-session executors, that should be a separate follow-up after this correctness pass.
