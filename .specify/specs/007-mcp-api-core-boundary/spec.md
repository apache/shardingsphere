# Feature Specification: MCP Workflow Shared Module Extraction

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-05-01
**Status**: Draft
**Input**: User description: "Use speckit to sort out the requirement for the MCP api/core boundary, keep working on the current branch, and use `shardingsphere-mcp-workflow` as the new shared workflow module."

## Clarifications

### Session 2026-05-01

- This requirement analysis must stay on the current branch; no new branch may be created, checked out, or switched to.
- The target design is no longer limited to only `mcp/api` and `mcp/core`; a new shared module named `shardingsphere-mcp-workflow` is explicitly allowed.
- The new module name must be `shardingsphere-mcp-workflow`; alternative names such as `workflow-support` are rejected.
- `encrypt` and `mask` feature modules may depend on `mcp/api` and `mcp/workflow`, but they must not depend on `mcp/core` directly.
- `mcp/workflow` must stay workflow-specific and must not become a generic dumping ground for unrelated MCP helpers.
- This iteration should prefer minimal API redraw. Shared workflow model and bridge contracts may remain in `mcp/api` if moving them would require a broader SPI redesign.
- If a workflow helper currently exposes a generic non-workflow helper to features, the seam should be redesigned instead of moving that generic helper wholesale into `mcp/workflow`.
- External MCP tool names, resource URIs, protocol semantics, feature loading behavior, and encrypt/mask business semantics are not being redesigned here.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Introduce a clear shared workflow layer (Priority: P1)

As an MCP maintainer, I want workflow-specific shared code to live in one dedicated module so that `mcp/api` stays focused on contracts and `mcp/core` stays focused on runtime ownership.

**Why this priority**: The current pain comes from workflow shared logic being split across `mcp/api` and `mcp/core`, which makes module ownership hard to read and easy to regress.

**Independent Test**: Inspect the module graph and shared workflow classes after the refactor and verify that workflow-shared helpers are owned by `mcp/workflow`, not by `mcp/api` or by core-private runtime packages.

**Acceptance Scenarios**:

1. **Given** a class provides workflow-specific shared binding, planning, validation, payload, masking, lifecycle, or rule utility behavior, **When** the refactor is complete, **Then** that class lives in `mcp/workflow`.
2. **Given** a reviewer opens `mcp/api`, **When** they inspect its retained production types, **Then** they find foundational shared contracts and workflow bridge/model contracts rather than shared workflow helper implementations.
3. **Given** a reviewer opens `mcp/core`, **When** they inspect its retained production types, **Then** they find runtime registries, runtime handlers, runtime execution services, and runtime stores rather than cross-feature shared workflow helper code.

---

### User Story 2 - Let feature modules reuse workflow behavior without depending on core (Priority: P1)

As an encrypt or mask feature maintainer, I want to compile against shared workflow behavior without importing `mcp/core` so that feature modules remain extensions rather than runtime-owned implementations.

**Why this priority**: If features still need `mcp/core`, the new module does not actually solve the current ownership problem.

**Independent Test**: Verify that `mcp/features/encrypt` and `mcp/features/mask` compile using only `mcp/api` and `mcp/workflow` from the MCP shared layers, with no direct dependency on `mcp/core`.

**Acceptance Scenarios**:

1. **Given** a feature planning handler needs workflow request binding and workflow descriptor helpers, **When** the refactor is complete, **Then** it obtains them from `mcp/workflow` rather than `mcp/core`.
2. **Given** a feature planning or validation service needs shared workflow planning, validation, SQL, lifecycle, or payload support, **When** the refactor is complete, **Then** it obtains that support from `mcp/workflow` rather than `mcp/core`.
3. **Given** a feature module participates in workflow apply or validate flows, **When** the refactor is complete, **Then** it does so through shared workflow contracts and workflow contribution seams rather than direct imports of core runtime handlers.

---

### User Story 3 - Keep the new workflow module narrowly focused (Priority: P2)

As an MCP architect, I want the new shared workflow module to contain only workflow-specific shared code so that it does not become a renamed general-purpose support bucket.

**Why this priority**: Adding a new module only helps if its scope stays disciplined; otherwise the architecture just moves the blur to another place.

**Independent Test**: Inspect the contents of `mcp/workflow` and verify that non-workflow helpers used by generic execute or search-metadata handlers are not moved there.

**Acceptance Scenarios**:

1. **Given** a helper is used by non-workflow handlers such as generic SQL execution or metadata search, **When** the refactor is complete, **Then** it remains outside `mcp/workflow` unless it is first reduced to a workflow-specific abstraction.
2. **Given** a workflow helper currently exposes a generic argument parser type to feature code, **When** the refactor is complete, **Then** the feature-facing seam is workflow-scoped rather than generic.
3. **Given** a proposed migration would place unrelated generic support logic into `mcp/workflow`, **When** the design is reviewed, **Then** that migration is rejected as out of boundary.

---

### Edge Cases

- What happens if a current workflow helper depends on a generic helper that is also used by non-workflow handlers?
- What happens if a workflow SPI type is currently in `mcp/api` but is only meaningful when the workflow layer exists?
- What happens if a workflow model type is used by multiple layers but also carries obvious runtime aggregation state?
- What happens if a runtime-only handler in `mcp/core` currently imports a helper that should become workflow-shared?
- What happens if keeping a generic helper outside `mcp/workflow` requires introducing a smaller workflow-scoped adapter or accessor?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST introduce a new shared module named `shardingsphere-mcp-workflow`.
- **FR-002**: `shardingsphere-mcp-workflow` MUST depend on `shardingsphere-mcp-api` and MUST NOT depend on `shardingsphere-mcp-core`.
- **FR-003**: `shardingsphere-mcp-core` MUST depend on `shardingsphere-mcp-api` and `shardingsphere-mcp-workflow` for workflow-shared behavior.
- **FR-004**: `shardingsphere-mcp-feature-encrypt` and `shardingsphere-mcp-feature-mask` MUST depend on `shardingsphere-mcp-api` and `shardingsphere-mcp-workflow`, and MUST NOT declare a direct dependency on `shardingsphere-mcp-core`.
- **FR-005**: `shardingsphere-mcp-api` MUST remain the foundational shared-contract module for MCP handler contracts, base SPI contracts, protocol DTOs, metadata DTOs, and workflow bridge/model contracts that are still shared across multiple layers.
- **FR-006**: Workflow-specific shared helpers currently split between `mcp/api` and `mcp/core` MUST move into `shardingsphere-mcp-workflow`.
- **FR-007**: `shardingsphere-mcp-workflow` MUST own workflow-specific shared request binding, planning support, validation support, artifact payload shaping, masking, lifecycle, rule extraction, SQL utility, and workflow descriptor helper behavior.
- **FR-008**: `shardingsphere-mcp-workflow` MUST own workflow-specific SPI contribution types and callbacks that are shared between feature modules and core runtime materialization.
- **FR-009**: `shardingsphere-mcp-core` MUST remain the owner of runtime-only workflow execution handlers, runtime validation handlers, runtime registries, runtime materializers, runtime execution services, runtime query services, and workflow session-store implementations.
- **FR-010**: The refactor MUST preserve existing MCP tool names, resource URI layouts, protocol behavior, workflow apply and validate behavior, and encrypt/mask business semantics.
- **FR-011**: `shardingsphere-mcp-workflow` MUST remain workflow-specific and MUST NOT become a generic support module for unrelated MCP helpers.
- **FR-012**: Generic helpers used by non-workflow handlers, including generic SQL execution and metadata search helpers, MUST remain outside `shardingsphere-mcp-workflow` unless they are first reduced to a workflow-specific abstraction.
- **FR-013**: If a workflow-facing helper currently exposes a generic non-workflow helper type such as `MCPToolArguments` to feature code, the design MUST replace that seam with a workflow-scoped accessor, binder contract, or equivalent workflow-specific abstraction instead of moving the generic helper wholesale into `shardingsphere-mcp-workflow`.
- **FR-014**: This iteration MUST prefer minimal API redraw and MUST NOT require a broad redesign of `MCPFeatureContext`, `WorkflowSessionContext`, `WorkflowPropertySource`, or shared workflow model contracts if feature-to-core decoupling can be achieved without that redesign.
- **FR-015**: Feature-facing context and handler signatures MUST continue to expose only API-owned contracts and MUST NOT expose core-owned implementation classes.
- **FR-016**: The resulting module graph MUST make the workflow ownership chain explicit: `mcp/api <- mcp/workflow <- mcp/core` and `mcp/api <- mcp/workflow <- mcp/features/encrypt|mask`.
- **FR-017**: Requirement analysis and resulting specification updates MUST be completed on the current branch without branch creation or branch switching.
- **FR-018**: The resulting design MUST remain compatible with module-scoped build and test execution for `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, and `mcp/features/mask`.

### Key Entities *(include if feature involves data)*

- **MCP API Contract**: A foundational MCP contract, DTO, or protocol type shared across MCP layers and owned by `mcp/api`.
- **MCP Workflow Shared Layer**: The new `mcp/workflow` module that owns workflow-specific shared helpers and workflow-specific SPI extensions without taking ownership of runtime-only implementations.
- **Core Runtime Workflow Implementation**: A runtime-only class owned by `mcp/core`, such as handler materializers, apply/validate handlers, execution services, query services, registries, or in-memory workflow session stores.
- **Workflow Bridge Contract**: A workflow-facing API type that may temporarily remain in `mcp/api` because multiple layers compile against it and moving it would require a larger SPI redesign.
- **Workflow-Scoped Argument Accessor**: A workflow-specific binding abstraction that replaces direct feature dependence on generic non-workflow argument helpers.
- **Workflow Contribution SPI**: Workflow-specific contribution and callback contracts shared between features and core runtime materialization.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `shardingsphere-mcp-workflow` exists as a separate module in the MCP reactor and is used by both `mcp/core` and workflow feature modules.
- **SC-002**: `shardingsphere-mcp-feature-encrypt` and `shardingsphere-mcp-feature-mask` declare no direct dependency on `shardingsphere-mcp-core`.
- **SC-003**: Workflow-specific shared helpers for binding, planning, validation, artifact payload shaping, masking, lifecycle, SQL, and descriptor construction no longer live in `mcp/api`.
- **SC-004**: Runtime-only workflow execution handlers, validation handlers, registries, materializers, and session-store implementations remain owned by `mcp/core`.
- **SC-005**: No unrelated generic execute or metadata-search helper is moved into `shardingsphere-mcp-workflow`.
- **SC-006**: Feature-facing workflow planning handlers no longer need to import generic non-workflow helper types directly in order to bind workflow requests.
- **SC-007**: Module-scoped build and test verification passes for `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, and `mcp/features/mask`.
- **SC-008**: This requirement work is completed on the existing branch only, with no branch creation or branch switching.

## Assumptions

- `mcp/api` should remain the foundational contract module rather than a shared implementation bucket.
- `mcp/workflow` is allowed because workflow shared behavior has become a distinct architectural layer rather than a small helper family.
- Keeping workflow model and bridge contracts in `mcp/api` for this iteration is acceptable if it avoids reopening a larger SPI redesign.
- Workflow-specific SPI contracts may move out of base `mcp/api` when they are only meaningful in the presence of the workflow shared layer.
- External MCP behavior is preserved while module ownership and dependency direction are cleaned up.
