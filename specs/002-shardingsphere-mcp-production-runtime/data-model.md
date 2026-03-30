# Data Model: ShardingSphere MCP Production Runtime Integration

## Core Domain Entities

### ProductionRuntimeProviderConfig

- **Purpose**: Describe how the packaged MCP runtime locates and initializes a real ShardingSphere-backed provider.
- **Fields**:
  - `providerType`
  - `modeType`
  - `properties`
  - `requiredDependencies`
  - `databaseScope`
  - `startupValidationMode`
- **Validation rules**:
  - `providerType` is mandatory in production launch mode.
  - `properties` must contain all provider-specific connection or lookup inputs.
  - Missing required configuration must fail startup before the HTTP endpoint is published.
  - `startupValidationMode` defaults to fail-fast behavior.

### RuntimeProviderHandle

- **Purpose**: Represent the opened production-runtime integration used by bootstrap after validation succeeds.
- **Fields**:
  - `providerId`
  - `metadataProvider`
  - `executionAdapter`
  - `refreshBridge`
  - `diagnostics`
  - `opened`
- **Validation rules**:
  - A handle is created only after configuration and dependency validation succeed.
  - `metadataProvider` and `executionAdapter` must both be present for production mode.
  - Closing the handle releases provider-owned resources and prevents further dispatch.

### RuntimeMetadataSnapshot

- **Purpose**: Capture the metadata facts projected from the real ShardingSphere runtime into MCP discovery structures.
- **Fields**:
  - `generatedAtMillis`
  - `databases`
  - `schemas`
  - `objects`
  - `databaseTypes`
  - `sourceVersion`
- **Validation rules**:
  - `databases` must reflect the visible logical databases in the selected runtime scope.
  - Metadata read failures must surface as explicit errors instead of silently degrading to empty snapshots.
  - `sourceVersion` or equivalent freshness marker must be available for refresh reasoning when the provider supports it.

### RuntimeDatabaseDescriptor

- **Purpose**: Capture one logical database's runtime facts that complement capability assembly and discovery responses without duplicating them.
- **Fields**:
  - `databaseVersion`
- **Validation rules**:
  - `databaseVersion` may be blank when the backend cannot expose a reliable product version.
  - Runtime facts must not duplicate database capability matrices or metadata object inventories.

### CapabilityAssemblyInput

- **Purpose**: Provide the deterministic input set used to build the database-level capability response.
- **Fields**:
  - `transactionMatrixEntry`
  - `runtimeDatabaseDescriptor`
  - `deploymentOverride`
- **Validation rules**:
  - Assembly order is fixed and may not be reordered by implementation shortcuts.
  - Missing runtime descriptor for an otherwise visible database is an error, not an implicit empty capability.
  - Deployment overrides may narrow or clarify behavior, but must remain reviewable and explicit.

### ExecutionAdapterRequest

- **Purpose**: Carry one validated MCP execution request from the transport/core boundary into the real execution adapter.
- **Fields**:
  - `sessionId`
  - `database`
  - `schema`
  - `sql`
  - `maxRows`
  - `timeoutMs`
  - `autocommit`
  - `transactionState`
  - `savepoints`
- **Validation rules**:
  - `sql` must already satisfy MCP single-statement and unsupported-command rules before reaching the adapter.
  - `database` is mandatory.
  - Cross-database execution during an active transaction is rejected before backend dispatch.

### ExecutionAdapterResult

- **Purpose**: Carry the backend execution outcome before it is normalized into the public MCP result models.
- **Fields**:
  - `statementClass`
  - `resultKind`
  - `columns`
  - `rows`
  - `affectedRows`
  - `statementAck`
  - `backendError`
  - `refreshHint`
- **Validation rules**:
  - Only one result variant is populated for a successful execution.
  - `backendError` must be mappable into the MCP unified error surface.
  - `refreshHint` is emitted for committed DDL and DCL events that affect metadata visibility.

### RefreshVisibilityEvent

- **Purpose**: Describe a committed metadata-affecting change that must update current-session and global visibility windows.
- **Fields**:
  - `database`
  - `changeType`
  - `committedAtMillis`
  - `sessionVisibleImmediately`
  - `globalVisibleDeadlineMillis`
- **Validation rules**:
  - DDL and DCL commits mark the current session as immediately visible.
  - Global visibility must be satisfied within the 60-second SLA window.
  - Session-local visibility is cleared when the session is closed.

### ProductionLaunchDiagnostics

- **Purpose**: Capture operator-visible startup validation output for production mode.
- **Fields**:
  - `phase`
  - `status`
  - `message`
  - `missingDependencies`
  - `failingProperties`
  - `suggestedAction`
- **Validation rules**:
  - Diagnostics must be emitted for provider configuration errors, missing drivers, unreachable metadata sources, and unsupported topologies.
  - Diagnostics must be concrete enough for operators to fix configuration without inspecting source code.

## Relationships

- One `ProductionRuntimeProviderConfig` produces zero or one `RuntimeProviderHandle`.
- One `RuntimeProviderHandle` serves one `RuntimeMetadataSnapshot` stream and one real `executionAdapter`.
- One `RuntimeMetadataSnapshot` contains many `RuntimeDatabaseDescriptor` entries.
- One `CapabilityAssemblyInput` combines one matrix entry, one runtime descriptor, and at most one deployment override.
- One `ExecutionAdapterRequest` yields exactly one `ExecutionAdapterResult`.
- One committed `ExecutionAdapterResult` may emit one `RefreshVisibilityEvent`.
- One failed startup emits one or more `ProductionLaunchDiagnostics` records.

## State Transitions

### Production launch lifecycle

- `configured -> validating`: bootstrap reads provider configuration.
- `validating -> opened`: provider validation succeeds and runtime handle is created.
- `validating -> failed`: required configuration, dependencies, or runtime reachability checks fail.
- `opened -> serving`: HTTP endpoint is published only after the provider handle is ready.
- `serving -> closed`: process shutdown or explicit runtime close releases provider resources.

### Metadata visibility lifecycle

- `stale -> session_visible`: committed DDL or DCL becomes immediately visible to the current session.
- `session_visible -> globally_visible`: background or inline refresh completes within the SLA window.
- `session_visible -> cleared`: session closes before global visibility state is needed locally.

## Derived Rules

- `001-shardingsphere-mcp` remains the baseline for transport, session, and public MCP domain contracts.
- Production mode is successful only when both metadata discovery and real execution wiring are ready.
- Optional object types continue to follow capability gating; unsupported optional objects return `unsupported`.
- Production startup errors are operational errors, not silent capability degradations.
- Provider-specific runtime details may exist internally, but all MCP-visible behavior must still map to the unified domain surface.
