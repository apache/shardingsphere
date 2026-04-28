# Research: MCP Feature SPI Simplification

## Decision 1: `mcp/features/spi` only keeps pure SPI interfaces and minimal signature-owned support types

- **Decision**: The end state of `mcp/features/spi` is a pure extension boundary. It may contain SPI interfaces and only the minimal enums, annotations, and constants that are inseparable from those interfaces. It may not contain concrete services, stores, binders, payload builders, registries, parsers, or default concrete handlers.
- **Rationale**:
  - This matches the requirement that the module should be "as clean as possible" and ideally contain only SPI interfaces.
  - It makes the stable extension surface obvious to maintainers and future feature authors.
  - It removes the current ambiguity where readers must distinguish real SPI from shared runtime baggage.
- **Alternatives considered**:
  - Keep the current "SPI plus large shared contract/runtime" model: rejected because it directly conflicts with the new requirement.
  - Keep a few convenient concrete workflow helpers in `mcp/features/spi`: rejected because convenience is exactly how the SPI boundary became too heavy.

## Decision 2: Shared non-SPI contracts move to a dedicated shared API module, not to core internals

- **Decision**: Requests, responses, descriptors, metadata models, protocol models, exceptions, and other non-SPI contracts compiled against by both `mcp/core` and feature modules move to a dedicated shared API module, proposed as `mcp/api`.
- **Rationale**:
  - These types are not SPI, so they do not belong in `mcp/features/spi`.
  - Moving them straight to `mcp/core` would force feature modules to compile against core internals, weakening module boundaries.
  - A shared API module gives both core and features a neutral compile-time home.
- **Alternatives considered**:
  - Leave them in `mcp/features/spi`: rejected because they are not SPI interfaces.
  - Move them all into `mcp/core`: rejected because that creates a reverse dependency pressure from features to core.

## Decision 3: Do not introduce `mcp/features/support`; decompose shared concrete helpers instead

- **Decision**: A new `mcp/features/support` module will not be introduced. Reusable concrete helpers that are shared by multiple feature modules must be decomposed so that shared contracts move to `mcp/api` or stay as SPI interfaces, while final concrete implementations become either core-private runtime code or feature-owned code.
- **Rationale**:
  - Some current workflow helpers are concrete implementations, not contracts.
  - They cannot stay in `mcp/features/spi`, but introducing a support module would just create a new shared dumping ground.
  - Decomposition forces the design to distinguish contracts from implementations and keeps the final graph simpler.
- **Alternatives considered**:
  - Introduce `mcp/features/support`: rejected by explicit requirement confirmation.
  - Move all shared concrete helpers into `mcp/core`: rejected because feature modules would then depend on core implementation code.
  - Duplicate every shared helper into each feature module: only acceptable when the helper proves to be feature-owned after decomposition, otherwise rejected because it increases drift.

## Decision 4: Core-private runtime helpers move into `mcp/core`

- **Decision**: Registry-only utilities, URI parsing that is only used by core registries, runtime context implementations, in-memory stores used only by core, and other concrete runtime infrastructure move into `mcp/core`.
- **Rationale**:
  - These classes are implementation details of the shared runtime, not extension contracts.
  - Keeping them in `mcp/features/spi` hides their actual ownership and encourages accidental feature coupling to core implementation details.
- **Alternatives considered**:
  - Keep them in `mcp/features/spi` because core currently depends on that module: rejected because dependency convenience is not a valid ownership rule.
  - Move them into a shared API module: rejected because they are implementations, not shared contracts.

## Decision 5: Interface-only runtime seams may remain in SPI

- **Decision**: If features need a stable way to access shared runtime capabilities, interface-only seams such as feature runtime facades may remain in `mcp/features/spi`, but their implementations must live outside that module.
- **Rationale**:
  - Features still need a stable compile-time boundary to shared metadata, execution, capability, or session access.
  - The new requirement is about removing concrete weight from the SPI module, not about eliminating extension-facing interfaces.
- **Alternatives considered**:
  - Remove all runtime seams from SPI and let features call core implementations directly: rejected because that breaks the extension boundary.
  - Keep both interfaces and default implementations in SPI: rejected because it keeps the module heavy.

## Decision 6: External MCP surface redesign is out of scope for this requirement

- **Decision**: Tool-name families, resource URI layouts, and feature workflow semantics are not part of this simplification unless another specification explicitly changes them.
- **Rationale**:
  - The current requirement is already substantial and concerns module purity, ownership, and dependencies.
  - Mixing in external surface redesign would obscure the acceptance boundary and make the refactor harder to review.
- **Alternatives considered**:
  - Reopen external naming and URI decisions during the same change: rejected because it couples internal architectural cleanup to unrelated product-surface changes.

## Decision 7: Classify before moving

- **Decision**: Before implementation, the current `mcp/features/spi` production sources must be classified into five buckets:
  - pure SPI contract
  - shared API contract
  - core runtime implementation
  - feature-owned implementation
- **Rationale**:
  - The current module mixes several ownership models.
  - A class-by-class destination matrix avoids ad hoc moves and helps reviewers validate that the new boundaries are intentional.
- **Alternatives considered**:
  - Start moving files package by package without a classification matrix: rejected because it is likely to recreate the same muddled boundary under different module names.

## Decision 8: Use the current package families as the first-pass classification seed

- **Decision**: The initial classification matrix should start from the package families that exist today under `mcp/features/spi`, rather than from abstract concepts alone.
- **Rationale**:
  - The module is already heavy enough that reviewers need concrete starting evidence, not just a target architecture.
  - Current package-level weight shows where the simplification pressure really comes from.
  - Several boundary leaks are visible directly in today's code and should become explicit acceptance anchors.
- **Observed baseline**:
  - `mcp/features/spi` currently contains `6128` production lines.
  - Workflow-like families under `tool/service/workflow`, `tool/model/workflow`, `tool/handler/workflow`, `tool/descriptor`, `tool/request`, and `tool/response` account for `3932` lines, about `64.2%` of the module.
  - `tool/service/workflow` alone contributes `1858` lines across `14` files.
  - `tool/model/workflow` contributes `1273` lines across `18` files.
  - `MCPFeatureContext` currently exposes `WorkflowContextStore` directly, proving that the SPI surface leaks a concrete support implementation in its method signature.
  - `WorkflowExecutionService` is a concrete orchestration service that applies artifacts, persists lifecycle state, and builds payload maps, which is runtime behavior rather than SPI.
  - `ResourceHandlerRegistry` in `mcp/core` constructs and validates `MCPUriPattern`, indicating that URI-pattern parsing is a core-private registry concern rather than a feature SPI concern.
  - `EncryptFeatureProvider` and `MaskFeatureProvider` directly instantiate `WorkflowExecutionToolHandler` and `WorkflowValidationToolHandler`, showing that `mcp/features/spi` is currently acting as a shared support/runtime module rather than a pure SPI layer.
- **Initial seed mapping**:
  - likely `pure-spi`: `capability/*`, `context/MCPFeatureContext` after signature cleanup, `feature/spi/*`, `tool/handler/ToolHandler`, `resource/handler/ResourceHandler`
  - likely `shared-api`: `metadata/model/*`, `metadata/jdbc/RuntimeDatabaseProfile`, `protocol/*`, `protocol/error/*`, `protocol/exception/*`, `protocol/response/*`, `tool/descriptor/MCPToolDescriptor`, `MCPToolFieldDefinition`, `MCPToolValueDefinition`, `tool/request/*`, `tool/response/*`, `resource/uri/MCPUriVariables`
  - likely decomposition candidates before final placement: `tool/service/workflow/*`, `tool/model/workflow/*`, `tool/handler/workflow/*`, `tool/descriptor/WorkflowToolDescriptors`
  - likely `core-runtime`: `resource/uri/MCPUriPattern`
- **Retained SPI note**:
  - `MCPFeatureProvider` remains part of the intended SPI surface for this requirement; the simplification is about purging non-SPI concrete weight, not removing the top-level provider contract.
- **Alternatives considered**:
  - Keep the research purely principle-level and defer all concrete families to implementation: rejected because that makes the plan less reviewable and less actionable.
