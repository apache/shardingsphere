# Feature Specification: MCP Public API Flattening

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-05-01
**Status**: Draft
**Input**: User description: "Use speckit to sort out the requirement for the MCP api/core boundary, keep only API-level abstractions in `mcp/api`, remove implementation classes from the API module, and keep all public abstractions at the same level."

## Clarifications

### Session 2026-05-01

- This requirement analysis must stay on the current branch; no new branch may be created, checked out, or switched to.
- `mcp/api` must be a pure API module and must not retain production implementation classes.
- Public MCP extension concepts must stay at one abstraction level; `tool` and `resource` are valid top-level public capability categories, while `workflow contribution` is not.
- Workflow is not a new public capability category. It is an internal orchestration pattern built on tools.
- The preferred end state is the more thorough design: feature-specific planning remains a tool, while workflow apply and workflow validate become platform-scoped generic tools.
- The preferred end state is confirmed for this requirement set; public workflow apply and validate are allowed to replace feature-specific public apply and validate tools.
- Compatibility concerns around existing public workflow tool names do not constrain this requirement analysis.
- `mcp/workflow` is explicitly retained as an internal shared module and is not being removed in this redesign.
- This iteration stops at purifying the existing `mcp/api` module; it does not further split `mcp/api` into separate protocol-only and feature-only API modules.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Expose only same-level public MCP capabilities (Priority: P1)

As an MCP API consumer or extension author, I want the public MCP API to expose only same-level capability concepts so that I can understand the extension model without learning runtime-only composition terms.

**Why this priority**: The root design problem is not only misplaced classes; it is that the public API currently mixes protocol concepts with internal contribution and workflow composition concepts.

**Independent Test**: Inspect the public MCP API after the redesign and verify that feature contribution is expressed only through public tool and public resource contracts, with no public workflow contribution or direct contribution categories.

**Acceptance Scenarios**:

1. **Given** an extension author reads the MCP public API surface, **When** they inspect the feature contribution contracts, **Then** they find only tool and resource capability categories as top-level public concepts.
2. **Given** an extension author reads the MCP public API surface, **When** they look for workflow-specific contribution or composition types, **Then** they do not find public categories such as direct contribution, workflow contribution, invoker, reader, or materializer.
3. **Given** a reviewer inspects `mcp/api`, **When** they classify every retained production type, **Then** each type belongs to API-level contracts, DTOs, descriptors, context capabilities, or exceptions rather than implementation helpers.

---

### User Story 2 - Keep workflow reusable without making it a public top-level category (Priority: P1)

As a workflow feature maintainer, I want encrypt, mask, and future workflow-capable features to reuse shared workflow behavior without leaking workflow orchestration concepts into the public MCP API.

**Why this priority**: Workflow is real shared behavior, but it should be represented as internal shared architecture rather than a new public API tier beside tool and resource.

**Independent Test**: Verify that a workflow-capable feature can provide feature-specific planning tools and internal workflow definitions while the public API still exposes workflow execution and validation through generic platform-scoped tools.

**Acceptance Scenarios**:

1. **Given** a workflow-capable feature exposes planning behavior, **When** it contributes its public API surface, **Then** it contributes planning as a normal tool rather than through a public workflow contribution type.
2. **Given** a workflow plan has already been created, **When** a caller executes or validates that plan, **Then** the public API uses generic workflow apply and validate tools instead of feature-specific public apply and validate categories.
3. **Given** a future workflow-capable feature is added, **When** it integrates with the platform, **Then** it reuses the same generic workflow execution and validation path without requiring a new public capability category.

---

### User Story 3 - Make internal workflow routing explicit and stable (Priority: P2)

As an MCP runtime maintainer, I want the runtime to route workflow execution and validation through explicit internal workflow identity so that generic public workflow tools do not depend on hidden type guessing.

**Why this priority**: Once workflow apply and validate become generic public tools, runtime routing must rely on explicit workflow identity instead of accidental Java type knowledge.

**Independent Test**: Inspect the workflow runtime model and verify that a stored workflow snapshot carries explicit workflow identity and that runtime workflow definitions map that identity to the correct internal validation and synchronization behavior.

**Acceptance Scenarios**:

1. **Given** a workflow plan is persisted, **When** it is later applied or validated, **Then** the runtime can determine its workflow kind explicitly from stored workflow metadata.
2. **Given** two different workflow features share the same generic apply and validate public tools, **When** their plans are executed, **Then** the runtime dispatches to the correct internal validation and synchronization strategies for each workflow kind.
3. **Given** a new workflow-capable feature is introduced, **When** it is wired into the runtime, **Then** the runtime extends internal workflow definition registration rather than changing the public API category model.

---

### Edge Cases

- What happens when a workflow plan exists but its stored workflow kind is missing or unknown?
- What happens when a future workflow feature needs extra internal execution or validation strategy without changing the public generic workflow tool shape?
- What happens when a feature exposes only resources or only tools and does not participate in workflow at all?
- What happens when an API-level helper is discovered to contain hidden runtime behavior and therefore no longer qualifies as pure API?
- What happens when a workflow-specific context capability is needed by some tools but should not leak into the base public API module?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `mcp/api` MUST be a pure API module and MUST NOT contain production implementation classes.
- **FR-002**: The public MCP feature contribution model MUST expose only same-level public capability categories for contributed features.
- **FR-003**: The public same-level capability categories MUST be public tools and public resources.
- **FR-004**: The public MCP API MUST NOT expose top-level public categories or helper families such as contribution, direct contribution, workflow contribution, invoker, reader, materializer, or default wrapper classes.
- **FR-005**: Workflow MUST NOT be modeled as a top-level public capability category beside tool and resource.
- **FR-006**: Workflow planning MAY remain feature-specific, but every public workflow planning entrypoint MUST be expressed as a normal public tool.
- **FR-007**: Public workflow execution MUST be expressed through a platform-scoped generic workflow apply tool rather than feature-specific public apply categories.
- **FR-008**: Public workflow validation MUST be expressed through a platform-scoped generic workflow validate tool rather than feature-specific public validate categories.
- **FR-009**: Workflow-capable features MUST provide their feature-specific workflow behavior through internal workflow definitions or equivalent workflow-scoped contracts owned outside `mcp/api`.
- **FR-010**: Internal workflow definitions MUST map explicit workflow identity to the internal workflow validation and post-apply synchronization behavior required by that workflow kind.
- **FR-011**: A stored workflow plan MUST carry explicit workflow identity so that generic workflow execution and validation do not rely on hidden type inference.
- **FR-012**: `mcp/workflow` MUST own workflow-shared contracts, workflow-shared helpers, and internal workflow definition seams that are required by both workflow-capable features and runtime core.
- **FR-013**: `mcp/core` MUST own runtime-only workflow dispatch, workflow execution, workflow validation dispatch, registries, materialization, and workflow session-store implementations.
- **FR-014**: `mcp/features/encrypt` and `mcp/features/mask` MUST depend on `mcp/api` and `mcp/workflow`, and MUST NOT depend directly on `mcp/core`.
- **FR-015**: Feature-facing workflow planning code MUST obtain workflow binding and workflow helper behavior through workflow-scoped contracts rather than generic helper types leaked from runtime or from base API.
- **FR-016**: The resulting public API surface MUST allow a new workflow-capable feature to be added without introducing a new top-level public capability category.
- **FR-017**: The resulting public API surface MUST make it possible to classify every public type as either public tool contract, public resource contract, shared descriptor or DTO, context capability contract, or exception contract.
- **FR-018**: Requirement analysis and resulting specification updates MUST be completed on the current branch without branch creation or branch switching.

### Key Entities *(include if feature involves data)*

- **Public Tool Contract**: A public API contract that defines a tool contributed by a feature or by the platform.
- **Public Resource Contract**: A public API contract that defines a resource contributed by a feature or by the platform.
- **Workflow Kind**: The explicit internal identity of a workflow family such as encrypt or mask, stored with a workflow plan and used for generic runtime dispatch.
- **Workflow Runtime Definition**: An internal workflow-scoped definition that binds one workflow kind to its planning continuation, validation behavior, synchronization behavior, and any required workflow-specific state handling.
- **Workflow Snapshot**: The persisted workflow plan state that carries explicit workflow kind, request state, feature-specific workflow data, and lifecycle state.
- **API-Level Contract Type**: A public type in `mcp/api` that remains after the redesign because it represents a public contract, descriptor, DTO, context capability, or exception rather than an implementation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: No retained production class in `mcp/api` is classified as an implementation helper, default wrapper, direct contribution, workflow contribution, materializer, invoker, or reader.
- **SC-002**: The public MCP feature contribution surface contains only tool and resource capability categories as top-level public contribution concepts.
- **SC-003**: Generic public workflow apply and validate tools can execute both encrypt and mask workflow plans by using explicit workflow kind recorded in the stored workflow plan.
- **SC-004**: `mcp/features/encrypt` and `mcp/features/mask` declare no direct dependency on `mcp/core`.
- **SC-005**: A new workflow-capable feature can be integrated by adding feature-specific planning tools plus internal workflow definitions, without adding a new top-level public API category.
- **SC-006**: Module-scoped verification passes for `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, and `mcp/features/mask`.
- **SC-007**: This requirement package is updated on the existing branch only, with no branch creation or branch switching.

## Assumptions

- Workflow remains important shared behavior, but it is treated as an internal orchestration layer rather than a new public API category.
- Public planning tools may stay feature-specific because feature intent gathering differs substantially by workflow family, while apply and validate can be normalized at the platform level.
- Internal workflow runtime definitions may be introduced even if they require broader internal rewiring, because they do not violate the requirement that `mcp/api` stay pure.
