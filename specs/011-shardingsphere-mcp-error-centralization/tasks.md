# Tasks: ShardingSphere MCP Error Centralization and Protocol Error Conversion

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/mcp-error-contract.md

**Tests**: Add and update dedicated core tests for typed exceptions and centralized `MCPErrorResponse` conversion, plus one bootstrap tool-call regression.

**Organization**: Tasks are grouped by user story so internal exceptionization, centralized conversion,
and `execute_query` failure cleanup can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path
- Only error-handling-related files are allowed in implementation; no unrelated cleanup tasks may be added

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the centralized error-conversion scope before code changes.

- [X] T001 Add centralized error-conversion scope and non-goals to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/spec.md`
- [X] T002 [P] Record exception-hierarchy and boundary decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/research.md`
- [X] T003 [P] Capture the converter and exception data model in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/data-model.md`
- [X] T004 [P] Freeze public error payload and mapping rules in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/contracts/mcp-error-contract.md`
- [X] T005 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/plan.md`

---

## Phase 2: Foundational (Shared Exception and Converter Infrastructure)

**Purpose**: Introduce the minimum shared infrastructure before changing runtime paths.

**CRITICAL**: No runtime path should be migrated before the converter and exception hierarchy exist.

- [ ] T006 Add the exception hierarchy under `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/exception/`
- [ ] T007 [P] Add the centralized converter in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/response/MCPProtocolErrorConverter.java`
- [ ] T008 [P] Add dedicated converter tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/protocol/MCPProtocolErrorConverterTest.java`

**Checkpoint**: Core has one shared exception model and one shared `MCPErrorResponse` converter.

---

## Phase 3: User Story 1 - 内部链路只抛异常，不构造协议错误 (Priority: P1)

**Goal**: Remove failure-as-data from metadata and tool dispatch internals.

**Independent Test**: metadata and dispatch tests assert typed exceptions instead of failure results.

### Tests for User Story 1

- [ ] T009 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryServiceTest.java`
  to assert typed exceptions for unsupported and invalid paths
- [ ] T010 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcherTest.java`
  to assert typed exceptions and preserved `search_metadata` skip behavior

### Implementation for User Story 1

- [ ] T011 [US1] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java`
  to throw typed exceptions instead of `MetadataQueryResult.error(...)`
- [ ] T012 [P] [US1] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcher.java`
  to throw typed exceptions instead of `ToolDispatchResult.error(...)`
- [ ] T013 [P] [US1] Reduce failure APIs from
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryResult.java`
  and
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/ToolDispatchResult.java`
  so they no longer carry error results on the main path

**Checkpoint**: metadata and tool dispatch internals only express failure by throwing exceptions.

---

## Phase 4: User Story 2 - 协议入口统一 catch 并转换为 MCPErrorResponse (Priority: P1)

**Goal**: Make tool and resource entrypoints the only runtime catch-and-convert boundaries.

**Independent Test**: tool and resource regression tests prove unsupported, not-found, invalid-request and unsupported metadata paths all return `MCPErrorResponse`.

### Tests for User Story 2

- [ ] T014 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResolverTest.java`
  to verify centralized error payload conversion
- [ ] T015 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/MCPResourceControllerTest.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerTest.java`
  to verify resource-side centralized conversion
- [ ] T016 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolCallHandlerTest.java`
  to verify tool-call rendering still uses centralized error payload

### Implementation for User Story 2

- [ ] T017 [US2] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResolver.java`
  to catch internal exceptions and delegate to `MCPProtocolErrorConverter`
- [ ] T018 [P] [US2] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResult.java`
  into a passive payload container with no local `MCPErrorResponse` construction
- [ ] T019 [P] [US2] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java`
  to become the only resource-side catch-and-convert boundary
- [ ] T020 [P] [US2] Remove distributed resource-side conversions from
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPResourceResponseFactory.java`,
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/capability/DatabaseCapabilitiesHandler.java`
  and
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/AbstractMetadataResourceHandler.java`

**Checkpoint**: all protocol conversion is concentrated at the two runtime entrypoints.

---

## Phase 5: User Story 3 - execute_query 失败路径和确定异常类一次性收口 (Priority: P1)

**Goal**: Remove `ExecuteQueryResponse.error(...)` and make SQL failures participate in the same exception-to-`MCPErrorResponse` path.

**Independent Test**: execute-path tests prove failures now throw typed exceptions internally and resolve to centralized error payloads externally.

### Tests for User Story 3

- [ ] T021 [P] [US3] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacadeTest.java`
  to verify typed exception propagation and centralized tool-level mapping
- [ ] T022 [P] [US3] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutorTest.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/MCPJdbcTransactionStatementExecutorTest.java`
  to assert exceptions instead of failure responses
- [ ] T023 [P] [US3] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/protocol/ExecuteQueryResponseTest.java`
  so it only covers success responses

### Implementation for User Story 3

- [ ] T024 [US3] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutor.java`
  to wrap SQL failures in typed MCP exceptions instead of returning `ExecuteQueryResponse.error(...)`
- [ ] T025 [P] [US3] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcTransactionStatementExecutor.java`
  to throw typed MCP exceptions instead of returning failure responses
- [ ] T026 [P] [US3] Refactor `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacade.java`
  so facade-level validation and auditing work with exception-based failures
- [ ] T027 [P] [US3] Remove failure support from
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/response/ExecuteQueryResponse.java`
  and align `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionNotExistedException.java`
  with the new exception hierarchy

**Checkpoint**: `execute_query` failures no longer use a custom error envelope and instead flow through centralized conversion.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency and scope guard.

- [ ] T028 [P] Run scoped core verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T029 [P] Run scoped bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T030 [P] Run static grep verification to confirm old failure factories and distributed `MCPErrorResponse` construction are gone
- [ ] T031 [P] Align final implementation notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed with this design round.
- **Foundational (Phase 2)**: Blocks all runtime migrations because exception hierarchy and converter must exist first.
- **User Stories (Phase 3-5)**: Depend on the shared converter and family exceptions from Phase 2.
- **Polish (Phase 6)**: Runs after the selected implementation slice is complete.

### User Story Dependencies

- **User Story 1 (P1)**: First delivery slice because internal failure-as-data must be removed before centralized conversion is complete.
- **User Story 2 (P1)**: Depends on US1 so protocol entrypoints receive exceptions rather than mixed result objects.
- **User Story 3 (P1)**: Depends on US2 because `execute_query` failure also exits through the tool protocol boundary.

### Parallel Opportunities

- `T007` and `T008` can run in parallel once the exception package name is frozen.
- `T009` and `T010` can run in parallel while metadata/tool dispatch code is being migrated.
- `T014`, `T015`, and `T016` can run in parallel as entrypoint regression slices.
- `T021`, `T022`, and `T023` can run in parallel as execute-path regression updates.
- `T028`, `T029`, and `T030` can run in parallel as final verification.

## Implementation Strategy

### MVP First

1. Add the exception hierarchy and centralized converter.
2. Migrate metadata/tool dispatch to exception-based failures.
3. Make tool and resource entrypoints the only catch-and-convert boundaries.
4. Remove `ExecuteQueryResponse.error(...)` and migrate execute-path failures.

### Incremental Delivery

1. Freeze design and mapping rules.
2. Land shared exception infrastructure.
3. Migrate metadata/tool/resource runtime paths.
4. Migrate execute runtime paths.
5. Reconfirm no unrelated code changed and all legacy failure factories are gone.

## Notes

- 这轮 feature 的 review 重点是“边界是否唯一”和“是否越界改动”，不是代码风格优化。
- 如果实现过程中发现需要无关重构，必须拆到后续 follow-up，不得混入本轮。
