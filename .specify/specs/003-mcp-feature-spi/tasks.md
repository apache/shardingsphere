# Tasks: MCP Feature SPI Modularization

**Input**: Design documents from `/.specify/specs/003-mcp-feature-spi/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/feature-spi.md`
**Tests**: Add or update module-scoped tests for `mcp/features/spi`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/core`, and `mcp/bootstrap`.

**Organization**: Tasks are grouped by user story so the refactor can be implemented and verified in independent slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Analysis Gates

以下 5 个问题必须在重度迁移前落成代码边界，而不是留到实现中途再回头返工：

- `mcp/features/spi` 需要提升哪些 extension-facing contracts
- feature 访问 shared MCP 能力时的 runtime facade 最小集合
- workflow 状态与 snapshot store 的 ownership
- handler SPI 注册与 registry 聚合方式
- `bootstrap` / `distribution` 如何把 feature jars 带入运行时 classpath

---

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Update `mcp/pom.xml` and create `mcp/features/pom.xml` so the MCP reactor includes `spi`, `encrypt`, and `mask` modules under `mcp/features/`.
- [ ] T002 Update `mcp/bootstrap/pom.xml` and `distribution/mcp/pom.xml`, and create `mcp/features/spi/pom.xml`, `mcp/features/encrypt/pom.xml`, and `mcp/features/mask/pom.xml` so feature jars are built and packaged without direct implementation wiring in bootstrap code.
- [ ] T003 [P] Create module source, test, and service-registration skeletons under `mcp/features/spi/src/main/`, `mcp/features/encrypt/src/main/`, `mcp/features/mask/src/main/`, and matching `src/test/` trees.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Create the SPI elevation set in `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/`, including `tool/handler/ToolHandler.java`, `resource/handler/ResourceHandler.java`, `tool/descriptor/MCPToolDescriptor.java`, `tool/descriptor/MCPToolFieldDefinition.java`, `tool/descriptor/MCPToolValueDefinition.java`, and feature-facing response contracts under `protocol/response/`.
- [ ] T005 [P] Create runtime facade contracts in `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/feature/spi/runtime/`, including `MCPFeatureRuntimeContext.java`, `MCPFeatureMetadataFacade.java`, `MCPFeatureExecutionFacade.java`, `MCPFeatureSessionFacade.java`, `MCPFeatureWorkflowStore.java`, and `MCPFeatureCapabilityFacade.java`.
- [ ] T006 Create core-side SPI adapters in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/runtime/`, including `CoreMCPFeatureRuntimeContext.java` and the concrete facade implementations that expose shared metadata, execution, session, capability, and workflow-store services to feature modules.
- [ ] T007 Define workflow state ownership by extracting feature-neutral workflow-store contracts into `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/feature/spi/workflow/` and adapting `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowContextStore.java` to back the SPI facade instead of being called directly by feature code.
- [ ] T008 [P] Update boundary tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerTest.java`, `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerTest.java`, and create `mcp/core/src/test/java/org/apache/shardingsphere/mcp/feature/runtime/CoreMCPFeatureRuntimeContextTest.java` so the new SPI contracts are locked before registry migration starts.

**Checkpoint**: Module graph and SPI contracts are in place; feature modules can now depend only on `mcp/features/spi`.

---

## Phase 3: User Story 1 - Keep core feature-agnostic through direct handler SPI loading (Priority: P1)

**Goal**: Make `mcp/core` discover encrypt and mask only through `ToolHandler` and `ResourceHandler` SPI, while keeping shared MCP infrastructure in core.
**Independent Test**: Start MCP with encrypt and mask feature handlers on the classpath and verify discovery comes from SPI-aggregated surfaces with no hard-coded encrypt/mask dispatch in core.

### Tests for User Story 1

- [ ] T009 [P] [US1] Update `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistryTest.java`, `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java`, `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java`, and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/MCPResourceControllerTest.java` to assert SPI-aggregated discovery and the absence of encrypt/mask-specific branching in core dispatch.
- [ ] T010 [P] [US1] Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`, `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactoryTest.java`, and `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncherTest.java` to verify bootstrap consumes only aggregated surfaces.

### Implementation for User Story 1

- [ ] T011 [US1] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistry.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistry.java` so core aggregates shared handlers with feature handlers through handler SPI only.
- [ ] T012 [US1] Rewrite `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolController.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java`, and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/service/MCPServiceCapabilityProvider.java` so runtime dispatch and capability reporting depend only on aggregated handler surfaces.
- [ ] T013 [US1] Rewrite `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`, `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java`, and `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java` so bootstrap never references encrypt or mask implementation classes directly.
- [ ] T014 [US1] Remove provider-based assembly from `mcp/core/src/main/java/org/apache/shardingsphere/mcp/feature/MCPFeatureProviderRegistry.java` and related uses, or reduce that contract to non-surface metadata only if any residual metadata seam remains necessary after registry simplification.

**Checkpoint**: `mcp/core` is feature-agnostic and runtime discovery depends on handler SPI aggregation rather than hard-coded encrypt/mask classes or provider-enumerated surfaces.

---

## Phase 4: User Story 2 - Let feature modules own and self-register their workflow surface (Priority: P1)

**Goal**: Move encrypt and mask tools, resources, planning, execution-preparation, recommendation, and validation into their own feature modules and register them directly through handler SPI.
**Independent Test**: Review module boundaries and run feature-module tests to confirm encrypt-only or mask-only behavior changes stay within the owning feature module plus stable SPI contracts.

### Tests for User Story 2

- [ ] T015 [P] [US2] Create encrypt feature tests in `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/PlanEncryptRuleToolHandlerTest.java`, `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/ApplyEncryptRuleToolHandlerTest.java`, `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/ValidateEncryptRuleToolHandlerTest.java`, and `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/resource/EncryptResourceHandlerTest.java`.
- [ ] T016 [P] [US2] Create mask feature tests in `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/tool/PlanMaskRuleToolHandlerTest.java`, `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/tool/ApplyMaskRuleToolHandlerTest.java`, `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/tool/ValidateMaskRuleToolHandlerTest.java`, and `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/resource/MaskResourceHandlerTest.java`.

### Implementation for User Story 2

- [ ] T017 [US2] Move encrypt tools and resources into `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/` and `/resource/`, and add `META-INF/services/org.apache.shardingsphere.mcp.tool.handler.ToolHandler` plus `META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler` under `mcp/features/encrypt/src/main/resources/`.
- [ ] T018 [US2] Move encrypt workflow services and models from shared MCP locations into `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/workflow/`, including planning, execution, validation, rule inspection, algorithm recommendation, property template, DistSQL planning, DDL planning, and index planning.
- [ ] T019 [US2] Move mask tools and resources into `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/tool/` and `/resource/`, and add `META-INF/services/org.apache.shardingsphere.mcp.tool.handler.ToolHandler` plus `META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler` under `mcp/features/mask/src/main/resources/`.
- [ ] T020 [US2] Move mask workflow services and models from shared MCP locations into `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/workflow/`, keeping only shared workflow-store/runtime adapters in core.
- [ ] T021 [US2] Delete or replace feature-specific classes under shared MCP locations once feature modules fully own them, and remove any remaining provider-based handler enumeration from feature modules.

**Checkpoint**: Encrypt and mask behavior is owned by their feature modules, and core retains only shared platform infrastructure.

---

## Phase 5: User Story 3 - Add future MCP features by registering handlers, not editing core (Priority: P2)

**Goal**: Make the handler-level SPI model reusable for future MCP feature modules beyond encrypt and mask.
**Independent Test**: Register a hypothetical future feature's handlers in tests and verify they can be discovered without adding any feature-specific branching to core.

### Tests for User Story 3

- [ ] T022 [P] [US3] Extend `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistryTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java` to cover duplicate tool name, duplicate URI pattern, overlapping URI pattern, empty registry, and invalid SPI registration failures.
- [ ] T023 [P] [US3] Add subset-loading coverage in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java` and `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMetadataDiscoveryIT.java` to verify runtime behavior with only encrypt or only mask jars available.

### Implementation for User Story 3

- [ ] T024 [US3] Add deterministic ordering and completeness validation to `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistry.java` and `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistry.java`.
- [ ] T025 [US3] Add onboarding Javadoc and examples to `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandler.java`, `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandler.java`, and related SPI contracts so future feature authors can add surfaces without changing core.

**Checkpoint**: Core is reusable for future MCP feature modules and rejects ambiguous or incomplete registration deterministically.

---

## Phase 6: User Story 4 - Publish clean first-release feature contracts (Priority: P2)

**Goal**: Replace shared pre-release tool names and flat URI layout with explicit encrypt/mask tool families and feature-scoped URI namespaces.
**Independent Test**: Inspect MCP discovery output and verify only encrypt-specific and mask-specific tool families plus `shardingsphere://features/<feature>/...` URI spaces are exposed.

### Tests for User Story 4

- [ ] T026 [P] [US4] Update discovery-surface tests in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`, `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactoryTest.java`, `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistryTest.java`, and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java` to assert the new tool-name families and feature-scoped URIs.
- [ ] T027 [P] [US4] Add legacy-surface absence checks in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandlerRegistryTest.java` and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java` so `plan_encrypt_mask_rule`, `apply_encrypt_mask_rule`, `validate_encrypt_mask_rule`, and old flat resource paths are no longer published.

### Implementation for User Story 4

- [ ] T028 [US4] Rename encrypt external contracts inside `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/` and `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/resource/` to `plan_encrypt_rule`, `apply_encrypt_rule`, `validate_encrypt_rule`, and `shardingsphere://features/encrypt/...`.
- [ ] T029 [US4] Rename mask external contracts inside `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/tool/` and `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/resource/` to `plan_mask_rule`, `apply_mask_rule`, `validate_mask_rule`, and `shardingsphere://features/mask/...`.
- [ ] T030 [US4] Update user-facing and design documentation in `mcp/README.md`, `mcp/README_ZH.md`, `mcp/CODEX_INTEGRATION_ZH.md`, `/.specify/specs/003-mcp-feature-spi/contracts/feature-spi.md`, and `/.specify/specs/003-mcp-feature-spi/quickstart.md` so only the first-release pluginized contracts are documented.

**Checkpoint**: MCP publishes clean encrypt/mask contract families and no longer carries transitional combined naming.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T031 [P] Run module-scoped test commands for `mcp/features/spi`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/core`, and `mcp/bootstrap`, then fix failures in the corresponding `src/test/java` trees.
- [ ] T032 Run scoped style and packaging verification against `mcp/pom.xml`, `mcp/bootstrap/pom.xml`, `distribution/mcp/pom.xml`, and all touched Java sources until Checkstyle, Spotless, and MCP distribution assembly pass.

## Dependencies & Execution Order

- Setup must finish before SPI extraction and registry work begin.
- Foundational work blocks all user stories because the SPI boundary, runtime facades, workflow-store seam, and packaging path must be settled first.
- US1 is the MVP slice and should land before any feature migration.
- US2 depends on US1 because feature modules need the SPI contracts and aggregated surface path to exist first.
- US3 can start after US1 for registry validation, but subset-loading checks complete only after US2 publishes real encrypt and mask modules.
- US4 depends on US2 because clean first-release names and URIs should be published from the final feature-owned surfaces.
- Polish runs last after the desired user stories are complete.

## Parallel Execution Examples

- After T004 completes, T005 and T008 can run in parallel because runtime facade contracts and boundary tests touch different files.
- After T011 completes, T009 and T010 can run in parallel with T012/T013 because tests and registry/bootstrap rewrites are split across different files.
- In US2, T015 can run in parallel with T016, and T017 can run in parallel with T019 because encrypt and mask modules have disjoint write scopes.
- In US3, T022 and T023 can run in parallel because core registry failure tests and bootstrap subset-loading tests touch different modules.
- In US4, T028 and T029 can run in parallel because encrypt and mask contract renames live in different feature modules.

## Implementation Strategy

- MVP first: complete Phase 1, Phase 2, and US1 so `mcp/core` becomes feature-agnostic before any business logic migration.
- Increment 2: complete US2 so encrypt and mask truly own their workflow surfaces in separate modules and self-register them through handler SPI.
- Increment 3: complete US4 to publish the final first-release tool names and URI namespaces.
- Hardening: complete US3 and Phase 7 to lock deterministic loading, fail-fast validation, subset loading, and packaging quality.
