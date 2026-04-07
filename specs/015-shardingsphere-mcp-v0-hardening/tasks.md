# Tasks: ShardingSphere MCP V0 Surface Hardening

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/`  
**Prerequisites**: plan.md (required), spec.md (required)

**Tests**: Add and update dedicated `mcp/core` and `mcp/bootstrap` tests for truncation, strict validation, and loopback-only HTTP config.

**Organization**: Tasks are grouped by V0 delivery slice so contract/doc alignment, correctness fixes, and security hardening can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`, `US4`)
- Every task includes an exact file path
- No unrelated cleanup tasks may be added

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the V0 scope before code changes.

- [X] T001 Add V0 scope, constraints, and acceptance criteria to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/spec.md`
- [X] T002 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/plan.md`
- [X] T003 [P] Freeze executable tasks in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/tasks.md`

---

## Phase 2: User Story 1 - Public surface 收口到 resource-only (Priority: P1)

**Goal**: Remove stale metadata tool promises from public docs and contract.

**Independent Test**: review the updated docs and grep for removed public metadata tools.

### Implementation for User Story 1

- [X] T004 [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`
  to keep only `search_metadata` and `execute_query` as public tools and document capabilities/detail discovery as resources
- [X] T005 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md`
  to replace removed metadata tools with resource-based discovery flow
- [X] T006 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`
  to describe resource-only metadata discovery and fix stale source-path references
- [X] T007 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
  to describe resource-only metadata discovery and fix stale source-path references

---

## Phase 3: User Story 2 - `execute_query` 截断语义修复 (Priority: P1)

**Goal**: Make `truncated` reflect whether rows were actually cut off by `max_rows`.

**Independent Test**: core executor test and HTTP production integration test both cover truncated and non-truncated branches.

### Tests for User Story 2

- [X] T008 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutorTest.java`
  to assert `truncated=true` when result rows exceed `max_rows`
- [X] T009 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionExecuteQueryIntegrationTest.java`
  to assert the same behavior over HTTP

### Implementation for User Story 2

- [X] T010 [US2] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutor.java`
  to detect overflow by reading at most one extra row beyond `max_rows`

---

## Phase 4: User Story 3 - Tool 参数严格校验 (Priority: P1)

**Goal**: Reject blank required strings and invalid `object_types` with `invalid_request`.

**Independent Test**: tool controller tests cover blank required args and invalid `object_types`; legal filtering still works.

### Tests for User Story 3

- [X] T011 [P] [US3] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java`
  to cover blank `query`, blank `database`, blank `sql`, and invalid `object_types`
- [X] T012 [P] [US3] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolServiceTest.java`
  only as needed to preserve the valid filter semantics

### Implementation for User Story 3

- [X] T013 [US3] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolController.java`
  to validate required string fields as non-blank
- [X] T014 [P] [US3] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/request/MCPToolArguments.java`
  to reject explicitly provided `object_types` values outside the current public search surface

---

## Phase 5: User Story 4 - HTTP 默认边界收紧到 loopback-only (Priority: P1)

**Goal**: Fail non-loopback HTTP binds during config assembly.

**Independent Test**: YAML swapper and config loader tests both reject non-loopback `bindHost`.

### Tests for User Story 4

- [X] T015 [P] [US4] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`
  to accept loopback hosts and reject non-loopback hosts
- [X] T016 [P] [US4] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidatorTest.java`
  to align with loopback-only config behavior

### Implementation for User Story 4

- [X] T017 [US4] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`
  to reject non-loopback `bindHost` values during swap

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency and scope guard.

- [X] T018 [P] Run scoped core verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [X] T019 [P] Run scoped bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [X] T020 [P] Run distribution packaging verification in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/pom.xml`
- [X] T021 [P] Run scoped style checks for touched MCP modules
- [X] T022 [P] Reconcile final implementation notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/015-shardingsphere-mcp-v0-hardening/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed first to freeze scope.
- **User Story 1 (Phase 2)**: Can land before code changes and should go first so public surface stops drifting.
- **User Story 2-4 (Phase 3-5)**: Can proceed independently after Phase 1.
- **Polish (Phase 6)**: Runs after selected implementation slices are complete.

### Parallel Opportunities

- `T005`, `T006`, and `T007` can run in parallel as doc alignment.
- `T008` and `T009` can run in parallel with `T010`.
- `T011` and `T012` can run in parallel with `T013` and `T014`.
- `T015` and `T016` can run in parallel with `T017`.

## Notes

- 这轮 feature 的评审重点是 “口径是否真实、行为是否正确、默认边界是否保守”。
- 任何恢复旧 tools 或引入新的 remote HTTP 放开开关的想法，都必须拆到后续 follow-up。
