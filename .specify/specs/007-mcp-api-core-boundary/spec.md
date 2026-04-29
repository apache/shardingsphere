# Feature Specification: MCP API Boundary Slimming

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-30
**Status**: Draft
**Input**: User description: "Analyze `shardingsphere-mcp-api` more carefully, identify what should sink to `mcp/core`, and use speckit to sort out the requirement without switching branches."

## Clarifications

### Session 2026-04-30

- This requirement must be analyzed and documented on the current branch; no new branch may be created, checked out, or switched to.
- This feature is a follow-up to `003-mcp-feature-spi` and focuses on the boundary between `mcp/api` and `mcp/core` rather than reopening the SPI-purity decision for `mcp/features/spi`.
- The target state for `mcp/api` is a minimal neutral shared-contract module: only shared contracts, DTOs, and protocol models that are meaningfully compiled against by multiple MCP layers may remain there.
- Concrete runtime behavior is not shared API. Workflow orchestration helpers, execution and validation handlers, request binders, payload builders, runtime stores, runtime registries, runtime parsers, and other runtime-only helpers are candidates to leave `mcp/api`.
- Types used only by shared MCP runtime infrastructure belong in `mcp/core`.
- If a current type family mixes neutral contract surface and concrete runtime behavior, the requirement is to decompose it so that only the neutral shared contract remains in `mcp/api` and runtime ownership moves to `mcp/core`.
- Workflow model families should be split aggressively: if a current workflow model carries core planning, execution, diagnostics, or lifecycle state, that core state should sink to `mcp/core` and only minimal shared contract data may remain in `mcp/api`.
- Shrinking `mcp/api` must not force feature modules to depend on core-private implementation packages.
- Encrypt and mask feature modules must continue to depend only on `mcp/api` for shared MCP contracts and must not depend on `mcp/core` directly.
- This requirement should be solved within the existing `mcp/api` and `mcp/core` boundary. Introducing a new shared support or workflow module is out of scope for this requirement analysis.
- This feature is about module purity and dependency ownership. It does not redesign external MCP tool names, resource URIs, protocol semantics, or encrypt and mask workflow semantics.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Make the shared API boundary obvious (Priority: P1)

As an MCP maintainer, I want `mcp/api` to contain only shared contracts, DTOs, and protocol models so that anyone reading the module can immediately distinguish shared API from shared runtime implementation.

**Why this priority**: This is the core architectural intent. If runtime-only helpers still remain in `mcp/api`, the module is still too heavy and its ownership is still unclear.

**Independent Test**: Inspect production sources under `mcp/api` and verify that retained types are limited to neutral shared contracts, DTOs, and protocol models that are genuinely compiled against by multiple MCP layers, while runtime-only implementations and core-owned state holders are absent.

**Acceptance Scenarios**:

1. **Given** a reviewer opens `mcp/api`, **When** they inspect its production classes, **Then** they find neutral shared contracts rather than runtime-only workflow framework implementations.
2. **Given** a class under `mcp/api` performs workflow orchestration, request binding, artifact payload shaping, runtime storage, registry assembly, runtime URI parsing, or other runtime-only behavior, **When** the cleanup is complete, **Then** that class no longer lives in `mcp/api`.
3. **Given** a type under `mcp/api` is only used by shared runtime infrastructure, **When** the boundary cleanup is complete, **Then** its ownership has moved to `mcp/core`.

---

### User Story 2 - Give every API type family a deterministic destination (Priority: P1)

As an MCP architect, I want every current `mcp/api` type family to have a clear retain, move, or decompose rule so that slimming the module does not become ad hoc or inconsistent.

**Why this priority**: The module only becomes maintainable if every current family has a documented destination rule instead of case-by-case intuition.

**Independent Test**: For every production type family currently under `mcp/api`, verify that it is explicitly classified into one final outcome: retained shared API contract, moved to core runtime, or decomposed into shared API contract plus core runtime implementation.

**Acceptance Scenarios**:

1. **Given** a type family is compiled against by both `mcp/core` and at least one non-core MCP layer, **When** it is reclassified, **Then** it remains in or moves to `mcp/api` as a neutral shared contract.
2. **Given** a type family is compiled against only by shared runtime infrastructure, **When** it is reclassified, **Then** it moves to `mcp/core`.
3. **Given** a type family currently combines contract surface with runtime behavior, **When** it is reclassified, **Then** it is decomposed until only neutral contract remains shared and runtime ownership is clearly core-private.

---

### User Story 3 - Preserve feature extension boundaries while slimming `mcp/api` (Priority: P2)

As an encrypt or mask feature maintainer, I want feature modules to keep depending on neutral shared API only so that slimming `mcp/api` does not push me onto core-private implementation classes.

**Why this priority**: The cleanup is only useful if it improves architecture without creating a new feature-to-core coupling problem.

**Independent Test**: Verify that feature modules can continue to compile and participate in shared MCP behavior without importing core-private implementation packages or constructing core-private runtime helpers directly.

**Acceptance Scenarios**:

1. **Given** a feature module needs shared MCP contract, DTO, or protocol model types, **When** the cleanup is complete, **Then** those neutral shared types are still available from `mcp/api` without importing `mcp/core`.
2. **Given** a generic workflow behavior moves out of `mcp/api`, **When** a feature participates in that behavior, **Then** it does so through neutral shared contracts rather than direct dependence on core-private implementation packages.
3. **Given** a future feature is added, **When** its author follows the documented boundary rules, **Then** they can extend MCP without placing new concrete runtime framework code back into `mcp/api`.

---

### Edge Cases

- What happens to a current `mcp/api` interface that also exposes a static factory for an in-memory runtime implementation?
- What happens to a generic workflow handler that is reused by multiple features today but currently lives in `mcp/api` as a concrete implementation?
- What happens if a workflow model is shared across modules but also carries core-only planning, execution, or diagnostics state?
- What happens to a runtime descriptor that is physically located under `mcp/api` but is only consumed by `mcp/core`?
- What happens if keeping a helper in `mcp/api` would be convenient but would blur the shared-contract and runtime-implementation boundary again?
- What happens if someone proposes a new shared support or workflow module instead of clarifying ownership between `mcp/api` and `mcp/core`?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST treat `mcp/api` as a minimal neutral shared-contract module rather than a container for shared runtime implementation.
- **FR-002**: Production code under `mcp/api` MUST NOT keep concrete runtime services, workflow orchestration helpers, request binders, payload builders, runtime stores, runtime registries, runtime parsers, or default runtime handler implementations.
- **FR-003**: Every current production type family under `mcp/api` MUST be classified into exactly one destination category before code migration starts: retained shared API contract, moved to core runtime, or decomposed into shared API contract plus core runtime implementation.
- **FR-004**: Type families used only by shared MCP runtime infrastructure MUST move to `mcp/core`.
- **FR-005**: Only neutral shared contracts, DTOs, and protocol models that are compiled against by both `mcp/core` and at least one non-core MCP layer MUST remain in or move to `mcp/api` rather than `mcp/core`.
- **FR-006**: If a current `mcp/api` type family mixes shared contract surface with runtime behavior, the design MUST decompose it so that only the neutral contract remains shared and runtime ownership moves to `mcp/core`.
- **FR-007**: Feature-facing context and handler signatures MUST expose only neutral shared API contracts and MUST NOT expose core-private implementation classes.
- **FR-008**: Shared interfaces under `mcp/api` MUST NOT provide static factories, default constructors, or other entry points that directly instantiate core-owned runtime implementations.
- **FR-009**: Workflow state storage implementations MUST NOT remain in `mcp/api` once the boundary cleanup is complete.
- **FR-010**: Generic workflow execution, workflow validation, workflow request-binding, artifact-payload shaping, and runtime-only helper behavior MUST NOT remain in `mcp/api` if that behavior is not itself a neutral shared contract.
- **FR-011**: Core-only runtime descriptors, loaders, query helpers, and runtime coordination helpers MUST NOT remain in `mcp/api`.
- **FR-012**: Shrinking `mcp/api` MUST NOT force encrypt or mask feature modules to import core-private implementation packages or construct core-private helpers directly.
- **FR-013**: The cleanup MUST preserve existing external MCP tool names, resource URI layouts, protocol behavior, and encrypt and mask business semantics unless another specification explicitly changes them.
- **FR-014**: The cleanup MUST preserve handler discovery and runtime feature loading behavior after type migrations are complete.
- **FR-015**: This requirement MUST be solved within the existing `mcp/api` and `mcp/core` boundary and MUST NOT introduce a new shared support or workflow module.
- **FR-016**: The resulting module graph MUST make it obvious whether a type is a shared API contract or a core runtime implementation.
- **FR-017**: Requirement analysis and resulting specification updates MUST be completed on the current branch without branch switching.
- **FR-018**: The resulting design MUST remain compatible with module-scoped build and test execution for `mcp/api`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, and `mcp/features/mask`.
- **FR-019**: If a shared workflow model currently carries both neutral contract data and core-only planning, execution, or diagnostic state, the model MUST be reduced or split until the shared API surface is limited to contract-relevant data.
- **FR-020**: If a workflow model family can be split, the design MUST prefer sinking core planning, execution, diagnostics, lifecycle, and mutable runtime state into `mcp/core` instead of retaining that state in `mcp/api`.
- **FR-021**: Encrypt and mask feature modules MUST continue to depend only on `mcp/api` for shared MCP contracts and MUST NOT add a direct dependency on `mcp/core` as part of this cleanup.

### Key Entities *(include if feature involves data)*

- **Shared API Contract**: A neutral shared contract, DTO, or protocol model that is meaningfully compiled against by multiple MCP layers and does not own runtime-only behavior.
- **Core Runtime Implementation**: A concrete type owned by `mcp/core` because it exists only to support shared MCP runtime behavior.
- **Mixed Contract/Runtime Family**: A current type family that combines shared contract surface with concrete runtime behavior and therefore must be decomposed.
- **Core-Only Runtime Descriptor**: A data or helper type physically located under `mcp/api` today but actually consumed only by shared runtime infrastructure.
- **Feature-Facing Workflow Contract**: A minimal shared workflow-facing contract or DTO that features may compile against, provided it remains neutral and does not expose core-private implementation ownership or carry core runtime state.
- **Type Classification Record**: The documented retain, move, or decompose outcome for each current `mcp/api` type family.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After the cleanup, production code under `mcp/api` contains zero concrete runtime service, runtime store implementation, runtime registry, runtime parser, runtime payload builder, or default runtime handler implementation classes.
- **SC-002**: Every current `mcp/api` production type family has a documented final ownership outcome with no unresolved destination gaps.
- **SC-003**: Encrypt and mask feature modules do not need to import core-private implementation packages in order to compile after `mcp/api` is slimmed.
- **SC-004**: Runtime-only workflow execution, validation, storage, and coordination behavior is owned by `mcp/core` rather than `mcp/api` after the cleanup is complete.
- **SC-005**: No new shared support or workflow module is introduced as part of this requirement.
- **SC-006**: This requirement analysis is completed on the existing branch only, with no branch creation or branch switching.
- **SC-007**: Workflow model families that remain in `mcp/api` expose only minimal shared contract data and no longer retain core planning, execution, diagnostics, or lifecycle state.

## Assumptions

- This feature is a boundary-cleanup follow-up to `003-mcp-feature-spi`, not a redesign of the SPI module itself.
- `mcp/api` should remain as small as possible and keep only shared contracts, DTOs, and protocol models that are genuinely shared across MCP layers.
- Moving runtime-only workflow framework code into `mcp/core` is acceptable when that code is not part of a neutral shared API contract.
- When a workflow model can be split, core planning, execution, diagnostics, lifecycle, and mutable runtime state should sink to `mcp/core`.
- Introducing a new shared support or workflow module is out of scope for this requirement.
- Encrypt and mask should continue to depend only on `mcp/api` for shared MCP contracts.
- No branch changes are allowed while producing this requirement analysis.
