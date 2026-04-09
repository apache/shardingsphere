# Tasks: ShardingSphere MCP Production Runtime Integration

**Input**: Design documents from `/specs/002-shardingsphere-mcp-production-runtime/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Include test tasks because this feature closes a production-runtime gap and must prove behavior without fixture-only launch paths.

**Organization**: Tasks are grouped by user story so each slice can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the follow-up runtime integration boundary and acceptance scaffolding.

- [x] T001 Add follow-up runtime-integration rollout notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [x] T002 [P] Add follow-up runtime-integration gap notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Land the provider abstraction, production launch-mode validation, and shared acceptance harness before story work starts.

**⚠️ CRITICAL**: No user story work should start until this phase is complete.

- [x] T003 Define runtime provider abstractions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPRuntimeProvider.java`
- [x] T004 [P] Add production runtime loading and fail-fast validation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java`
- [x] T005 [P] Extend MCP packaged configuration loading for runtime providers in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPConfigurationLoader.java`
- [x] T006 Replace empty-runtime production launch defaults in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/MCPRuntimeLauncher.java`
- [x] T007 [P] Add bootstrap configuration and fail-fast tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPLaunchRuntimeLoaderTest.java`
- [x] T008 Implement a shared production-runtime E2E harness in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/AbstractProductionRuntimeE2ETest.java`

**Checkpoint**: Provider loading, launch validation, and shared test scaffolding are ready.

---

## Phase 3: User Story 1 - 真实元数据发现默认可用 (Priority: P1) 🎯 MVP

**Goal**: 让默认发行包基于真实 ShardingSphere metadata 返回非空 discovery 结果与真实 capability。

**Independent Test**: 使用接入真实 metadata provider 的 MCP 服务，验证 `list_databases`、`list_tables`、`describe_table`、`get_capabilities(database)` 全部返回真实结果。

### Tests for User Story 1

- [x] T009 [P] [US1] Add runtime metadata provider unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/runtime/ShardingSphereMetadataProviderTest.java`
- [x] T010 [P] [US1] Add HTTP integration tests for real metadata-backed discovery in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/ProductionMetadataDiscoveryIntegrationTest.java`
- [x] T011 [P] [US1] Add production-runtime metadata smoke E2E verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/ProductionRuntimeSmokeE2ETest.java`

### Implementation for User Story 1

- [x] T012 [P] [US1] Implement ShardingSphere metadata provider in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/ShardingSphereMetadataProvider.java`
- [x] T013 [P] [US1] Adapt provider output to `MetadataCatalog` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MetadataCatalogFactory.java`
- [x] T014 [US1] Overlay runtime metadata into capability assembly in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java`
- [x] T015 [US1] Wire metadata provider into bootstrap runtime loading in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java`

**Checkpoint**: Discovery and capability flows are real-runtime backed and demoable as the MVP slice.

---

## Phase 4: User Story 2 - 真实 SQL 执行与事务语义默认可用 (Priority: P2)

**Goal**: 让 `execute_query`、事务控制与 savepoint 通过真实 ShardingSphere 执行链路工作，而不是依赖内存 runtime。

**Independent Test**: 在真实 runtime integration 模式下执行 `SELECT`、DML、`BEGIN / COMMIT`、`SAVEPOINT` 或 `unsupported` 场景，确认统一结果和错误语义保持成立。

### Tests for User Story 2

- [x] T016 [P] [US2] Add real execution adapter unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/ShardingSphereExecutionAdapterTest.java`
- [x] T017 [P] [US2] Add HTTP integration tests for metadata-backed execute-query in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/ProductionExecuteQueryIntegrationTest.java`
- [x] T018 [P] [US2] Extend production-runtime smoke E2E verification for execution coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/ProductionRuntimeSmokeE2ETest.java`

### Implementation for User Story 2

- [x] T019 [P] [US2] Implement real execution adapter in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ShardingSphereExecutionAdapter.java`
- [x] T020 [P] [US2] Introduce execution runtime factory for production mode in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java`
- [x] T021 [US2] Replace in-memory production execution path in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java`
- [x] T022 [US2] Wire transaction binding and savepoint behavior to the real execution path in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutor.java`

**Checkpoint**: `execute_query` and transaction semantics now depend on real ShardingSphere runtime behavior in production mode.

---

## Phase 5: User Story 3 - 发行包与验收路径兑现 PRD 终态 (Priority: P3)

**Goal**: 让默认发行包、文档与验收路径都以真实 runtime integration 为准，真正可部署、可注册、可被模型使用。

**Independent Test**: 通过发行包启动真实 runtime integration，按 README 完成 deploy、register、discovery、query、DDL/DCL refresh、close-session 全流程。

### Tests for User Story 3

- [x] T023 [P] [US3] Add distribution startup validation tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/ProductionRuntimeLauncherTest.java`
- [x] T024 [P] [US3] Merge refresh-visibility E2E coverage into `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/ProductionMultiDatabaseE2ETest.java`

### Implementation for User Story 3

- [x] T025 [P] [US3] Extend packaged MCP configuration with provider settings in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`
- [x] T026 [P] [US3] Harden packaged startup diagnostics in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/bin/start.sh`
- [x] T027 [US3] Connect real DDL / DCL refresh visibility to runtime metadata refresh behavior in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinator.java`
- [x] T028 [US3] Update MCP operator and quickstart docs in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [x] T029 [P] [US3] Update Chinese MCP operator and quickstart docs in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

**Checkpoint**: The packaged runtime, docs, and acceptance path now reflect the real product surface rather than the protocol shell.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Close final verification, rollout, and quality gaps.

- [x] T030 [P] Run MCP core scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [x] T031 [P] Run MCP bootstrap scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [x] T032 [P] Run production-runtime E2E verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [x] T033 [P] Run packaged distribution quickstart validation against `/Users/zhangliang/IdeaProjects/shardingsphere/specs/002-shardingsphere-mcp-production-runtime/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Foundational and is the recommended MVP for closing the biggest PRD gap first.
- **User Story 2 (P2)**: Starts after Foundational and depends logically on provider wiring from US1 for real runtime lookup.
- **User Story 3 (P3)**: Starts after Foundational, but its packaging and docs become meaningful only after US1 and US2 surface the production path.

### Within Each User Story

- Tests should be updated before finalizing implementation.
- Provider abstractions come before runtime-specific adapters.
- Real metadata integration comes before real query execution.
- Distribution and docs land after the runtime path is usable end-to-end.

### Parallel Opportunities

- `T004`, `T005`, and `T007` can proceed in parallel after `T003`.
- In **US1**, `T009`, `T010`, `T012`, and `T013` can run in parallel on separate files.
- In **US2**, `T016`, `T017`, `T018`, `T019`, and `T020` can be split across different owners.
- In **US3**, `T023`, `T024`, `T025`, `T026`, `T028`, and `T029` can run in parallel once the production runtime path stabilizes.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for real metadata discovery and capability assembly.
3. Validate that the default distribution can no longer succeed with an empty runtime path.
4. Demo non-empty `list_databases` and `describe_table` before starting execute-query integration.

### Incremental Delivery

1. Land provider abstraction and fail-fast launch behavior.
2. Deliver real metadata discovery and capability assembly.
3. Deliver real `execute_query` and transaction semantics.
4. Deliver packaged runtime, docs, and production-runtime E2E validation.

## Notes

- This follow-up task list intentionally focuses on the production-runtime gap left by `001-shardingsphere-mcp`.
- The path names reflect likely implementation targets and can be refined during detailed design as long as the standalone MCP boundary remains intact.
