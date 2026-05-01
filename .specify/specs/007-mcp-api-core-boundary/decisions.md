# Design Decisions: MCP Public API Flattening

**Scope**: Lock the three highest-impact design decisions before implementation so the API flattening work does not drift back toward contribution-style wrappers or stringly typed workflow routing.

## Decision 1: Public provider SPI exposes only tools and resources

### Context

The current public SPI is centered on `MCPFeatureProvider#getContributions()`, which leaks internal assembly concepts into `mcp/api` through `MCPContribution`, `MCPDirectToolContribution`, `MCPDirectResourceContribution`, and workflow contribution wrappers.

At the same time, the stable public capability abstractions already exist:

- `mcp/api/src/main/java/org/apache/shardingsphere/mcp/tool/handler/ToolHandler.java`
- `mcp/api/src/main/java/org/apache/shardingsphere/mcp/resource/ResourceHandler.java`

The redesign goal is to keep public abstractions at one level and make `mcp/api` a pure API module.

### Decision

`MCPFeatureProvider` will expose only public tools and public resources:

- `default Collection<ToolHandler> getToolHandlers()`
- `default Collection<ResourceHandler> getResourceHandlers()`

Both methods must:

- return non-null collections
- default to empty collections
- be treated as immutable by callers

This iteration keeps the existing `ToolHandler` and `ResourceHandler` type names.

We are **not** renaming them to `MCPTool` or `MCPResource` in this redesign, because the boundary problem is the public assembly model, not the interface names. A rename would create broad churn without changing the public abstraction shape.

### Rejected alternatives

- Keep `getContributions()` and only move implementations out of `mcp/api`
  - Rejected because it preserves the wrong public abstraction model.
- Add `getWorkflowDefinitions()` or similar to `MCPFeatureProvider`
  - Rejected because it would leak workflow-internal concepts back into `mcp/api`.
- Rename `ToolHandler` and `ResourceHandler` in the same iteration
  - Rejected because it adds mechanical churn without resolving the core boundary issue.

### Consequences

- `mcp/api` becomes readable through only same-level public nouns.
- Feature providers become simpler and no longer need public wrapper classes.
- `mcp/core` must load tools/resources directly instead of materializing contributions.

## Decision 2: Workflow identity uses a typed internal value object, not enum inference

### Context

Generic public workflow apply and validate tools can only work safely if runtime dispatch is based on explicit workflow identity rather than:

- tool names
- request Java types
- feature-specific handler wiring hidden inside wrappers

The identity must remain extensible for future workflow families and must not require central enum edits.

### Decision

Introduce a typed internal workflow identity in `mcp/workflow`:

- `WorkflowKind` as a value object
- canonical serialized field name: `workflow_kind`
- canonical identifier format: lowercase dotted name `<feature>.<workflow>`

Examples:

- `encrypt.rule`
- `mask.rule`

`WorkflowContextSnapshot` will carry `WorkflowKind`, and any payload/binder layer that persists or serializes the snapshot will persist the canonical `workflow_kind` string.

The workflow runtime registry must validate:

- the identifier is present
- the identifier format is valid
- each registered `WorkflowKind` is unique

### Rejected alternatives

- Raw `String` everywhere
  - Rejected because it leaves runtime contracts stringly typed and easy to mismatch.
- Central enum
  - Rejected because every new workflow family would require core enum edits.
- Infer workflow family from tool name or request type
  - Rejected because generic apply/validate must not rely on hidden coupling.

### Consequences

- Runtime dispatch becomes explicit and type-safe inside the workflow layer.
- Future workflow families can be added without editing a central enum.
- Snapshot copy/persistence tests must start asserting `workflow_kind`.

## Decision 3: Workflow runtime definitions live behind a separate workflow-scoped SPI

### Context

Workflow still needs shared internal behavior for:

- validation
- post-apply synchronization
- kind-based dispatch

But that behavior must not reappear in `mcp/api` as another public contribution tier.

We therefore need an internal workflow boundary that is:

- outside `mcp/api`
- shared by workflow-capable features and `mcp/core`
- small enough not to become a renamed `MCPWorkflowToolContribution`

### Decision

Introduce a separate workflow-scoped SPI in `mcp/workflow`, loaded independently from `MCPFeatureProvider`:

- `MCPWorkflowDefinitionProvider`
- `WorkflowRuntimeDefinition`

`WorkflowRuntimeDefinition` is a small internal aggregate that binds one `WorkflowKind` to runtime-only workflow behavior:

- `WorkflowKind getWorkflowKind()`
- `MCPWorkflowValidationHandler getValidationHandler()`
- `MCPWorkflowApplySynchronizationHandler getApplySynchronizationHandler()`

Public planning tools do **not** belong inside `WorkflowRuntimeDefinition`.

Planning remains part of the public tool surface and is still contributed through `MCPFeatureProvider#getToolHandlers()`. Planning code is responsible for stamping the created snapshot with its `WorkflowKind`.

`mcp/core` will therefore have two separate loading paths:

- public capability loading from `MCPFeatureProvider`
- internal workflow-definition loading from `MCPWorkflowDefinitionProvider`

Generic public `apply` and `validate` tool handlers in `mcp/core` will resolve the `WorkflowRuntimeDefinition` by `WorkflowKind` from the stored snapshot.

### Rejected alternatives

- Put workflow definitions back onto `MCPFeatureProvider`
  - Rejected because it pollutes `mcp/api` with workflow-internal concepts.
- Put planning tool handlers inside workflow runtime definitions
  - Rejected because it mixes public capability registration with internal runtime dispatch.
- Register validation and synchronization handlers separately with no aggregate definition
  - Rejected because it makes kind-based configuration easier to mismatch.
- Keep `MCPWorkflowToolContribution` and strip some fields
  - Rejected because it remains the same wrapper pattern under a smaller payload.

### Consequences

- `mcp/workflow` becomes the only shared home for workflow-internal extension seams.
- `mcp/api` stays pure even though workflow reuse remains real.
- Encrypt and mask will contribute:
  - planning tools through `MCPFeatureProvider`
  - workflow runtime definitions through `MCPWorkflowDefinitionProvider`

## Summary

The implementation should now assume the following fixed target shape:

1. `mcp/api` public SPI: only tool/resource contribution methods.
2. `mcp/workflow` internal identity: `WorkflowKind` value object with canonical `workflow_kind`.
3. `mcp/workflow` internal SPI: separate workflow definition provider plus small runtime definition aggregate.

Any implementation approach that reintroduces contribution wrappers, raw string workflow routing, or workflow internals into `mcp/api` is out of scope for this spec.
