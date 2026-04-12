# Tasks: ShardingSphere MCP Execute Query Schema Semantics

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/018-shardingsphere-mcp-execution-schema-semantics/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Include explicit capability, metadata-normalization, and execute-query semantics tests because this feature is primarily about product contract correctness.

**Organization**: Tasks are grouped by user story so capability disclosure, execute-query semantics, and documentation alignment can land as separate slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Contract Alignment)

**Purpose**: 先把产品语义写清楚，再进入代码实现。

- [ ] T001 Add schema-execution follow-up notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md`
- [ ] T002 [P] Add execute-query schema semantics notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T003 [P] Add detailed schema-semantics wording to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- [ ] T004 [P] Reconcile `execute_query` input definition in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
- [ ] T005 [P] Reconcile `DatabaseCapability` and `ExecuteQueryRequest` fields in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/data-model.md`
- [ ] T006 [P] Reconcile `execute_query(database, schema?, sql, ...)` contract in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

---

## Phase 2: Foundational (Blocking Capability and Metadata Semantics)

**Purpose**: 先落 capability model 与 metadata normalization，这两项会阻塞后续 execute-query 语义实现。

**CRITICAL**: No user story work should be considered complete until this phase is done.

- [ ] T007 Create `SchemaExecutionSemantics` enum in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/SchemaExecutionSemantics.java`
- [ ] T008 [P] Extend database capability option contract in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityOption.java`
- [ ] T009 [P] Add execution-schema semantics to runtime capability model in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapability.java`
- [ ] T010 [P] Publish `schemaExecutionSemantics` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponse.java`
- [ ] T011 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/MySQLMCPDatabaseCapabilityOption.java`
- [ ] T012 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/MariaDBMCPDatabaseCapabilityOption.java`
- [ ] T013 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/ClickHouseMCPDatabaseCapabilityOption.java`
- [ ] T014 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/HiveMCPDatabaseCapabilityOption.java`
- [ ] T015 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/PostgreSQLMCPDatabaseCapabilityOption.java`
- [ ] T016 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/OpenGaussMCPDatabaseCapabilityOption.java`
- [ ] T017 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/SQLServerMCPDatabaseCapabilityOption.java`
- [ ] T018 [P] Assign per-dialect execution schema semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/H2MCPDatabaseCapabilityOption.java`, `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/OracleMCPDatabaseCapabilityOption.java`, `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/PrestoMCPDatabaseCapabilityOption.java`, `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/FirebirdMCPDatabaseCapabilityOption.java`, `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/DorisMCPDatabaseCapabilityOption.java`
- [ ] T019 Normalize no-native-schema metadata names in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java`
- [ ] T020 [P] Add capability matrix coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java`
- [ ] T021 [P] Add metadata normalization coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoaderTest.java`
- [ ] T022 [P] Add capability response payload coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponseTest.java`

**Checkpoint**: capability 已能发布 execution-time schema semantics，metadata 已对 no-native-schema 数据库使用逻辑 `database` 名称归一化 `schema`。

---

## Phase 3: User Story 1 - 调用方先看到 schema 的执行语义 (Priority: P1) MVP

**Goal**: 让 Agent 可以先通过 capability 判断 `schema` 是固定标签还是 best-effort hint。

**Independent Test**: `get_capabilities(database)` 能区分 MySQL-like 与 PostgreSQL-like 的 schema 执行语义。

### Tests for User Story 1

- [ ] T023 [P] [US1] Extend capability-provider assertions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java`
- [ ] T024 [P] [US1] Add database-capability response assertions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponseTest.java`

### Implementation for User Story 1

- [ ] T025 [P] [US1] Surface `schemaExecutionSemantics` through `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProvider.java`
- [ ] T026 [US1] Keep capability field ordering stable in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponse.java`

**Checkpoint**: 调用方只读 capability 就能知道当前数据库如何解释 `schema`。

---

## Phase 4: User Story 2 - `execute_query.schema` 被定义为 optional namespace hint (Priority: P1)

**Goal**: 把 `schema` 从隐含 selector 收敛成 capability-guided optional hint。

**Independent Test**: executor 对 `FIXED_TO_DATABASE` 与 `BEST_EFFORT` 走不同语义路径，且不新增 fail-fast 校验。

### Tests for User Story 2

- [ ] T027 [P] [US2] Add fixed-to-database and best-effort executor coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutorTest.java`
- [ ] T028 [P] [US2] Add facade-level execute-query contract coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeTest.java`
- [ ] T029 [P] [US2] Add tool-descriptor contract coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/ExecuteSQLToolHandlerTest.java`

### Implementation for User Story 2

- [ ] T030 [P] [US2] Clarify request field semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/request/SQLExecutionRequest.java`
- [ ] T031 [P] [US2] Update tool descriptor wording in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/ExecuteSQLToolHandler.java`
- [ ] T032 [US2] Align schema application logic with capability semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutor.java`

**Checkpoint**: `execute_query.schema` 的行为已与 capability 语义对齐，且没有新增 fail-fast 拒绝路径。

---

## Phase 5: User Story 3 - metadata、capability 和文档话术一致 (Priority: P2)

**Goal**: 把同一个 `schema` 在 metadata、capability 和文档中的意思统一起来。

**Independent Test**: no-native-schema 的 metadata 名称、capability 值和文档定义不会互相冲突。

### Tests for User Story 3

- [ ] T033 [P] [US3] Extend metadata-loader assertions for normalized schema naming in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoaderTest.java`

### Implementation for User Story 3

- [ ] T034 [P] [US3] Update PRD wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md`
- [ ] T035 [P] [US3] Update technical design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T036 [P] [US3] Update detailed design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- [ ] T037 [P] [US3] Update top-level MCP V1 spec wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
- [ ] T038 [P] [US3] Update top-level data model wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/data-model.md`
- [ ] T039 [US3] Update domain contract wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

**Checkpoint**: metadata、capability 和 `execute_query` 在所有主文档中的语义保持一致。

---

## Phase 6: Polish & Verification

**Purpose**: 收口最后的测试与文档一致性。

- [ ] T040 [P] Run capability scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T041 [P] Run metadata scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T042 [P] Run execute-query scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T043 [P] Run scoped style checks in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T044 [P] Reconcile final wording across `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md` and `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because capability disclosure 决定调用方是否能安全使用 `schema`。
- **User Story 2 (P1)**: Depends on capability model from US1 being stable enough to drive executor behavior.
- **User Story 3 (P2)**: Depends on capability and executor semantics being stable enough to document.

### Parallel Opportunities

- `T002` to `T006` can run in parallel after `T001`.
- `T008` to `T018` can run in parallel after `T007`.
- `T020`, `T021`, and `T022` can run in parallel once capability and metadata changes are in place.
- `T027`, `T028`, and `T029` can run in parallel for execute-query verification.
- `T035` to `T040` can run in parallel for documentation alignment.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 so capability 先说清楚 schema 的执行语义。
3. Validate that callers can distinguish `FIXED_TO_DATABASE` and `BEST_EFFORT` without reading prose docs only.

### Incremental Delivery

1. Publish schema execution semantics in capability.
2. Normalize no-native-schema metadata names.
3. Align execute-query request semantics with capability.
4. Reconcile all contracts and design docs.

## Notes

- This task list intentionally avoids introducing new fail-fast schema validation.
- The primary success signal is contract honesty and consistency, not more aggressive runtime rejection.
- `ExecuteSQLToolHandlerTest` and `MCPDatabaseCapabilityResponseTest` may be new test files if coverage does not already exist.
