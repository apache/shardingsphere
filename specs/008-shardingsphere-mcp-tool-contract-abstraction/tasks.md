# Tasks: ShardingSphere MCP Tool Contract Abstraction Between Core and Bootstrap

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: Add core descriptor coverage tests, bootstrap schema adapter tests, and one public `tools/list`
compatibility regression.

**Organization**: Tasks are grouped by user story so contract centralization, SDK adapter cleanup,
and public compatibility remain independently reviewable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Spec and Boundary Freeze)

**Purpose**: Freeze the contract-centralization target before code changes.

- [X] T001 Add contract-centralization scope and compatibility rules to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/spec.md`
- [X] T002 [P] Record core-vs-bootstrap boundary decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/research.md`
- [X] T003 [P] Capture descriptor and input-definition objects in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/data-model.md`
- [X] T004 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/plan.md`

---

## Phase 2: Foundational (Core Descriptor Model)

**Purpose**: Introduce the minimum core model needed to centralize tool contracts.

**CRITICAL**: No bootstrap adapter cleanup should start before the core descriptor model exists.

- [X] T005 Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolDescriptor.java`
- [X] T006 [P] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolDispatchKind.java`
- [X] T007 [P] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolInputDefinition.java`
- [X] T008 [P] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolFieldDefinition.java`
- [X] T009 [P] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolValueDefinition.java`

**Checkpoint**: Core now has a repository-owned model for tool static contracts.

---

## Phase 3: User Story 1 - tool 静态契约在 core 中只有一份事实来源 (Priority: P1)

**Goal**: Make `MCPToolCatalog` the single source of truth for supported tools and their static contracts.

**Independent Test**: Core tests prove every supported tool has an explicit descriptor,
including zero-argument and optional-argument tools.

### Tests for User Story 1

- [X] T010 [P] [US1] Extend descriptor coverage assertions in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalogTest.java`

### Implementation for User Story 1

- [X] T011 [US1] Refactor
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalog.java`
  to own explicit descriptors for all supported tools
- [X] T012 [P] [US1] Make `list_databases()` and `get_capabilities(database?)`
  explicit in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalog.java`
- [X] T013 [P] [US1] Keep metadata and execution request normalization traceable in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalog.java`

**Checkpoint**: Core owns supported tool names, titles, descriptions, dispatch kinds, input definitions,
and existing request normalization.

---

## Phase 4: User Story 2 - bootstrap 只做 SDK 适配 (Priority: P1)

**Goal**: Remove tool-name-specific schema business logic from bootstrap and replace it with a pure adapter.

**Independent Test**: Bootstrap unit tests validate descriptor-to-SDK-schema mapping without any `toolName` schema branching.

### Tests for User Story 2

- [X] T014 [P] [US2] Add
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolInputSchemaFactoryTest.java`

### Implementation for User Story 2

- [X] T015 [US2] Refactor
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolInputSchemaFactory.java`
  into a pure descriptor-to-SDK adapter
- [X] T016 [P] [US2] Refactor
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`
  to consume core descriptors directly
- [X] T017 [P] [US2] Ensure bootstrap does not reintroduce tool-specific schema rules
  outside
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`

**Checkpoint**: Bootstrap becomes a transport adapter layer for tool schema/spec creation.

---

## Phase 5: User Story 3 - public tool contract 与 runtime 行为保持零损失 (Priority: P1)

**Goal**: Prove that the layering change does not alter `tools/list` or existing runtime dispatch behavior.

**Independent Test**: Public tool listing and tool calls still behave as before while reflecting the explicit core descriptors.

### Tests for User Story 3

- [X] T018 [P] [US3] Reconfirm public tool-list compatibility in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java`
- [X] T019 [P] [US3] Reconfirm `get_capabilities` and metadata call behavior in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionMetadataDiscoveryIntegrationTest.java`

### Implementation for User Story 3

- [X] T020 [US3] Keep handler/session binding unchanged in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolCallHandler.java`
- [X] T021 [P] [US3] Ensure `tools/list` reflects explicit descriptors without
  public surface drift in
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`

**Checkpoint**: Static contract ownership changed internally, but public listing and runtime behavior did not regress.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency and quality closure.

- [X] T022 [P] Run scoped core and bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [X] T023 [P] Run scoped core and bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [X] T024 [P] Align final implementation notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed with this design round.
- **Foundational (Phase 2)**: Blocks all code changes because bootstrap needs the core descriptor model first.
- **User Stories (Phase 3-5)**: Depend on the core model from Phase 2.
- **Polish (Phase 6)**: Runs after the selected implementation slice is complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice because core descriptor ownership is the basis for everything else.
- **User Story 2 (P1)**: Depends on US1 because bootstrap adapter can only be simplified after core descriptors exist.
- **User Story 3 (P1)**: Depends on US1/US2 so compatibility checks match the final layering.

### Parallel Opportunities

- `T006`, `T007`, `T008`, and `T009` can run in parallel once the descriptor shape is fixed.
- `T012` and `T013` can run in parallel while refactoring `MCPToolCatalog`.
- `T014`, `T016`, and `T017` can run in parallel after the core descriptor API stabilizes.
- `T018` and `T019` can run in parallel as public compatibility verification slices.
- `T022` and `T023` can run in parallel as final verification commands.

## Implementation Strategy

### MVP First

1. Add the minimum core descriptor model.
2. Move all supported tool static definitions into `MCPToolCatalog`.
3. Convert bootstrap schema generation into a pure adapter.
4. Re-run public listing and targeted runtime regressions.

### Incremental Delivery

1. Centralize static tool contract ownership in core.
2. Remove bootstrap tool-name-specific schema business logic.
3. Keep runtime dispatch/session logic unchanged.
4. Verify public listing and call behavior remain stable.

## Notes

- This feature is a layering cleanup, not a new MCP protocol revision.
- If the descriptor model already expresses a future tool, bootstrap should not grow new schema branches.
- If later work wants to abstract call handling, it should be a separate follow-up after this contract-centralization pass.
