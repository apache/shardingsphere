# Tasks: MCP Request-Scoped Metadata Lifecycle

**Input**: Design documents from `/.specify/specs/002-mcp-request-scoped-metadata/`
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`
**Tests**: Add or update scoped tests for each touched request path; keep response field order unchanged.

**Organization**: Tasks are grouped by user story so the refactor can be implemented and verified in controlled slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Code Cut Points

- **Delete after replacement**
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPDatabaseMetadataCatalog.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataRefresher.java`
- **Rewrite around request lifecycle**
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/MCPRuntimeContext.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolController.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandler.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandler.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolService.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowPlanningService.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationService.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowProxyQueryService.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacade.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcTransactionStatementExecutor.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcTransactionResourceManager.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java`
- **New infrastructure expected**
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/request/MCPRequestContext.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/request/MCPRequestContextFactory.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/context/RequestScopedMetadataContext.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPRequestScopedMetadataLoader.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityCache.java`
  - `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPJdbcDatabaseCapabilityLoader.java`

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/MCPRuntimeContext.java` so runtime startup wires `MCPSessionManager`, runtime database configs, and process-level capability cache only, with no startup-time metadata catalog preload.
- [ ] T002 Create `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPJdbcDatabaseCapabilityLoader.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityCache.java` to resolve and retain database capability independently from request-scoped metadata.
- [ ] T003 Create `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/request/MCPRequestContext.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/request/MCPRequestContextFactory.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/context/RequestScopedMetadataContext.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPRequestScopedMetadataLoader.java` as the new per-request metadata lifecycle backbone.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandler.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandler.java` so handlers receive request-scoped context instead of reaching global metadata state through `MCPRuntimeContext`.
- [ ] T005 Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolController.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java` to create one `MCPRequestContext` per MCP call and close it at the end of the call on both success and failure paths.
- [ ] T006 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java` and `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java` so every tool call and resource read runs through the new per-request context boundary without changing payload shape or field order.

**Checkpoint**: Request lifecycle is explicit and all later metadata consumers can share one request-scoped context inside one MCP call.

---

## Phase 3: User Story 1 - Read fresh metadata on every new request (Priority: P1)

**Goal**: Every new MCP call loads metadata from current database state instead of a stale process-wide snapshot.
**Independent Test**: Finish one request, change schema externally, then verify a later request sees the changed metadata without restart or manual refresh.

### Tests for User Story 1

- [ ] T010 [P] [US1] Add request-isolation coverage in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/MCPResourceControllerTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java`.
- [ ] T011 [P] [US1] Add bootstrap wiring coverage in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncherTest.java`, `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`, and `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactoryTest.java`.

### Implementation for User Story 1

- [ ] T012 [US1] Replace the catalog-oriented contract in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java` with request-scoped metadata access backed by `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/context/RequestScopedMetadataContext.java`.
- [ ] T013 [US1] Rewrite metadata resource handlers under `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/` so they resolve metadata through the active request context rather than `runtimeContext.getMetadataCatalog()`.
- [ ] T014 [US1] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolService.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolHandler.java` to use request-scoped metadata loading with unchanged pagination and response ordering.

**Checkpoint**: Metadata reads are fresh per request and no longer depend on startup preload or a process-wide metadata snapshot.

---

## Phase 4: User Story 2 - Reuse metadata within a single request only (Priority: P1)

**Goal**: Metadata loaded earlier in one MCP call is reused later in that same call and discarded when the call finishes.
**Independent Test**: Run one request flow that touches the same metadata more than once and verify no duplicate load happens for already loaded request-local metadata.

### Tests for User Story 2

- [ ] T020 [P] [US2] Add focused request-reuse coverage for the new request context in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/` and update `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolServiceTest.java`.
- [ ] T021 [P] [US2] Add workflow-path reuse coverage in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowPlanningServiceTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationServiceTest.java`.

### Implementation for User Story 2

- [ ] T022 [US2] Implement append-and-reuse semantics in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/context/RequestScopedMetadataContext.java` so metadata loaded earlier in the request is reused and newly touched scopes can be added without affecting earlier request state.
- [ ] T023 [US2] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowPlanningService.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationService.java` so all metadata reads inside one workflow tool call share the same request-scoped context.
- [ ] T024 [US2] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowProxyQueryService.java` so database-type-dependent behavior uses capability information instead of global metadata lookup.

**Checkpoint**: Repeated metadata access within one MCP call is efficient, while request state never leaks into the next call.

---

## Phase 5: User Story 3 - Keep capability available without global metadata state (Priority: P1)

**Goal**: Capability remains process-scoped and available before any request-scoped metadata is loaded.
**Independent Test**: Start the runtime with no metadata preload and verify capability-driven resource and execution paths still work from the process-level capability cache.

### Tests for User Story 3

- [ ] T030 [P] [US3] Rewrite `mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java` around the new process-level capability cache contract.
- [ ] T031 [P] [US3] Add capability bootstrap coverage in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeConcurrencyTest.java` and capability resource coverage in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerTest.java`.

### Implementation for User Story 3

- [ ] T032 [US3] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProvider.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/capability/DatabaseCapabilitiesHandler.java` so capability comes only from `MCPDatabaseCapabilityCache`.
- [ ] T033 [US3] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacade.java` to consume request-scoped metadata and process-level capability as separate concerns.

**Checkpoint**: Capability is fully decoupled from metadata lifecycle and no longer needs global metadata state to function.

---

## Phase 6: User Story 4 - Preserve external behavior while removing legacy refresh flows (Priority: P2)

**Goal**: Remove obsolete refresh logic and compatibility scaffolding without changing externally visible behavior.
**Independent Test**: Run existing execution, planning, validation, search, and capability flows and confirm their behavior and payload field order remain stable.

### Tests for User Story 4

- [ ] T040 [P] [US4] Rewrite execution-path coverage in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeTest.java`, `mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/MCPJdbcTransactionStatementExecutorTest.java`, and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcTransactionResourceManagerTest.java`.
- [ ] T041 [P] [US4] Remove or replace catalog/refresh-only tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/model/MCPDatabaseMetadataCatalogTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataRefresherTest.java`.

### Implementation for User Story 4

- [ ] T042 [US4] Delete `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPDatabaseMetadataCatalog.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataRefresher.java` after all callers have moved to the new architecture.
- [ ] T043 [US4] Simplify `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcTransactionResourceManager.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcTransactionStatementExecutor.java` by removing dirty-metadata tracking and commit-time metadata refresh behavior.
- [ ] T044 [US4] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationService.java` so validation no longer performs pre-check refreshes and instead relies on the current request-scoped metadata context.

**Checkpoint**: The old metadata refresh-and-repair model is gone, while external MCP behavior remains functionally stable.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T050 [P] Verify response field order remains unchanged by reviewing touched `toPayload()` producers and related assertions in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/` and `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/`.
- [ ] T051 Run `./mvnw -pl mcp/core -am test -Dsurefire.failIfNoSpecifiedTests=false`, `./mvnw -pl mcp/bootstrap -am test -Dsurefire.failIfNoSpecifiedTests=false`, and the relevant scoped style checks for touched modules.

## Dependencies & Execution Order

- Phase 1 must finish before handler and controller rewrites begin.
- Phase 2 blocks all user story work because request lifecycle boundaries must be explicit first.
- User Story 1 and User Story 3 can progress in parallel after Phase 2 if file ownership is split cleanly.
- User Story 2 depends on User Story 1 because in-request reuse builds on the new request-scoped metadata access path.
- User Story 4 depends on User Story 1 and User Story 3 because refresh cleanup is safe only after all metadata and capability callers have been migrated.
- Polish runs last after all desired slices are complete.
