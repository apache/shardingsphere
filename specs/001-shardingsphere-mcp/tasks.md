# Tasks: ShardingSphere MCP V1 Unified Database Contract

**Input**: Design documents from `/specs/001-shardingsphere-mcp/`
**Prerequisites**: plan.md (required), spec.md (required for user stories),
research.md, data-model.md, contracts/, quickstart.md

**Tests**: Include test tasks because the repository quality rules and
implementation plan require layered verification across unit, integration,
protocol, and E2E levels.

**Organization**: Tasks are grouped by user story to enable independent
implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the MCP build chain, module skeleton, and CI isolation.

- [X] T001 Update root Maven profile wiring in `/Users/zhangliang/IdeaProjects/shardingsphere/pom.xml`
- [X] T002 Update distribution profile wiring in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/pom.xml`
- [X] T003 [P] Update E2E profile wiring in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/pom.xml`
- [X] T004 [P] Create the MCP aggregator and module parents in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/pom.xml`
- [X] T005 [P] Create the distribution and E2E module POMs in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/pom.xml`
- [X] T006 [P] Add dedicated JDK 17 MCP workflow in `/Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows/jdk17-subchain-ci.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Land the shared runtime skeleton and blocking domain primitives.

**⚠️ CRITICAL**: No user story work should start until this phase is complete.

- [X] T007 Implement the transaction matrix registry and related enums in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityRegistry.java`
- [X] T008 [P] Implement shared result and error protocol models in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/ExecuteQueryResponse.java`
- [X] T009 [P] Implement the session, transaction, and savepoint manager skeleton, including no session recovery and close-triggered rollback in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/McpSessionManager.java`
- [X] T010 [P] Implement runtime configuration loading and distribution configuration contracts in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/McpConfigurationLoader.java`
- [X] T011 Implement MCP server bootstrap and registry wiring in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/McpServerBootstrap.java`
- [X] T012 Implement the shared integration test harness in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/AbstractMcpIntegrationTest.java`

**Checkpoint**: Build isolation, shared domain contracts, runtime configuration, and bootstrap
skeleton are ready; user stories can now proceed.

---

## Phase 3: User Story 1 - 统一元数据发现 (Priority: P1) 🎯 MVP

**Goal**: 让调用方用统一 resources 和 metadata tools 发现公共对象与
capability，而不再为数据库方言编写独立发现逻辑。

**Independent Test**: 在同一个 MCP 服务后挂接至少两种正式支持数据库，
调用 capability、`list_*`、`describe_*`、`search_metadata`，确认只返回
当前实现支持的公共对象，并在不支持 `index` 时返回 `unsupported`。

### Tests for User Story 1

- [X] T013 [P] [US1] Add capability assembler unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssemblerTest.java`
- [X] T014 [P] [US1] Add metadata discovery integration tests, including unsupported `index` behavior and excluded-object non-exposure for materialized views, sequences, routines, triggers, events, and synonyms in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/MetadataDiscoveryIntegrationTest.java`
- [X] T042 [P] [US1] Add supported-database baseline contract tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/SupportedDatabaseBaselineTest.java`

### Implementation for User Story 1

- [X] T015 [P] [US1] Implement service and database capability assemblers in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java`
- [X] T016 [P] [US1] Implement metadata resource loaders with V1 public-object whitelist mapping and normalized object mapping in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceLoader.java`
- [X] T017 [P] [US1] Implement metadata tool handlers for `list_*`, `describe_*`, and `search_metadata` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcher.java`
- [X] T018 [US1] Wire capability resources and metadata tools into bootstrap registration in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/wiring/McpToolRegistry.java`
- [X] T019 [US1] Add metadata discovery E2E coverage for baseline objects only, asserting excluded objects are not surfaced in resources or metadata tools in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/MetadataDiscoveryE2ETest.java`

**Checkpoint**: User Story 1 should now be independently functional and demoable
as the MVP slice.

---

## Phase 4: User Story 2 - 统一 SQL 执行与事务边界 (Priority: P2)

**Goal**: 通过统一 `execute_query`、事务控制和 savepoint 语义，让多数据库
执行面拥有一致的结果模型、错误模型和会话边界。

**Independent Test**: 使用统一工具执行查询、DML、事务控制、savepoint 和
不支持语句，确认结果类型、错误码和单数据库事务绑定行为符合契约。

### Tests for User Story 2

- [X] T020 [P] [US2] Add execute-query classification and mapping unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacadeTest.java`
- [X] T021 [P] [US2] Add Streamable HTTP runtime integration tests for session creation, protocol mismatch rejection, stale-session rejection, and close semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StreamableHttpRuntimeIntegrationTest.java`
- [X] T040 [P] [US2] Add STDIO transport integration tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StdioTransportIntegrationTest.java`

### Implementation for User Story 2

- [X] T022 [P] [US2] Implement statement classification and single-statement validation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/StatementClassifier.java`
- [X] T023 [P] [US2] Implement the unified `execute_query` facade and result mapping in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java`
- [X] T024 [P] [US2] Implement transaction and savepoint command execution in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutor.java`
- [X] T025 [US2] Implement the `/mcp` Streamable HTTP runtime with session-header enforcement, protocol stability checks, local-mode `Origin` validation, and terminal-session cleanup in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMcpServer.java`
- [X] T039 [P] [US2] Implement the STDIO runtime bootstrap in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/stdio/StdioMcpServer.java`
- [X] T041 [US2] Wire the STDIO launch path into MCP bootstrap lifecycle in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncher.java`
- [X] T026 [US2] Add execution and transaction-matrix E2E coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/ExecuteQueryTransactionE2ETest.java`

**Checkpoint**: User Story 2 should now be independently testable on top of the
foundation and should not require User Story 3 to validate execution semantics.

---

## Phase 5: User Story 3 - 运行边界、审计与变化可见性 (Priority: P3)

**Goal**: 让 capability、审计、运行边界与结构 / DCL 变化可见性形成
当前实现的最小治理闭环，满足运行与排障场景。

**Independent Test**: 验证审计记录字段完整性、结构 / DCL 变化的同步可见性，
以及本地模式 `Origin` 边界行为。

### Tests for User Story 3

- [X] T027 [P] [US3] Add audit-facade unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/audit/AuditFacadeTest.java`
- [X] T028 [P] [US3] Add metadata-refresh coordinator unit tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinatorTest.java`
- [X] T029 [P] [US3] Add runtime audit and refresh E2E coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/RuntimeAuditE2ETest.java`

### Implementation for User Story 3

- [X] T030 [P] [US3] Implement audit recording and digest mapping in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/audit/AuditFacade.java`
- [X] T031 [P] [US3] Implement metadata refresh coordination for session/global visibility in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinator.java`
- [X] T032 [US3] Wire DDL / DCL refresh visibility into `execute_query` handling in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java`
- [X] T033 [US3] Harden local-mode `Origin` validation and follow-up session checks in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMcpServer.java`

**Checkpoint**: User Story 3 should be independently verifiable once the
runtime boundary, audit, and refresh paths are implemented, even though it enriches US1/US2.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finish packaging, operations assets, and final verification.

- [X] T034 [P] Package MCP runtime configuration and scripts in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`
- [X] T035 [P] Add standalone container and runtime assets in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/Dockerfile`
- [X] T036 [P] Add distribution control scripts in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/bin/start.sh`
- [X] T037 [P] Update operator-facing rollout notes in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [X] T038 Run the end-to-end quickstart validation against `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md`
- [X] T043 Add 12-database capability-matrix provider coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java`
- [X] T044 Run scoped Checkstyle, Spotless, and Maven verification for MCP modules against `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/pom.xml`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Foundational and is the recommended MVP.
- **User Story 2 (P2)**: Starts after Foundational; integrates with the shared
  transport/session skeleton but does not require User Story 3 to validate.
- **User Story 3 (P3)**: Starts after Foundational; enriches US1/US2 with
  audit, refresh visibility, and runtime boundary checks but remains independently testable.

### Within Each User Story

- Write or update the story tests before finalizing implementation.
- Domain models and assemblers come before higher-level dispatchers.
- Core logic comes before bootstrap wiring.
- E2E coverage lands after story behavior is runnable end-to-end.

### Parallel Opportunities

- `T003`-`T006` can proceed in parallel after `T001`/`T002`.
- `T008`-`T010` can proceed in parallel after `T007`.
- In **US1**, `T013`, `T014`, `T015`, `T016`, `T017`, and `T042` can run in parallel on
  separate files once the foundational phase is done.
- In **US2**, `T020`, `T021`, `T022`, `T023`, `T024`, `T039`, and `T040` can be split across
  different owners after `T009` and `T011`.
- In **US3**, `T027`, `T028`, `T029`, `T030`, and `T031` can run in parallel
  after the core session and execution skeleton is stable.
- `T034`-`T037` and `T044` are parallelizable after the runtime surfaces are in place.

---

## Parallel Example: User Story 1

```bash
# Parallelizable test and domain work for User Story 1
Task: "T013 Add capability assembler unit tests in mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssemblerTest.java"
Task: "T014 Add metadata discovery integration tests in mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/MetadataDiscoveryIntegrationTest.java"
Task: "T042 Add supported-database baseline contract tests in mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/SupportedDatabaseBaselineTest.java"
Task: "T015 Implement service and database capability assemblers in mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java"
Task: "T016 Implement metadata resource loaders and normalized object mapping in mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MetadataResourceLoader.java"
Task: "T017 Implement metadata tool handlers in mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcher.java"
```

## Parallel Example: User Story 2

```bash
# Parallelizable execution and transport work for User Story 2
Task: "T020 Add execute-query unit tests in mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacadeTest.java"
Task: "T021 Add runtime integration tests in mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StreamableHttpRuntimeIntegrationTest.java"
Task: "T040 Add STDIO transport integration tests in mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StdioTransportIntegrationTest.java"
Task: "T022 Implement statement classification in mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/StatementClassifier.java"
Task: "T023 Implement execute_query facade in mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacade.java"
Task: "T024 Implement transaction command execution in mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/TransactionCommandExecutor.java"
Task: "T039 Implement STDIO runtime bootstrap in mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/stdio/StdioMcpServer.java"
```

## Parallel Example: User Story 3

```bash
# Parallelizable audit and refresh work for User Story 3
Task: "T027 Add audit facade tests in mcp/core/src/test/java/org/apache/shardingsphere/mcp/audit/AuditFacadeTest.java"
Task: "T028 Add refresh coordinator tests in mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinatorTest.java"
Task: "T029 Add runtime audit E2E coverage in test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/RuntimeAuditE2ETest.java"
Task: "T030 Implement audit recording in mcp/core/src/main/java/org/apache/shardingsphere/mcp/audit/AuditFacade.java"
Task: "T031 Implement refresh coordination in mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/MetadataRefreshCoordinator.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for metadata discovery and capability exposure.
3. Validate User Story 1 independently with its integration and E2E tests.
4. Demo the unified discovery contract before starting execution semantics.

### Incremental Delivery

1. Build isolation and skeleton first.
2. Deliver User Story 1 as the discovery MVP.
3. Add User Story 2 for execution and transaction semantics.
4. Add User Story 3 for runtime boundary checks, auditing, and refresh guarantees.
5. Finish with packaging, operator assets, and quickstart validation.

### Parallel Team Strategy

1. One owner lands the Maven/profile/toolchain setup.
2. One owner builds `mcp/core` domain primitives while another builds
   `mcp/bootstrap` transport wiring.
3. After the foundational phase, separate owners can take US1, US2, and US3.
4. Distribution and docs polish can run in parallel once the runtime stabilizes.

---

## Notes

- All tasks follow the required checklist format with IDs, optional `[P]`
  markers, story labels, and exact file paths.
- Total tasks: 44
- User Story task counts:
  - US1: 8 tasks
  - US2: 10 tasks
  - US3: 7 tasks
- Suggested MVP scope: Phase 1 + Phase 2 + User Story 1
