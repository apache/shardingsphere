# Feature Specification: MCP Feature SPI Modularization

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-18
**Status**: Draft
**Input**: User description: "Refactor ShardingSphere-MCP so encrypt and mask become feature modules under `mcp/features`, with `spi` as the only stable contract layer, and load their tools and resources through SPI rather than hard-coding feature logic inside `mcp/core`."

## Clarifications

### Session 2026-04-18

- Target module structure is fixed as:
  - `mcp/features/spi`
  - `mcp/features/encrypt`
  - `mcp/features/mask`
  - `mcp/core`
  - `mcp/bootstrap`
- `mcp/core` should keep shared MCP platform responsibilities only and should not retain feature-specific encrypt or mask workflow logic.
- `mcp/features/spi` is the only stable contract layer between shared MCP infrastructure and feature implementations.
- `mcp/features/spi` may include handler contracts, descriptor models, response contracts, runtime facades, and finer-grained workflow subcontracts needed by feature-owned planning, validation, resource, or tool composition.
- `mcp/features/encrypt` and `mcp/features/mask` must each own their tools, resources, planning logic, execution preparation, validation logic, and feature-specific recommendation or property-template behavior.
- Feature modules should interact with upper-layer MCP only through SPI registration and stable SPI contracts, not through direct implementation coupling to `mcp/core`.
- Tool contracts should be split into encrypt-specific and mask-specific names rather than preserving a shared cross-feature workflow tool family.
- Resource URIs should be redesigned around feature ownership and pluginized discovery rather than preserving the pre-modularization URI layout by default.
- Backward compatibility is not a delivery requirement for this refactor because the product has not been released yet.
- The desired architecture must make encrypt and mask symmetrical with ShardingSphere feature-style modularity so that future MCP features can follow the same extension pattern.
- This feature is about modularization, extension boundaries, and first-release contract design. Existing encrypt and mask operator workflows remain governed by the already defined MCP workflow behavior unless this modularization intentionally redesigns the external MCP surface.
- Feature-exposed tools must be registered directly through `ToolHandler` SPI service registration inside the owning feature module rather than being enumerated by a top-level feature provider.
- Feature-exposed resources must be registered directly through `ResourceHandler` SPI service registration inside the owning feature module rather than being enumerated by a top-level feature provider.
- Tool and resource registry assembly in `mcp/core` must consume `ToolHandler` and `ResourceHandler` SPI as the only runtime source for feature surface discovery.
- A top-level feature metadata SPI is not required for this delivery. If such metadata is retained or introduced later, it must not control tool or resource surface assembly.
- Under the fixed module structure, `mcp/bootstrap` may package official feature jars directly, but it must not reference encrypt or mask implementation classes in code.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Keep core feature-agnostic through direct handler SPI loading (Priority: P1)

As an MCP platform maintainer, I want `mcp/core` to discover encrypt and mask capabilities only through `ToolHandler` and `ResourceHandler` SPI so that shared platform code no longer carries feature-specific workflow branches or provider-specific assembly logic.

**Why this priority**: This is the architectural goal of the refactor. Without it, the module split is only cosmetic and the design remains asymmetric.

**Independent Test**: Inspect the loaded MCP tool and resource catalog after startup and verify that encrypt and mask capabilities appear through handler SPI registration while shared MCP services remain available without feature-specific hard-coded dispatch in `mcp/core`.

**Acceptance Scenarios**:

1. **Given** the runtime includes `mcp/features/encrypt` and `mcp/features/mask`, **When** MCP loads tool and resource handlers, **Then** encrypt and mask capabilities are discovered through `ToolHandler` and `ResourceHandler` SPI registration rather than feature-specific branching or feature-provider surface assembly in `mcp/core`.
2. **Given** the runtime includes only one feature module, **When** MCP starts, **Then** shared core capabilities remain available and only the installed feature's tools and resources are exposed.
3. **Given** a maintainer inspects the shared MCP layer after the refactor, **When** they review the architecture, **Then** `mcp/core` contains shared platform responsibilities only and does not encode encrypt-versus-mask business decisions.

---

### User Story 2 - Let feature modules own and self-register their workflow surface (Priority: P1)

As an encrypt or mask feature maintainer, I want each feature module to own and self-register its own tools, resources, and workflow logic so that changes stay local to the feature and do not require editing shared MCP infrastructure.

**Why this priority**: Ownership and locality are required to make encrypt and mask truly pluggable rather than partially extracted.

**Independent Test**: Review the module boundaries and service-registration files and verify that feature-specific rule inspection, planning, recommendation, property-template, validation responsibilities, tool names, and resource URI design are implemented under the corresponding feature module and are exposed only through stable SPI contracts.

**Acceptance Scenarios**:

1. **Given** the encrypt feature module is loaded, **When** MCP exposes encrypt-related capabilities, **Then** the corresponding tools, resources, URI space, and workflow responsibilities originate from `mcp/features/encrypt` and its own SPI registrations.
2. **Given** the mask feature module is loaded, **When** MCP exposes mask-related capabilities, **Then** the corresponding tools, resources, URI space, and workflow responsibilities originate from `mcp/features/mask` and its own SPI registrations.
3. **Given** a feature maintainer changes encrypt-only or mask-only workflow behavior, **When** they complete the change, **Then** the change is confined to the target feature module plus stable SPI contracts rather than requiring edits across shared core implementation code.

---

### User Story 3 - Add future MCP features by registering handlers, not editing core (Priority: P2)

As an MCP platform architect, I want the same extension mechanism to work for future MCP features so that encrypt and mask become the first examples of a reusable handler-level feature architecture rather than one-off exceptions.

**Why this priority**: The value of the refactor comes from establishing a repeatable pattern, not just relocating two existing features.

**Independent Test**: Define a hypothetical future MCP feature against the published SPI contracts and verify that the architecture allows the new feature to register its own tools and resources without changing `mcp/core`.

**Acceptance Scenarios**:

1. **Given** a new MCP feature is designed after encrypt and mask are modularized, **When** its maintainer implements the published handler SPI contracts and registers service files, **Then** the feature can be loaded without adding new feature-specific branching to shared MCP core code.
2. **Given** the shared SPI contract is reviewed, **When** architects assess it, **Then** it is sufficient for feature-local tools, resources, and workflow services beyond encrypt and mask.

---

### User Story 4 - Publish clean first-release feature contracts (Priority: P2)

As an MCP client integrator, I want encrypt and mask to expose explicit feature-specific tool names and URI spaces from the start so that the MCP surface matches the pluginized design rather than carrying pre-release shared contracts forward.

**Why this priority**: The product has not been released, so this is the right moment to establish a clean contract surface instead of preserving a transitional one.

**Independent Test**: Review the published discovery surface and verify that encrypt and mask each expose their own tool families and resource URI schemes, with no ambiguous overlap and no dependency on legacy combined naming.

**Acceptance Scenarios**:

1. **Given** the encrypt feature module is loaded, **When** MCP lists tools, **Then** encrypt capabilities are exposed through encrypt-specific tool names rather than a generic shared workflow tool family.
2. **Given** the mask feature module is loaded, **When** MCP lists tools and resources, **Then** mask capabilities are exposed through mask-specific tool names and mask-owned URI design.
3. **Given** duplicate or conflicting tool names, URI patterns, or SPI registrations would make discovery ambiguous, **When** MCP loads the feature modules, **Then** the conflict is rejected explicitly rather than producing silent shadowing.

---

### Edge Cases

- What happens when `mcp/features/encrypt` or `mcp/features/mask` is absent from the runtime?
- How does the system handle duplicate tool names, duplicate resource URI patterns, or overlapping URI patterns across feature modules?
- What happens when a feature module is present but one or more handler SPI service registrations are missing, duplicated, or invalid?
- How does MCP expose discovery results when only a subset of feature modules is installed?
- How does the system keep redesigned encrypt and mask URI spaces non-overlapping and discoverable?
- What happens when a future feature tries to publish valid handlers but breaks naming or namespace conventions?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST organize MCP feature code under `mcp/features`, with `spi`, `encrypt`, and `mask` as distinct modules.
- **FR-002**: The system MUST define `mcp/features/spi` as the only stable contract layer between shared MCP infrastructure and feature implementations.
- **FR-003**: The system MUST place feature-facing `ToolHandler`, `ResourceHandler`, descriptor models, response contracts, runtime facades, and limited workflow subcontracts under `mcp/features/spi`.
- **FR-004**: The system MUST keep `mcp/core` responsible for shared MCP platform capabilities such as protocol, session, metadata access, execution facade, registry, and other feature-agnostic infrastructure.
- **FR-005**: The system MUST NOT retain encrypt-specific or mask-specific workflow branching in `mcp/core`.
- **FR-006**: The system MUST package encrypt-specific MCP tools, resources, planning logic, execution-preparation logic, validation logic, and feature-specific recommendation behavior inside `mcp/features/encrypt`.
- **FR-007**: The system MUST package mask-specific MCP tools, resources, planning logic, execution-preparation logic, validation logic, and feature-specific recommendation behavior inside `mcp/features/mask`.
- **FR-008**: The system MUST load encrypt and mask MCP tools through `ToolHandler` SPI registration rather than through direct implementation wiring or feature-provider surface assembly in shared core code.
- **FR-009**: The system MUST load encrypt and mask MCP resources through `ResourceHandler` SPI registration rather than through direct implementation wiring or feature-provider surface assembly in shared core code.
- **FR-010**: The system MUST allow `mcp/bootstrap` to start MCP runtime behavior without direct knowledge of encrypt or mask implementation classes.
- **FR-011**: The system MUST make feature modules depend only on stable contracts from `mcp/features/spi` and MUST prevent direct dependency on `mcp/core` implementation internals.
- **FR-012**: The system MUST let `mcp/core` depend only on `mcp/features/spi` for feature extensibility and MUST prevent direct dependency on `mcp/features/encrypt` or `mcp/features/mask`.
- **FR-013**: The system MUST ensure that feature modules interact with shared MCP services only through stable SPI-level contracts or shared abstractions, not by reaching into core implementation details.
- **FR-014**: The system MUST make encrypt and mask symmetrical in module structure, extension boundaries, and loading mechanism.
- **FR-015**: The system MUST expose encrypt workflow capabilities through encrypt-specific tool names rather than through a shared cross-feature workflow tool family.
- **FR-016**: The system MUST expose mask workflow capabilities through mask-specific tool names rather than through a shared cross-feature workflow tool family.
- **FR-017**: The system MUST redesign encrypt and mask resource URI schemes around feature ownership and pluginized discovery rather than preserving the pre-modularization URI layout by default.
- **FR-018**: The system MUST require every feature-exposed tool name and resource URI pattern to remain unique at registry level, even when features are loaded independently.
- **FR-019**: The system MUST fail explicitly when SPI registration creates duplicate or ambiguous tool names, resource URI patterns, overlapping URI patterns, or incomplete handler registration.
- **FR-020**: The system MUST expose only the tools and resources of feature modules that are actually loaded in the current MCP runtime.
- **FR-021**: The system MUST preserve unrelated shared MCP capabilities when one feature module is absent, disabled, or not packaged.
- **FR-022**: The system MUST surface invalid or incomplete feature registration with explicit startup or discovery errors rather than silent partial availability.
- **FR-023**: The system MUST allow a future MCP feature to be added by implementing the published `ToolHandler` and `ResourceHandler` SPI contracts and registering it, without introducing new feature-specific branching into `mcp/core`.
- **FR-024**: The system MUST let encrypt and mask feature modules own their own rule inspection, algorithm recommendation, property requirement, artifact generation, validation responsibilities, and external MCP surface design.
- **FR-025**: The system MUST optimize external MCP contract design for the first released pluginized architecture and is not required to preserve pre-release tool or URI compatibility.
- **FR-026**: The system MUST make feature ownership traceable so reviewers can determine whether a change belongs to shared MCP infrastructure or to a specific feature module.
- **FR-027**: The system MUST keep feature-local lifecycle changes confined to the owning feature module and stable SPI contracts wherever possible.
- **FR-028**: The system MUST make feature loading and registry assembly deterministic so MCP discovery results do not vary unpredictably across equivalent runtimes.
- **FR-029**: The system MUST support module-scoped build and test execution for `mcp/features/encrypt` and `mcp/features/mask` as independent reactor modules.
- **FR-030**: The system MUST NOT require `MCPFeatureProvider` indirection as the assembly source for tool or resource discovery.
- **FR-031**: If feature-level metadata SPI is retained or introduced in the future, it MUST be optional and MUST NOT control tool or resource registry assembly in this feature.
- **FR-032**: Under the fixed module layout, `mcp/bootstrap` MAY package official feature jars directly, but it MUST NOT reference encrypt or mask implementation classes in code paths that build runtime tool or resource surfaces.

### Key Entities *(include if feature involves data)*

- **Feature Surface SPI Contract**: The stable MCP extension contract defined in `mcp/features/spi` that feature modules use to register tools, resources, and feature-owned workflow behavior.
- **Workflow Subcontract**: A finer-grained SPI contract under `mcp/features/spi` that a feature implementation can use for planning, validation, artifact generation, or other workflow-local responsibilities.
- **Feature Module**: An MCP module under `mcp/features` that owns one domain capability, such as encrypt or mask, and contributes that capability through handler SPI registration.
- **Shared MCP Core Capability**: A feature-agnostic MCP service owned by `mcp/core`, such as session management, protocol handling, metadata access, execution facade, or registry assembly.
- **Registered Tool Surface**: The set of MCP tools made visible to callers after runtime handler SPI discovery and registry validation.
- **Registered Resource Surface**: The set of MCP resources made visible to callers after runtime handler SPI discovery and registry validation.
- **Feature Contract Surface**: The externally visible tool-name family and resource URI scheme owned by one MCP feature module.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Shared MCP core code no longer contains encrypt-specific or mask-specific workflow branching after the modularization is complete.
- **SC-002**: Encrypt and mask MCP tools and resources are discoverable in runtime output solely through `ToolHandler` and `ResourceHandler` SPI registration.
- **SC-003**: Removing one feature module from the runtime leaves unrelated shared MCP capabilities and the remaining feature modules available.
- **SC-004**: Duplicate or invalid SPI registration is rejected with explicit error reporting instead of silent partial loading.
- **SC-005**: A future MCP feature can be described against the published handler SPI contracts without requiring new feature-specific branching in `mcp/core`.
- **SC-006**: The resulting architecture exposes distinct encrypt and mask tool-name families and feature-scoped resource URI schemes without relying on legacy combined contracts.

## Assumptions

- Existing MCP registry mechanics for tool and resource discovery can remain the base mechanism, but the ownership and registration sources will move to handler-level SPI inside feature modules.
- Shared MCP business behavior already covered by `.specify/specs/001-proxy-encrypt-mask-mcp/spec.md` remains the behavioral baseline for encrypt and mask workflows unless this feature explicitly changes the external MCP surface.
- Repository-level rules from `AGENTS.md` and `CODE_OF_CONDUCT.md` remain authoritative for module boundaries, testing, and review expectations.
- The product has not been released yet, so external MCP tool names and resource URI schemes may be redesigned for a cleaner first release.
- Under the fixed module structure, packaging official feature jars directly in bootstrap is acceptable as long as runtime surface assembly still depends only on SPI contracts and does not reference feature implementation classes.

## Out of Scope

- Adding a brand-new MCP business feature beyond encrypt and mask in this change.
- Redefining the functional scope of encrypt or mask workflows beyond what is required for modularization and first-release contract design.
- Changing ShardingSphere-Proxy rule semantics.
- Introducing a new transport model in `mcp/bootstrap`.
- Building an external plugin directory or dynamic classpath loader outside the current fixed module structure.
- Rewriting shared MCP session, protocol, or execution behavior except where required to remove feature-specific coupling.
