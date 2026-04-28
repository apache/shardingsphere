# Contract: MCP Feature SPI Simplification

## 1. Contract goal

- `mcp/features/spi` is the stable extension boundary for MCP feature modules.
- The module must be recognizable as SPI by inspection alone.
- Non-SPI shared contracts must not remain in `mcp/features/spi`.
- Concrete runtime implementations must not remain in `mcp/features/spi`.

## 2. What stays in `mcp/features/spi`

The following kinds of production types are allowed to remain in `mcp/features/spi`:

- SPI interfaces, such as feature extension points and handler contracts
- interface-owned minimal enums
- interface-owned annotations
- interface-owned constants that cannot be cleanly separated from the interface contract
- interface-only runtime seams that expose shared MCP capabilities to features

### 2.1 Required properties of allowed SPI types

- They describe extension behavior but do not implement runtime behavior.
- They do not allocate or store workflow state.
- They do not perform registry assembly.
- They do not perform request binding or payload construction.
- They do not perform core-only URI parsing or registry validation.
- They do not expose concrete support implementations or core-private classes in public method signatures.

## 3. What must leave `mcp/features/spi`

The following kinds of production types are not SPI and must move out:

- concrete services
- workflow orchestration helpers
- request binders
- payload builders
- context stores
- registry implementations
- URI parsing utilities used only by registry/runtime
- default concrete handlers shared by multiple features
- requests, responses, descriptors, protocol DTOs, metadata DTOs, and exceptions that are compiled against by both core and feature modules

## 4. Destination rules

### 4.1 Move to `mcp/core`

Move a type to `mcp/core` when all of the following are true:

- it is a concrete implementation
- it is used only by shared MCP runtime infrastructure
- feature modules do not need to compile against it directly

Examples of likely candidates:

- registry-only utilities
- core runtime context implementations
- core-private in-memory stores
- dispatch-only validation helpers

### 4.2 Move to `mcp/api`

Move a type to `mcp/api` when all of the following are true:

- it is not an SPI interface
- it is a shared compile-time contract
- it is referenced by both core and feature modules

Examples of likely candidates:

- tool descriptors
- request / response DTOs
- metadata models
- protocol models
- shared exceptions

### 4.3 Decompose instead of creating `mcp/features/support`

When a concrete helper is reused by multiple feature modules and cannot remain in `mcp/features/spi`, do not create a new support module. Instead, split the family so that:

- shared contracts move to `mcp/api` or remain as SPI interfaces
- core-private implementations move to `mcp/core`
- feature-specific implementations move to the owning feature module

Examples of likely candidates:

- generic workflow apply helpers that can be reduced to SPI plus core implementation
- workflow validation helpers whose feature-specific logic belongs in encrypt or mask
- request binding utilities whose shared parts can become API contracts while concrete behavior becomes core-private or feature-owned

### 4.4 Move to the owning feature module

Move a type to `mcp/features/encrypt` or `mcp/features/mask` when it expresses behavior, models, or helpers that only one feature truly owns.

Examples of likely candidates:

- algorithm recommendation services
- rule inspection helpers
- feature-specific workflow state
- naming helpers
- DistSQL planning helpers

## 5. Minimal SPI surface

The stable SPI surface may continue to include interface-only contracts such as:

- `MCPFeatureProvider`, retained as the top-level feature SPI entry
- `ToolHandler`
- `ResourceHandler`
- `MCPFeatureContext`
- feature runtime facade interfaces
- capability and query facade interfaces

Rules:

- these contracts must not embed concrete behavior
- any default methods must remain trivial and non-runtime-bearing
- any parameter or return type that currently exposes a concrete helper, such as a workflow store implementation, must be replaced by an interface-only seam or neutral shared contract
- implementations of these interfaces must live outside `mcp/features/spi`

## 6. Dependency contract

The simplified end state must satisfy:

- `mcp/core -> mcp/features/spi`
- `mcp/core -> mcp/api`
- `mcp/features/encrypt -> mcp/features/spi`
- `mcp/features/encrypt -> mcp/api`
- `mcp/features/mask -> mcp/features/spi`
- `mcp/features/mask -> mcp/api`

The simplified end state must avoid:

- `mcp/features/encrypt -> mcp/core` implementation packages
- `mcp/features/mask -> mcp/core` implementation packages
- introducing `mcp/features/support`
- new concrete helper classes added back into `mcp/features/spi`

## 7. Review contract

Before implementation starts, reviewers must be able to answer, for every current production type under `mcp/features/spi`:

1. Is it a true SPI interface?
2. If not, is it a shared API contract, a core runtime implementation, or a feature-owned implementation after decomposition?
3. Does its target module avoid circular dependencies?
4. Does its target module make ownership more obvious than the current location?

If any answer is unclear, the type is not ready to move yet.
