# Tasks: ShardingSphere MCP Runtime Configuration Simplification

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/005-shardingsphere-mcp-runtime-config-simplification/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Include explicit canonical-config, legacy-alias, optional-driver, and derived-capability tests because this change is primarily about configuration contract correctness.

**Organization**: Tasks are grouped by user story so canonical config, automatic capability derivation, and migration/docs can land as separate slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Documentation and Alignment)

**Purpose**: Make the config-contract change explicit before touching implementation.

- [ ] T001 Add runtime-config simplification notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T002 [P] Add canonical runtime YAML and migration notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Land the canonical YAML model, legacy alias normalization, and shared parsing coverage before story work starts.

**CRITICAL**: No story work should be considered complete until this phase is done.

- [ ] T003 Redefine canonical runtime YAML envelope in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlRuntimeConfiguration.java`
- [ ] T004 [P] Reshape direct database binding YAML fields in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlRuntimeDatabaseConfiguration.java`
- [ ] T005 [P] Canonicalize `runtime.databaseDefaults` and legacy aliases in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeConfigurationSwapper.java`
- [ ] T006 [P] Merge metadata defaults, optional driver, and legacy capability fields in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeDatabaseConfigurationSwapper.java`
- [ ] T007 [P] Align runtime binding domain fields in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/RuntimeDatabaseConfiguration.java`
- [ ] T008 [P] Align connection configuration fields with metadata scope and optional driver in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseConnectionConfiguration.java`
- [ ] T009 [P] Extend canonical and legacy parsing coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java`
- [ ] T010 [P] Extend runtime YAML canonicalization coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeConfigurationSwapperTest.java`
- [ ] T011 [P] Extend per-database binding swapper coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeDatabaseConfigurationSwapperTest.java`
- [ ] T012 [P] Extend launch-level YAML swapper coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPLaunchConfigurationSwapperTest.java`

**Checkpoint**: Canonical YAML, legacy alias normalization, and parsing tests are aligned.

---

## Phase 3: User Story 1 - 用一套 canonical 配置表达 direct runtime (Priority: P1) MVP

**Goal**: 让 single-db 与 multi-db 都通过 `runtime.databases` 进入 default launch path，并保持 legacy `runtime.props` 作为受限迁移 alias。

**Independent Test**: canonical one-db 和 two-db 配置都能完成启动与 discovery；legacy `runtime.props` 仍能迁移为单 binding。

### Tests for User Story 1

- [ ] T013 [P] [US1] Add canonical single-db and multi-db bootstrap coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/MCPBootstrapTest.java`
- [ ] T014 [P] [US1] Add canonical launch-path and legacy alias coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoaderTest.java`

### Implementation for User Story 1

- [ ] T015 [P] [US1] Make `runtime.databases` the canonical default-launch input in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/MCPLaunchRuntimeLoader.java`
- [ ] T016 [P] [US1] Bound legacy single-db migration in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/DatabaseRuntimeFactory.java`
- [ ] T017 [US1] Update packaged canonical runtime example in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`
- [ ] T018 [US1] Update E2E runtime config rendering to emit canonical shape in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/AbstractProductionRuntimeE2ETest.java`

**Checkpoint**: Direct runtime has one canonical topology path and legacy single-db input is clearly bounded.

---

## Phase 4: User Story 2 - 数据库能力自动推导，配置只保留部署输入 (Priority: P2)

**Goal**: 去掉常规 operator capability booleans，把 `driverClassName` 降为 optional override，并让 capability 来自自动推导。

**Independent Test**: 不配置 capability booleans 仍能得到稳定 `get_capabilities` 结果；`EXPLAIN ANALYZE` 拦截与显式 driver 错误都通过自动 derivation / override 逻辑生效。

### Tests for User Story 2

- [ ] T019 [P] [US2] Add derived-capability coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssemblerTest.java`
- [ ] T020 [P] [US2] Add `EXPLAIN ANALYZE` gating coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/ExecuteQueryFacadeTest.java`
- [ ] T021 [P] [US2] Add optional-driver and explicit-driver failure coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcConnectionFactoryTest.java`

### Implementation for User Story 2

- [ ] T022 [P] [US2] Capture version-aware runtime facts in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/RuntimeDatabaseDescriptor.java`
- [ ] T023 [P] [US2] Load capability derivation inputs from JDBC metadata in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcMetadataLoader.java`
- [ ] T024 [P] [US2] Overlay automatic direct-runtime capability facts in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityAssembler.java`
- [ ] T025 [P] [US2] Extend type-level capability defaults with version-aware hooks in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/DatabaseCapabilityRegistry.java`
- [ ] T026 [US2] Keep `driverClassName` as optional override in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/runtime/JdbcConnectionFactory.java`

**Checkpoint**: Capability booleans are derived automatically and driver configuration noise is reduced.

---

## Phase 5: User Story 3 - 迁移路径明确，文档与默认配置统一 (Priority: P3)

**Goal**: 给 legacy aliases 提供明确诊断和迁移边界，同时让 README 与设计文档只推广 canonical 写法。

**Independent Test**: legacy aliases 被接受或拒绝时都得到清晰诊断；默认配置和 README 只展示 canonical 结构。

### Implementation for User Story 3

- [ ] T027 [P] [US3] Add legacy-key diagnostics and conflict validation at launch-config level in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPLaunchConfigurationSwapper.java`
- [ ] T028 [P] [US3] Update MCP operator docs to canonical runtime config in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [ ] T029 [P] [US3] Update Chinese MCP operator docs to canonical runtime config in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`
- [ ] T030 [P] [US3] Reconcile technical design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T031 [P] [US3] Reconcile detailed design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

**Checkpoint**: Migration rules are explicit and all operator-facing docs point to one canonical shape.

---

## Phase 6: Polish & Verification

**Purpose**: Close final quality and consistency gaps.

- [ ] T032 [P] Run bootstrap scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T033 [P] Run core scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T034 [P] Reconcile final wording across `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md` and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all story work.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because it delivers the canonical config path.
- **User Story 2 (P2)**: Depends on canonical config loading from US1 but is otherwise independent of documentation cleanup.
- **User Story 3 (P3)**: Depends on canonical config and migration rules being stable enough to document.

### Parallel Opportunities

- `T004`, `T005`, `T006`, `T007`, `T009`, `T010`, `T011`, and `T012` can run in parallel after `T003`.
- `T013` and `T014` can run in parallel for US1.
- `T019`, `T020`, and `T021` can run in parallel for US2 tests.
- `T022`, `T023`, `T024`, and `T025` can run in parallel once the foundational config model is stable.
- `T028`, `T029`, `T030`, and `T031` can run in parallel for documentation.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for canonical `runtime.databases` loading and migration alias bounding.
3. Validate that canonical one-db config no longer needs `runtime.props`.
4. Demo canonical config before landing capability derivation changes.

### Incremental Delivery

1. Land canonical runtime YAML and legacy alias normalization.
2. Switch default launch path and packaged config to `runtime.databases`.
3. Land automatic capability derivation and optional driver override logic.
4. Close migration diagnostics and documentation.

## Notes

- This task list focuses on direct runtime configuration simplification, not MCP protocol redesign.
- The file paths reflect the current implementation seams and can be refined during coding if the canonical config contract remains unchanged.
