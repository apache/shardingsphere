# Tasks: MCP Public API Flattening

**Input**: Design documents from `/.specify/specs/007-mcp-api-core-boundary/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`
**Tests**: Update or add module-scoped tests for `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, and `mcp/bootstrap`.

**Organization**: Tasks are grouped by user story so the API flattening and workflow routing redesign can land in reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Non-Negotiable Invariants

- `mcp/api` must remain a pure API module with no production implementation classes.
- Public MCP capability categories must stay at one abstraction level: only `tool` and `resource`.
- Workflow remains a shared internal architecture layer, not a public top-level capability category.
- Generic workflow apply and validate must route by explicit `workflowKind`.
- `mcp/features/encrypt` and `mcp/features/mask` must not gain a direct dependency on `mcp/core`.

---

## Phase 1: Setup and inventory

- [ ] T001 Produce a public-surface inventory for `mcp/api/src/main/java/org/apache/shardingsphere/mcp/` and `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/`, classifying every retained type as `public-tool`, `public-resource`, `descriptor-or-dto`, `context-capability`, `exception`, or `internal-only`, and explicitly flagging `mcp/api/src/main/java/org/apache/shardingsphere/mcp/feature/spi/MCPContribution.java`, `MCPToolContribution.java`, `MCPResourceContribution.java`, `MCPDirectToolContribution.java`, `MCPDirectResourceContribution.java`, `MCPToolInvoker.java`, `MCPResourceReader.java`, and `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/spi/MCPWorkflowToolContribution.java` as removal targets.
- [ ] T002 Record the provider-surface transition points in `mcp/api/src/main/java/org/apache/shardingsphere/mcp/feature/spi/MCPFeatureProvider.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPContributionRegistry.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPFeatureProviderRegistry.java` so the contribution-based loading path is removed without introducing replacement wrapper concepts.
- [ ] T003 [P] Record the workflow runtime ownership split across `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/`, covering the destination of `workflowKind`, internal workflow definitions, generic apply/validate descriptors, and workflow validation/synchronization seams.

---

## Phase 2: Foundational boundary changes

- [ ] T004 Redesign `mcp/api/src/main/java/org/apache/shardingsphere/mcp/feature/spi/MCPFeatureProvider.java` so feature contribution is expressed directly through public tool and public resource contracts, and remove the public contribution hierarchy from `mcp/api/src/main/java/org/apache/shardingsphere/mcp/feature/spi/`.
- [ ] T005 [P] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPContributionRegistry.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPFeatureProviderRegistry.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPToolContributionMaterializer.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPResourceContributionMaterializer.java` so core loads public tool/resource contracts directly instead of materializing contribution wrappers.
- [ ] T006 [P] Introduce explicit workflow identity and internal workflow definition seams by updating `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/model/WorkflowContextSnapshot.java` and adding workflow-definition contracts under `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/`.
- [ ] T007 [P] Update `mcp/api/pom.xml`, `mcp/workflow/pom.xml`, `mcp/core/pom.xml`, `mcp/features/encrypt/pom.xml`, `mcp/features/mask/pom.xml`, and `mcp/bootstrap/pom.xml` so dependency edges match the pure-API and workflow-shared ownership split.

**Checkpoint**: The base contract/runtime boundary is redrawn; user story work can proceed without reintroducing public assembly types.

---

## Phase 3: User Story 1 - Expose only same-level public MCP capabilities (Priority: P1)

**Goal**: Make the public MCP API readable through only `tool` and `resource` capability concepts.
**Independent Test**: Inspect providers and API-boundary tests to confirm that no public workflow contribution, direct contribution, invoker, reader, or materializer-style public category remains.

### Tests for User Story 1

- [ ] T008 [P] [US1] Update `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/EncryptFeatureProviderTest.java`, `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/MaskFeatureProviderTest.java`, and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/feature/MCPFeatureProviderRegistryTest.java` so they assert that providers expose only public tool and public resource contracts.
- [ ] T009 [P] [US1] Add an API-boundary test at `mcp/api/src/test/java/org/apache/shardingsphere/mcp/feature/spi/MCPFeatureProviderBoundaryTest.java` that fails if public contribution, invoker, reader, or workflow contribution wrapper types remain in the API module.

### Implementation for User Story 1

- [ ] T010 [US1] Remove public contribution-style types from `mcp/api/src/main/java/org/apache/shardingsphere/mcp/feature/spi/`, keeping only pure API-level provider contracts and any surviving API-owned DTO or exception types.
- [ ] T011 [US1] Rewrite `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/EncryptFeatureProvider.java`, `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/MaskFeatureProvider.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/core/CoreFeatureProvider.java` so they contribute only public tools and resources without `MCPDirect*Contribution` or `MCPWorkflowToolContribution`.
- [ ] T012 [US1] Remove or collapse runtime wrappers that exist only to materialize the old contribution surface, including `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/DelegatingToolHandler.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/DelegatingResourceHandler.java`.

**Checkpoint**: A reviewer can inspect `mcp/api` and provider implementations and find only same-level public capability concepts.

---

## Phase 4: User Story 2 - Keep workflow reusable without making it a public top-level category (Priority: P1)

**Goal**: Preserve workflow reuse for encrypt, mask, and future features while keeping the public API flat.
**Independent Test**: Verify that planning remains feature-specific public tools while workflow apply and validate are generic platform-scoped tools backed by internal workflow definitions.

### Tests for User Story 2

- [ ] T013 [P] [US2] Update `mcp/workflow/src/test/java/org/apache/shardingsphere/mcp/workflow/descriptor/WorkflowToolDescriptorsTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/workflow/WorkflowToolHandlerTest.java` to cover generic workflow apply/validate descriptors and handler behavior.
- [ ] T014 [P] [US2] Update `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/handler/EncryptToolHandlerTest.java` and `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/tool/handler/MaskToolHandlerTest.java` so planning stays exposed as ordinary public tools after workflow public-surface flattening.

### Implementation for User Story 2

- [ ] T015 [US2] Replace `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/spi/MCPWorkflowToolContribution.java` with internal workflow definition seams under `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/` that bind workflow kind to planning continuation, validation behavior, and post-apply synchronization.
- [ ] T016 [US2] Rewrite `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/descriptor/WorkflowToolDescriptors.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/WorkflowExecutionToolHandler.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/WorkflowValidationToolHandler.java` so apply and validate are platform-scoped generic public tools.
- [ ] T017 [US2] Rewire `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/EncryptFeatureProvider.java`, `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/MaskFeatureProvider.java`, and workflow-shared validation/synchronization seams so encrypt and mask contribute planning tools plus internal workflow definitions rather than public workflow contribution wrappers.

**Checkpoint**: Workflow remains reusable, but it is visible to the outside world only through tools, not through a separate public workflow category.

---

## Phase 5: User Story 3 - Make internal workflow routing explicit and stable (Priority: P2)

**Goal**: Route generic workflow apply and validate through explicit workflow identity instead of hidden type inference.
**Independent Test**: Inspect workflow snapshot and runtime dispatch tests to confirm that encrypt and mask plans route by stored `workflowKind`.

### Tests for User Story 3

- [ ] T018 [P] [US3] Update `mcp/workflow/src/test/java/org/apache/shardingsphere/mcp/workflow/model/WorkflowContextSnapshotTest.java` and add workflow-definition coverage at `mcp/workflow/src/test/java/org/apache/shardingsphere/mcp/workflow/WorkflowRuntimeDefinitionTest.java` so `workflowKind` is copied, persisted, and exposed for dispatch.
- [ ] T019 [P] [US3] Update `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionServiceTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/feature/MCPFeatureProviderRegistryTest.java` so generic apply/validate prove dispatch by explicit workflow kind rather than request type or tool name guessing.

### Implementation for User Story 3

- [ ] T020 [US3] Add `workflowKind` and any required lifecycle metadata to `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/model/WorkflowContextSnapshot.java` and any related workflow payload or binder classes that persist and copy workflow state.
- [ ] T021 [US3] Introduce a runtime workflow-definition registry across `mcp/workflow/src/main/java/org/apache/shardingsphere/mcp/workflow/` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/` so generic apply/validate resolves validation and synchronization behavior from explicit workflow kind.
- [ ] T022 [US3] Rework `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/WorkflowExecutionToolHandler.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/WorkflowValidationToolHandler.java` so runtime dispatch happens exclusively through workflow kind plus internal workflow definitions.

**Checkpoint**: Generic workflow execution and validation are explicit, stable, and extensible without adding new public categories.

---

## Phase 6: Polish and cross-cutting verification

- [ ] T023 [P] Update `mcp/README.md` and `mcp/README_ZH.md` so the documentation explains the flattened public API surface, generic workflow apply/validate tools, and the internal ownership split across `mcp/api`, `mcp/workflow`, and `mcp/core`.
- [ ] T024 Remove dead public wrappers, obsolete workflow contribution tests, and compatibility shims across `mcp/api/src/main/java/`, `mcp/workflow/src/main/java/`, `mcp/core/src/main/java/`, and the corresponding test trees so no old public assembly model survives as dead code.
- [ ] T025 Run module-scoped verification for `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, and `mcp/bootstrap`, including scoped test runs plus `spotless` and `checkstyle`, and fix any resulting issues.

## Dependencies and execution order

- Phase 1 must finish before contract or runtime rewiring starts.
- Phase 2 blocks all user stories because the provider boundary and workflow identity seams must exist first.
- User Story 1 and User Story 2 can proceed in parallel after Phase 2 if the write scopes are kept separate.
- User Story 3 depends on the workflow-definition work from User Story 2 because generic dispatch needs the internal workflow registry shape to exist.
- Phase 6 runs last after all desired user stories are complete.

## Parallel execution examples

- After T004 starts, T005 and T006 can proceed in parallel because provider-registry rewiring and workflow-definition seam introduction touch different primary packages.
- After T007 completes, T008 and T009 can run in parallel because provider behavior tests and API-boundary tests have separate write scopes.
- After T015 lands, T016 and T017 can proceed in parallel because generic workflow tool shaping and feature-provider rewiring affect different modules.
- After T020 lands, T021 and T019 can proceed in parallel because runtime registry implementation and dispatch verification touch different primary files.

## Implementation strategy

- First flatten the public API surface by removing contribution-style public assembly types from `mcp/api` and rewriting provider loading around direct tool/resource contracts.
- Next keep workflow shared internally by replacing public workflow contribution wrappers with internal workflow definitions plus generic apply/validate public tools.
- Then harden runtime routing with explicit `workflowKind`, remove dead assembly code, and finish with scoped verification and documentation.
