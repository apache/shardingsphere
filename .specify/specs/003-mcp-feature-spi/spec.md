# Feature Specification: MCP Feature SPI Simplification

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-29
**Status**: Draft
**Input**: User description: "Deeply analyze `shardingsphere-mcp-feature-spi`. The module feels too heavy. It should be kept as clean as possible and ideally contain only SPI interfaces. Use speckit to sort out the requirement without switching branches."

## Clarifications

### Session 2026-04-29

- This requirement must be analyzed and documented on the current branch; no new branch may be created or checked out.
- The previous 003 direction that allowed `mcp/features/spi` to hold large shared contract and workflow runtime surfaces is superseded by this requirement.
- The target state for `mcp/features/spi` is a pure extension boundary: stable SPI interfaces plus only the minimal enums, annotations, and signature-owned constants that cannot be cleanly separated.
- Concrete runtime services, workflow orchestration helpers, request binders, payload builders, stores, URI parsers, registries, and default handler implementations are not considered SPI and must not remain in `mcp/features/spi`.
- Classes used only by shared MCP runtime belong in `mcp/core`.
- Non-SPI classes that are used by both `mcp/core` and feature modules must move to the dedicated shared API module `mcp/api` rather than forcing feature modules to depend on `mcp/core` internals.
- A new `mcp/features/support` module is not accepted for this requirement. Reusable concrete helpers must be decomposed or relocated until their final concrete implementations belong either to `mcp/core` or to an owning feature module.
- `MCPFeatureProvider` remains in `mcp/features/spi` as a retained top-level SPI entry for this requirement.
- This requirement is about module purity and dependency boundaries. Redesigning external tool names, resource URIs, or workflow semantics is out of scope unless another specification explicitly changes them.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Make the SPI boundary obvious (Priority: P1)

As an MCP maintainer, I want `mcp/features/spi` to contain only true SPI contracts so that anyone reading the module can immediately see the stable extension boundary without sorting through runtime helpers and shared implementation code.

**Why this priority**: This is the core intent of the requirement. If the module still carries concrete helpers and runtime models, the refactor does not solve the architectural problem.

**Independent Test**: Inspect the production sources under `mcp/features/spi` and verify that they contain only stable SPI interfaces and minimal interface-owned supporting enums or annotations, with no concrete runtime services, stores, binders, payload builders, or registry logic.

**Acceptance Scenarios**:

1. **Given** a reviewer opens `mcp/features/spi`, **When** they inspect its production classes, **Then** they find only SPI-facing interfaces and minimal interface-owned supporting types.
2. **Given** a concrete class under `mcp/features/spi` performs workflow orchestration, request binding, payload construction, state storage, URI parsing, or registry assembly, **When** the simplification is complete, **Then** that class has been moved out of `mcp/features/spi`.
3. **Given** a future contributor needs the MCP extension boundary, **When** they inspect `mcp/features/spi`, **Then** they can identify it without tracing through unrelated runtime implementation code.

---

### User Story 2 - Place shared classes without creating circular dependencies (Priority: P1)

As an MCP architect, I want every non-SPI class currently living in `mcp/features/spi` to have a clear destination so that we can slim the module without introducing feature-to-core reverse dependencies or new circular coupling.

**Why this priority**: Moving code out of `mcp/features/spi` is only valuable if the resulting dependency graph is cleaner rather than more tangled.

**Independent Test**: For every current class family under `mcp/features/spi`, verify that it is explicitly classified into a final destination of: pure SPI contract, core-private runtime code, shared API contract, or feature-owned implementation, with any mixed concrete helper family decomposed before migration.

**Acceptance Scenarios**:

1. **Given** a class is only used by shared runtime infrastructure, **When** it is reclassified, **Then** it moves to `mcp/core` rather than staying in `mcp/features/spi`.
2. **Given** a non-SPI type is compiled against by both `mcp/core` and feature modules, **When** it is reclassified, **Then** it moves to `mcp/api` rather than forcing features to depend on core internals.
3. **Given** a concrete helper is reused by multiple feature modules, **When** it is reclassified, **Then** it is decomposed so that no new `mcp/features/support` module is needed and each resulting concrete implementation lands in `mcp/core` or an owning feature module.

---

### User Story 3 - Keep feature modules extensible without relying on core internals (Priority: P2)

As an encrypt or mask feature maintainer, I want feature modules to build against pure SPI and shared contracts only so that feature evolution stays independent from core implementation details.

**Why this priority**: A slim SPI module should strengthen extension boundaries, not simply relocate code while leaving feature modules tied to core internals.

**Independent Test**: Verify that encrypt and mask compile against `mcp/features/spi` and `mcp/api`, while avoiding imports from core-private implementation packages.

**Acceptance Scenarios**:

1. **Given** a feature module needs to implement MCP handlers, **When** it compiles, **Then** it depends on pure SPI contracts and shared API contracts rather than core-private implementations.
2. **Given** shared runtime implementations remain in `mcp/core`, **When** features consume shared capabilities, **Then** they do so through interface-only seams rather than direct core implementation classes.
3. **Given** a future feature is added, **When** its author follows the documented boundaries, **Then** they can extend MCP without putting new concrete helper code back into `mcp/features/spi`.

---

### Edge Cases

- What happens to a type that is not an SPI interface but is referenced by both `mcp/core` and feature modules?
- What happens if a concrete helper is reused by both encrypt and mask but would create a feature-to-core dependency if moved into `mcp/core`?
- How does the design handle classes that mix interface definition and concrete default behavior in the same type?
- What happens if a type currently under `mcp/features/spi` appears shared but is actually only used by one feature module?
- How do we prevent future additions from placing new concrete helper code back into `mcp/features/spi` after the simplification is complete?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST treat `mcp/features/spi` as a pure SPI boundary whose production code contains only stable extension interfaces and minimal interface-owned supporting enums, annotations, or constants.
- **FR-002**: The system MUST NOT keep concrete runtime services, workflow orchestration helpers, request binders, payload builders, stores, registries, URI parsers, or default concrete handlers in `mcp/features/spi`.
- **FR-003**: The system MUST classify every current production type under `mcp/features/spi` into exactly one destination category before code migration starts.
- **FR-004**: Classes used only by shared MCP runtime infrastructure MUST move to `mcp/core`.
- **FR-005**: Non-SPI contracts, DTOs, descriptors, requests, responses, metadata models, protocol models, or exceptions that are compiled against by both `mcp/core` and feature modules MUST move to the dedicated shared API module `mcp/api` rather than remain in `mcp/features/spi`.
- **FR-006**: Reusable concrete helpers that are shared by multiple feature modules and are not core-private MUST NOT move to a new `mcp/features/support` module; they MUST be decomposed or relocated so that final concrete implementations live in `mcp/core` or owning feature modules, with only shared contracts remaining in `mcp/api` or `mcp/features/spi`.
- **FR-007**: Feature modules MUST NOT depend on core-private implementation packages as the price of slimming `mcp/features/spi`.
- **FR-008**: If moving a non-SPI class directly into `mcp/core` would force feature modules to depend on core internals, the design MUST use `mcp/api` and/or refactor ownership instead of relaxing SPI purity or introducing `mcp/features/support`.
- **FR-009**: Interface-only seams that expose shared runtime capabilities to features MAY remain in `mcp/features/spi`, but their implementations MUST reside outside `mcp/features/spi`.
- **FR-010**: The simplification MUST preserve handler discovery and runtime feature loading behavior after the class migrations are complete.
- **FR-011**: The simplification MUST preserve existing encrypt and mask business behavior unless another specification explicitly changes external MCP semantics.
- **FR-012**: External MCP tool names and resource URI layouts MUST remain out of scope for this requirement and MUST NOT be redesigned as part of satisfying SPI purity.
- **FR-013**: The resulting module graph MUST make it obvious whether a type belongs to SPI, shared API, core runtime, or a specific feature module.
- **FR-014**: Workflow state storage implementations, generic workflow execution helpers, and generic workflow validation helpers MUST NOT remain in `mcp/features/spi` once the simplification is complete.
- **FR-015**: Registry-only utilities and parsing helpers that are not part of the stable extension surface MUST NOT remain in `mcp/features/spi`.
- **FR-016**: Future additions to MCP MUST follow the same classification rules and MUST NOT add new concrete helper code to `mcp/features/spi`.
- **FR-017**: The refactor MUST remain compatible with module-scoped build and test execution for `mcp/core`, `mcp/features/spi`, `mcp/features/encrypt`, and `mcp/features/mask`.
- **FR-018**: The requirement analysis and resulting specification updates MUST be completed on the current branch without branch switching.
- **FR-019**: SPI method signatures in `mcp/features/spi` MUST NOT expose concrete support implementations or core-private classes; any such signature MUST be replaced by an interface-only seam or a neutral shared contract.
- **FR-020**: `MCPFeatureProvider` MUST remain in `mcp/features/spi` as a stable SPI entry for feature registration unless another specification explicitly replaces it.

### Key Entities *(include if feature involves data)*

- **Pure SPI Contract**: An interface-level contract that defines how feature modules extend MCP without embedding runtime implementation.
- **Shared API Contract**: A non-SPI type such as a descriptor, request, response, metadata model, protocol model, or exception that is compiled against by both core and feature modules.
- **Shared Concrete Family**: A current concrete helper family used by multiple feature modules that must be decomposed until its resulting concrete implementations belong to core runtime or feature-owned modules.
- **Core Runtime Component**: A concrete implementation used only by shared MCP runtime infrastructure.
- **Feature-Owned Component**: A concrete implementation that belongs only to one feature module, such as encrypt or mask.
- **Type Classification Record**: The documented mapping that assigns each current `mcp/features/spi` type family to its final module and ownership category.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After the simplification, production code under `mcp/features/spi` contains zero concrete runtime service, workflow helper, store, binder, payload builder, registry, or parser classes.
- **SC-002**: Every current type family under `mcp/features/spi` has a documented destination category with no unresolved ownership gaps.
- **SC-003**: The resulting dependency graph does not require encrypt or mask to import core-private implementation packages in order to compile.
- **SC-004**: Core runtime still discovers and dispatches feature handlers successfully after the class migrations are complete.
- **SC-005**: No new `mcp/features/support` module is introduced as part of this simplification.
- **SC-006**: The scope of this simplification remains bounded to module purity and dependency boundaries, with no accidental expansion into external contract redesign.

## Assumptions

- Introducing `mcp/api` is acceptable and expected because it is the cleanest way to keep `mcp/features/spi` pure and avoid circular dependencies.
- Introducing `mcp/features/support` is not acceptable for this requirement.
- Existing handler-level extension behavior is still the correct architectural direction; the problem to solve is the thickness of the SPI module, not the existence of feature modularization itself.
- `MCPFeatureProvider` remains part of the accepted SPI surface for this requirement.
- This specification supersedes earlier 003 assumptions that allowed `mcp/features/spi` to carry large shared contract and workflow runtime surfaces.
- No branch changes are allowed while producing this requirement analysis.
