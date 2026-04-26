# Feature Specification: MCP Runtime Pluginization Completion

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-20
**Status**: Draft
**Input**: User description: "For MCP true pluginization, finish the runtime and packaging boundary so feature jars are discovered from the startup classpath rather than compile-time wiring in bootstrap, keep the official distribution shipping encrypt and mask by default, and split the work into implementable slices."

## Clarifications

### Session 2026-04-20

- This feature is a follow-up to `003-mcp-feature-spi` and does not reopen the already decided feature ownership split between `mcp/core`, `mcp/features/spi`, `mcp/features/encrypt`, and `mcp/features/mask`.
- "True pluginization" in this delivery means startup-time classpath discovery through ShardingSphere SPI, not hot reload, post-start jar installation, or a custom plugin manager.
- `mcp/bootstrap` must stop owning the default feature set through main-scope feature dependencies.
- The official packaged runtime must continue to expose encrypt and mask by default, but that default set must become an explicit packaging decision of `distribution/mcp`.
- External feature jars may be added through the packaged runtime extension classpath contract under `plugins/`.
- This feature should not introduce a new runtime aggregation module, a dynamic classloader framework, or hot-plug lifecycle management.
- Repository documentation must stop telling maintainers to add new features by making `mcp/bootstrap` depend on them directly.
- Bootstrap-level tests must verify that discovery publishes whatever feature handlers are present on the classpath, rather than assuming encrypt and mask are intrinsic to bootstrap itself.
- Distribution-level verification must prove that the official packaged runtime still includes the intended default feature set.
- Because the MCP distribution is not a released public product yet, changing the implicit feature surface of the standalone `shardingsphere-mcp-bootstrap` artifact is acceptable in this delivery if the packaged distribution behavior remains explicit and documented.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Publish feature surface from the startup classpath only (Priority: P1)

As an MCP runtime maintainer, I want `mcp/bootstrap` to publish tools and resources strictly from handlers present on the startup classpath so that feature availability is driven by installed jars instead of compile-time feature wiring in bootstrap.

**Why this priority**: Without this boundary, the current architecture is only structurally modular while runtime behavior still depends on bootstrap-level default coupling.

**Independent Test**: Start bootstrap with different feature-jar classpath combinations and verify that only the installed features are published, while shared MCP tools and resources remain available.

**Acceptance Scenarios**:

1. **Given** the runtime classpath includes only shared MCP infrastructure and no feature jar, **When** bootstrap starts, **Then** shared MCP tools and resources are published without encrypt or mask feature surfaces.
2. **Given** the runtime classpath includes shared MCP infrastructure plus the encrypt feature jar, **When** bootstrap starts, **Then** encrypt surfaces are published and mask surfaces are absent.
3. **Given** the runtime classpath includes shared MCP infrastructure plus both encrypt and mask feature jars, **When** bootstrap starts, **Then** both feature surfaces are published without bootstrap-specific feature branching.

---

### User Story 2 - Make the official distribution own the default feature bundle (Priority: P1)

As a distribution maintainer, I want `distribution/mcp` to select and package the official default feature set explicitly so that the shipped runtime keeps its current capabilities without requiring bootstrap to hard-code feature dependencies.

**Why this priority**: Runtime pluginization must not accidentally degrade the packaged product. Moving responsibility to distribution preserves behavior while cleaning the architecture.

**Independent Test**: Build the MCP distribution and verify that the packaged runtime still contains the encrypt and mask feature jars and publishes their surfaces on startup.

**Acceptance Scenarios**:

1. **Given** the official MCP distribution is packaged, **When** its runtime libraries are inspected, **Then** the encrypt and mask feature jars are included because distribution selected them explicitly.
2. **Given** the official MCP distribution starts from the packaged layout, **When** discovery runs, **Then** encrypt and mask surfaces are still available by default.
3. **Given** a maintainer depends on `shardingsphere-mcp-bootstrap` directly instead of the packaged distribution, **When** they omit feature jars from the runtime classpath, **Then** bootstrap no longer exposes those missing feature surfaces implicitly.

---

### User Story 3 - Add external features through a clear startup extension contract (Priority: P2)

As an MCP feature author or operator, I want a documented way to place additional feature jars onto the runtime classpath so that new features can be installed without modifying bootstrap source code.

**Why this priority**: A plugin architecture is only useful if maintainers and operators know how the extension path works in practice.

**Independent Test**: Place an additional feature jar under the documented packaged extension location and verify that startup discovery exposes the feature in the same way as built-in packaged features.

**Acceptance Scenarios**:

1. **Given** an external feature jar is added under the documented extension classpath location before process start, **When** MCP starts, **Then** the feature can be discovered through standard SPI loading.
2. **Given** an external feature jar is added after MCP has already started, **When** no restart happens, **Then** MCP does not expose that feature because discovery is startup-time only.
3. **Given** an external feature jar has invalid or incomplete SPI registration, **When** discovery runs, **Then** MCP rejects the registration explicitly rather than partially exposing it.

---

### User Story 4 - Split tests and documentation by responsibility (Priority: P2)

As an MCP maintainer, I want bootstrap tests, distribution tests, and documentation to each validate the right boundary so that reviewers can see which layer guarantees plugin discovery and which layer guarantees the official default bundle.

**Why this priority**: The current confusion exists partly because build wiring, packaging behavior, and runtime discovery responsibilities are mixed together.

**Independent Test**: Review bootstrap tests, distribution verification, and README guidance and verify that each artifact explains or validates a single clear boundary.

**Acceptance Scenarios**:

1. **Given** bootstrap-level tests, **When** a reviewer inspects them, **Then** they verify classpath-driven discovery rather than assuming encrypt and mask are intrinsic bootstrap features.
2. **Given** packaged distribution verification, **When** a reviewer inspects it, **Then** it verifies the official default feature bundle independently from bootstrap internals.
3. **Given** the MCP README, **When** a contributor reads how to add a feature, **Then** they are instructed to add the feature to the appropriate runtime classpath or distribution packaging layer rather than wiring it into bootstrap directly.

---

### Edge Cases

- What happens when no feature jar is present on the runtime classpath?
- What happens when only one of encrypt or mask is present?
- What happens when an external feature jar is placed in the extension directory after the process has already started?
- What happens when a feature jar is present but its `META-INF/services` entry is missing, duplicated, or invalid?
- What happens when bootstrap tests still transitively see encrypt and mask jars through test scope?
- What happens when a maintainer consumes `shardingsphere-mcp-bootstrap` directly and assumes official distribution defaults still apply?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define MCP feature availability as the set of valid feature handlers present on the startup runtime classpath.
- **FR-002**: The system MUST use ShardingSphere SPI discovery as the runtime mechanism for feature handler loading.
- **FR-003**: The system MUST treat runtime pluginization in this delivery as startup-time discovery only and MUST NOT require hot reload or post-start plugin installation.
- **FR-004**: `mcp/bootstrap` MUST NOT own the official default feature set through main-scope dependencies on `shardingsphere-mcp-feature-encrypt` or `shardingsphere-mcp-feature-mask`.
- **FR-005**: `mcp/bootstrap` MAY keep feature modules available in test scope if needed to preserve or refactor test coverage during the transition.
- **FR-006**: The official packaged MCP runtime MUST continue to ship encrypt and mask by default.
- **FR-007**: `distribution/mcp` MUST own the packaging decision for which official feature jars are included in the packaged runtime.
- **FR-008**: The packaged MCP runtime MUST keep a documented extension classpath location for externally added feature jars.
- **FR-009**: The packaged runtime MUST expose `plugins/` as the extension classpath location for JDBC drivers and optional externally added feature jars.
- **FR-010**: Documentation for adding a new MCP feature MUST instruct maintainers to wire packaged defaults through the distribution layer or external runtime classpath, not by adding direct bootstrap implementation dependencies.
- **FR-011**: Bootstrap-level tests MUST validate generic classpath-driven discovery behavior rather than hard-coding the official default feature set as an intrinsic bootstrap guarantee.
- **FR-012**: Distribution-level verification MUST validate that the official packaged runtime still publishes encrypt and mask by default.
- **FR-013**: The implementation MUST prove, through at least one isolated test fixture or equivalent verification, that a feature surface can be discovered from classpath registration without bootstrap implementation knowledge of that feature.
- **FR-014**: The system MUST expose only the tools and resources whose handlers are valid and present on the startup classpath.
- **FR-015**: The system MUST preserve shared MCP tools and resources even when no optional feature module is present.
- **FR-016**: The system MUST reject invalid, incomplete, duplicate, or ambiguous handler registration explicitly rather than silently exposing a partial feature surface.
- **FR-017**: The system MUST document that adding or removing feature jars requires a process restart for discovery changes to take effect.
- **FR-018**: The feature MUST NOT introduce a new runtime module solely to aggregate default MCP features.
- **FR-019**: The feature MUST NOT introduce a custom dynamic plugin manager or dynamic classloader lifecycle in this delivery.
- **FR-020**: The implementation MUST keep the runtime discovery contract deterministic so equivalent classpaths produce the same published tool and resource surface.
- **FR-021**: The feature MUST keep existing feature module SPI contracts as the basis of discovery and MUST NOT reintroduce bootstrap-side feature-specific code branching.
- **FR-022**: Reviewer-facing documentation MUST distinguish clearly between bootstrap responsibility, distribution responsibility, and operator extension responsibility.

### Key Entities *(include if feature involves data)*

- **Startup Classpath Feature Set**: The set of feature jars and SPI registrations visible to MCP when the process starts.
- **Official Feature Bundle**: The default feature jars intentionally packaged by `distribution/mcp` for the official MCP runtime.
- **Extension Classpath Location**: The packaged runtime `plugins/` directory where operators can place additional jars before process start.
- **Bootstrap Runtime Surface**: The final set of tools and resources published by `mcp/bootstrap` after startup discovery and registry validation.
- **Fixture Feature Plugin**: A minimal test-only feature implementation used to prove that bootstrap discovery works from classpath registration rather than compile-time feature wiring.
- **Packaging Boundary**: The decision line that separates runtime discovery responsibilities from distribution-level default bundle selection.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `mcp/bootstrap/pom.xml` no longer declares main-scope dependencies on encrypt or mask feature modules.
- **SC-002**: The official packaged MCP runtime still exposes encrypt and mask surfaces by default after packaging responsibility moves to `distribution/mcp`.
- **SC-003**: Bootstrap-level tests no longer hard-code the official encrypt and mask set as intrinsic bootstrap behavior.
- **SC-004**: At least one isolated verification proves that an arbitrary SPI-registered feature can be published without bootstrap implementation knowledge of that feature.
- **SC-005**: MCP documentation describes startup-time plugin discovery, the packaged extension location, and the separation between bootstrap and distribution responsibilities.
- **SC-006**: Reviewers can explain, from code and documentation alone, why removing a feature jar from the runtime classpath removes its MCP surface without affecting shared MCP capabilities.

## Assumptions

- Existing handler-level SPI discovery in shared infrastructure is functionally sufficient for runtime pluginization once packaging boundaries are corrected.
- The packaged runtime should keep official baseline jars under `lib/` and treat `plugins/` as the operator-managed extension directory for drivers and optional feature jars.
- The product is still pre-release enough that direct-bootstrap implicit feature availability can change if packaged distribution behavior remains explicit and documented.
- This feature is about runtime/plugin packaging boundaries and not about redesigning encrypt or mask workflow behavior.

## Out of Scope

- Hot reload or unloading of feature jars without restart.
- A new dedicated runtime plugin manager or dynamic classloader subsystem.
- Introducing a new MCP feature beyond the already modularized feature model.
- Reopening the `003-mcp-feature-spi` decision that feature ownership belongs under `mcp/features/*`.
- Changing encrypt or mask business semantics beyond what is required for runtime pluginization boundaries.
