# Data Model: MCP Feature SPI Simplification

## 1. Modeling goal

- The model describes ownership and classification rules for types currently living in `mcp/features/spi`.
- The model must make it easy to reason about where a class belongs after the simplification.
- The model must prevent "move it to core for convenience" from becoming the default answer for every non-SPI type.

## 2. Core entities

### 2.1 Type Family

A logical family of related production types currently grouped under one package or responsibility in `mcp/features/spi`.

- `name`: family name, for example `tool descriptor`, `workflow execution helper`, `metadata model`
- `currentPackage`: current package path under `mcp/features/spi`
- `currentRole`: why the family exists today
- `consumers`: modules that compile against the family

**Validation rules**

- `name` must be non-empty
- `currentPackage` must identify one current ownership location
- `consumers` must be explicit enough to decide whether the family is core-private, shared contract, feature-owned, or needs decomposition before final placement

### 2.2 Classification Record

The explicit decision that assigns one type family to its final ownership category.

- `typeFamily`: the family being classified
- `category`: one of `pure-spi`, `shared-api`, `feature-support`, `core-runtime`, `feature-owned`
- `targetModule`: final module destination
- `reason`: short ownership rationale

**Validation rules**

- each `typeFamily` must have exactly one classification record
- `category` and `targetModule` must agree
- `reason` must explain why other categories were rejected

### 2.3 Pure SPI Contract

A type family that remains in `mcp/features/spi`.

- `contractName`
- `interfaceSet`
- `minimalSupportingTypes`

**Validation rules**

- all members are interfaces or minimal interface-owned supporting types
- the family contains no concrete runtime implementation

### 2.4 Shared API Contract

A non-SPI compile-time contract shared by core and feature modules.

- `contractName`
- `exposedFieldsOrMethods`
- `consumingModules`

**Validation rules**

- it is not a concrete runtime implementation
- it is referenced by both core and feature modules
- it belongs in a neutral shared module rather than `mcp/features/spi`

### 2.5 Shared Concrete Family

A current concrete helper family reused by multiple feature modules that cannot remain as-is.

- `familyName`
- `featureConsumers`
- `sharedBehavior`
- `decompositionPlan`

**Validation rules**

- it is concrete code, not just a contract
- it is truly shared by multiple feature modules
- it must be decomposed until each resulting type becomes shared API, core runtime, or feature-owned

### 2.6 Core Runtime Component

A concrete implementation owned only by shared MCP runtime infrastructure.

- `componentName`
- `runtimeResponsibility`
- `coreConsumers`

**Validation rules**

- feature modules do not compile against it directly
- it may live in `mcp/core`

### 2.7 Feature-Owned Component

A concrete type family that belongs to one feature only.

- `featureName`
- `componentName`
- `responsibility`

**Validation rules**

- exactly one feature owns the family
- the family should not remain in `mcp/features/spi`

## 3. Relationships

- One `Type Family` has exactly one `Classification Record`.
- A `Classification Record` maps a type family to one of:
  - `Pure SPI Contract`
  - `Shared API Contract`
  - `Shared Concrete Family`
  - `Core Runtime Component`
  - `Feature-Owned Component`

## 4. Classification flow

1. Determine whether the type family is an SPI contract.
2. If not, determine whether it is a shared compile-time contract.
3. If not, determine whether it is a shared concrete helper used by multiple features and therefore needs decomposition.
4. If not, determine whether it is core-private runtime code.
5. Otherwise, treat it as feature-owned.

This flow exists to prevent convenience-based placement.

## 5. Initial type-family examples

### 5.1 Likely `pure-spi`

- `capability/*`
- `context/MCPFeatureContext` after removing any concrete implementation types from its signatures
- `feature/spi/*`
- `tool/handler/ToolHandler`
- `resource/handler/ResourceHandler`

### 5.2 Likely `shared-api`

- `metadata/model/*`
- `metadata/jdbc/RuntimeDatabaseProfile`
- `protocol/*`, `protocol/error/*`, `protocol/exception/*`, `protocol/response/*`
- `tool/descriptor/MCPToolDescriptor`, `MCPToolFieldDefinition`, `MCPToolValueDefinition`
- `tool/request/*`
- `tool/response/*`
- `resource/uri/MCPUriVariables`

### 5.3 Likely decomposition candidates

- `tool/service/workflow/*`
- `tool/model/workflow/*`
- `tool/handler/workflow/*`
- `tool/descriptor/WorkflowToolDescriptors`

### 5.4 Likely `core-runtime`

- `resource/uri/MCPUriPattern`
- any registry assembly helper that is used only by core registries
- any workflow store implementation that ends up owned only by shared runtime after classification

### 5.5 Likely `feature-owned`

- encrypt-specific workflow state and helpers after shared families are reduced
- mask-specific workflow state and helpers after shared families are reduced
- feature-specific recommendation or inspection logic
