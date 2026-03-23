# Tasks: ShardingSphere MCP Direct Multi-Database Runtime

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/004-shardingsphere-mcp-direct-multi-db/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Include explicit topology, outage-isolation, transaction-conflict, and refresh-isolation tests because the feature changes runtime boundaries more than protocol shape.

**Organization**: Tasks are grouped by user story so discovery, execution, and operational isolation can be implemented and verified as independent slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Documentation and Alignment)

**Purpose**: Make the direct multi-database follow-up explicit in design docs before implementation starts.

- [ ] T001 Add direct multi-database runtime follow-up notes to
  `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T002 [P] Add direct multi-database topology and isolation notes to
  `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Land the runtime topology model, startup validation, and shared test harness before story work starts.

**CRITICAL**: No story work should be considered complete until this phase is done.

- [ ] T003 Define direct multi-database runtime configuration loading in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPConfigurationLoader.java`
- [ ] T004 [P] Extend launcher runtime configuration to carry topology state in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/MCPRuntimeLauncher.java`
- [ ] T005 [P] Add fail-fast topology validation and diagnostics in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java`
- [ ] T006 [P] Extend runtime provider loading for multiple logical databases in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPRuntimeProvider.java`
- [ ] T007 [P] Add multi-database configuration parsing and fail-fast tests in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPConfigurationLoaderTest.java`
- [ ] T008 Implement a shared direct multi-database E2E harness in
  `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/AbstractDirectMultiDatabaseE2ETest.java`

**Checkpoint**: Runtime topology, startup validation, and shared test scaffolding are ready.

---

## Phase 3: User Story 1 - 统一接入多个独立数据库 (Priority: P1) MVP

**Goal**: 让一个 MCP 服务实例完成多数据库直连 discovery，并通过现有 `database` route key 暴露多个 logical databases。

**Independent Test**: 使用两个独立数据库启动服务，验证 `list_databases`、`list_tables`、`describe_table` 和 `search_metadata` 的多库行为。

### Tests for User Story 1

- [ ] T009 [P] [US1] Add runtime topology factory unit tests in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactoryTest.java`
- [ ] T010 [P] [US1] Add multi-database metadata loading tests in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoaderTest.java`
- [ ] T011 [P] [US1] Add HTTP integration coverage for direct multi-database discovery in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/DirectMultiDatabaseDiscoveryIntegrationTest.java`
- [ ] T012 [P] [US1] Add E2E verification for multi-database discovery in
  `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/DirectMultiDatabaseDiscoveryE2ETest.java`

### Implementation for User Story 1

- [ ] T013 [P] [US1] Extend direct connection configuration assembly for multiple
  logical databases in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java`
- [ ] T014 [P] [US1] Load metadata snapshots for multiple logical databases in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoader.java`
- [ ] T015 [US1] Preserve multi-database discovery routing in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceLoader.java`
- [ ] T016 [US1] Update packaged runtime example topology in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`

**Checkpoint**: Multi-database discovery works as the MVP slice.

---

## Phase 4: User Story 2 - 按数据库路由执行并保持事务隔离 (Priority: P2)

**Goal**: 让 `execute_query` 和事务控制继续以单 logical database 为边界执行，同时支持同一服务内的多数据库路由。

**Independent Test**: 在两个 logical databases 上执行 `SELECT`、DML、`BEGIN / COMMIT`，并验证活动事务中的跨库切换被拒绝。

### Tests for User Story 2

- [ ] T017 [P] [US2] Add direct multi-database execution routing tests in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/ShardingSphereExecutionAdapterTest.java`
- [ ] T018 [P] [US2] Add transaction conflict tests across logical databases in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutorTest.java`
- [ ] T019 [P] [US2] Add HTTP integration coverage for routed execute-query in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/DirectMultiDatabaseExecuteQueryIntegrationTest.java`
- [ ] T020 [P] [US2] Add E2E verification for cross-database transaction rejection in
  `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/DirectMultiDatabaseTransactionE2ETest.java`

### Implementation for User Story 2

- [ ] T021 [P] [US2] Route connections by logical database in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ShardingSphereExecutionAdapter.java`
- [ ] T022 [US2] Preserve single-database transaction binding in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java`
- [ ] T023 [US2] Wire transaction control and savepoint behavior to multi-database runtime in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutor.java`

**Checkpoint**: Routed execution works and cross-database transaction switching is still rejected.

---

## Phase 5: User Story 3 - 明确运行边界与单库故障行为 (Priority: P3)

**Goal**: 启动 fail-fast、运行后单库故障隔离、last-good snapshot 保留、按库刷新和 secret-safe diagnostics 全部落地。

**Independent Test**: 一个 logical database 运行时不可用后，其他 logical databases 继续工作；只读 discovery 继续返回 last-good snapshot；
DDL / DCL 只刷新目标数据库。

### Tests for User Story 3

- [ ] T024 [P] [US3] Add availability-state unit tests in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinatorTest.java`
- [ ] T025 [P] [US3] Add bootstrap integration coverage for single-database runtime outage in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/DirectMultiDatabaseAvailabilityIntegrationTest.java`
- [ ] T026 [P] [US3] Add E2E verification for outage isolation and snapshot retention in
  `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/DirectMultiDatabaseAvailabilityE2ETest.java`
- [ ] T027 [P] [US3] Add E2E verification for targeted refresh isolation in
  `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/DirectMultiDatabaseRefreshIsolationE2ETest.java`

### Implementation for User Story 3

- [ ] T028 [P] [US3] Preserve last-good snapshot and targeted replacement rules in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceLoader.java`
- [ ] T029 [P] [US3] Implement per-database refresh visibility behavior in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinator.java`
- [ ] T030 [P] [US3] Harden runtime diagnostics and redaction in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java`
- [ ] T031 [P] [US3] Update operator docs for direct multi-database runtime in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [ ] T032 [P] [US3] Update Chinese operator docs for direct multi-database runtime in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

**Checkpoint**: Operational boundaries and single-database outage behavior are explicit and verifiable.

---

## Phase 6: Polish & Verification

**Purpose**: Close final quality and consistency gaps.

- [ ] T033 [P] Run MCP bootstrap scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T034 [P] Run MCP core scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T035 [P] Run direct multi-database E2E verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T036 [P] Reconcile final wording across
  `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all story work.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because it delivers the central value of one MCP service exposing multiple logical databases.
- **User Story 2 (P2)**: Depends on topology loading from US1 but is otherwise independent of operational isolation work.
- **User Story 3 (P3)**: Depends on topology loading from US1 and gains full value after routed execution from US2 is stable.

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after `T003`.
- In **US1**, `T009`, `T010`, `T011`, `T012`, `T013`, and `T014` can run in parallel on separate files.
- In **US2**, `T017`, `T018`, `T019`, `T020`, and `T021` can be split across different owners.
- In **US3**, `T024`, `T025`, `T026`, `T027`, `T028`, and `T030` can run in parallel once runtime topology is stable.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for topology loading and multi-database discovery.
3. Validate that `list_databases` returns multiple logical databases without changing the public MCP contract.
4. Demo multi-database discovery before starting execute-query routing changes.

### Incremental Delivery

1. Land direct runtime topology loading and fail-fast startup validation.
2. Deliver multi-database discovery and capability routing.
3. Deliver routed execution and transaction isolation.
4. Deliver outage isolation, targeted refresh, and operator-facing documentation.

## Notes

- This task list intentionally focuses on direct multi-database runtime behavior, not protocol redesign.
- The file paths reflect likely implementation targets and can be refined during coding as long as the MCP V1 public contract remains unchanged.
