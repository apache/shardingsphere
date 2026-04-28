# Quickstart: MCP Feature SPI Simplification

## 1. Goal

This quickstart is the acceptance walk-through for a simplified SPI boundary.

If these checks pass, `mcp/features/spi` is no longer an overloaded shared module and the requirement is satisfied.

## 2. Expected module roles

```text
mcp
|-- api                  # shared non-SPI contracts
|-- core                 # core-private runtime implementations
|-- bootstrap
`-- features
    |-- spi              # pure SPI interfaces only
    |-- encrypt
    `-- mask
```

Notes:

- `mcp/api` is required for shared non-SPI contracts used by both core and feature modules.
- `mcp/features/support` must not be introduced for this requirement.

## 3. Expected dependency directions

- `mcp/core -> mcp/features/spi`
- `mcp/core -> mcp/api`
- `mcp/features/encrypt -> mcp/features/spi`
- `mcp/features/encrypt -> mcp/api`
- `mcp/features/mask -> mcp/features/spi`
- `mcp/features/mask -> mcp/api`

Anti-patterns:

- `mcp/features/encrypt -> mcp/core` implementation packages
- `mcp/features/mask -> mcp/core` implementation packages
- introducing `mcp/features/support`
- new concrete helper classes added back into `mcp/features/spi`

## 4. SPI module acceptance sample

### 4.1 What should remain in `mcp/features/spi`

- handler interfaces
- feature runtime facade interfaces
- capability/query facade interfaces
- minimal enums/annotations/constants directly owned by those interfaces

### 4.2 What should not remain in `mcp/features/spi`

- workflow execution services
- workflow planning services
- workflow validation services
- request binders
- payload builders
- context store implementations
- registry implementations
- URI parsing helpers
- descriptor DTO implementations
- request/response DTOs
- protocol response types
- SPI interfaces whose method signatures expose concrete helper implementations

### 4.3 Reviewer check

Open `mcp/features/spi/src/main/java`.

Expected result:

- the module reads like a pure contract layer
- there are no concrete helper packages pretending to be SPI

## 5. Shared API acceptance sample

If a type is compiled against by both core and feature modules and is not itself an SPI interface, it belongs in `mcp/api`.

Typical examples:

- tool descriptors
- request / response DTOs
- metadata models
- protocol models
- shared exceptions

Expected result:

- these types are no longer in `mcp/features/spi`
- features do not need core internals to compile against them

## 6. Core runtime acceptance sample

If a concrete helper is used only by shared runtime infrastructure, it belongs in `mcp/core`.

Typical examples:

- registry-only utilities
- core-owned URI parser
- core runtime context implementation
- core-owned in-memory store implementation

Expected result:

- these classes are no longer in `mcp/features/spi`
- feature modules do not import them directly

## 7. Shared concrete helper decomposition sample

If a concrete helper is actually reused by multiple feature modules, it still must not cause a new `mcp/features/support` module to appear.

Typical candidates:

- generic feature-side workflow apply helper
- generic feature-side validation helper
- generic feature-side payload helper

Expected result:

- shared contracts are extracted into `mcp/api` or SPI
- core-private implementations live in `mcp/core`
- feature-specific implementations live in `mcp/features/encrypt` or `mcp/features/mask`
- `mcp/features/support` does not exist

## 8. Feature module acceptance sample

### Encrypt

Expected result:

- encrypt-specific workflow helpers live in `mcp/features/encrypt`
- encrypt depends on pure SPI and shared API only
- encrypt does not depend on core implementation packages

### Mask

Expected result:

- mask-specific workflow helpers live in `mcp/features/mask`
- mask depends on pure SPI and shared API only
- mask does not depend on core implementation packages

## 9. Scope guard

This quickstart should pass without requiring any of the following:

- redesigning external tool names
- redesigning resource URIs
- changing encrypt or mask business behavior
- switching branches during the requirement analysis

If the work starts drifting into those areas, the simplification has left its intended scope.
