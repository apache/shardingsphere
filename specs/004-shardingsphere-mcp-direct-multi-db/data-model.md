# Data Model: ShardingSphere MCP Direct Multi-Database Runtime

## Core Domain Entities

### DirectRuntimeTopology

- **Purpose**: Describe the complete direct-connection runtime surface loaded by one MCP service instance.
- **Fields**:
  - `logicalDatabases`
  - `startupValidationMode`
  - `serviceAvailabilityState`
  - `sharedContractVersion`
- **Validation rules**:
  - `logicalDatabases` must be keyed by unique logical database names.
  - `startupValidationMode` defaults to fail-fast behavior for V1.
  - `sharedContractVersion` must stay aligned with the MCP V1 public contract.

### LogicalDatabaseBinding

- **Purpose**: Represent one externally visible logical database and its binding to an independently connected backend database.
- **Fields**:
  - `database`
  - `databaseType`
  - `connectionTarget`
  - `defaultSchema`
  - `schemaSemantics`
  - `capabilityOverride`
  - `supportedObjectTypes`
- **Validation rules**:
  - `database` is mandatory and unique within one runtime topology.
  - `databaseType` must be part of the V1 supported database set.
  - `defaultSchema` must be explicit when the target database does not expose native schema namespaces.
  - `capabilityOverride` may narrow or clarify behavior, but must not contradict fixed V1 guarantees.

### ConnectionTarget

- **Purpose**: Describe the operator-supplied destination used to reach one backend database without exposing secrets through MCP public surfaces.
- **Fields**:
  - `endpointLocator`
  - `credentialReference`
  - `driverIdentity`
  - `connectionProperties`
- **Validation rules**:
  - `endpointLocator` and required credentials must be present before startup succeeds.
  - Public diagnostics must use a redacted form of this entity.
  - Secret material must never appear in public resources, tools, audit records, or diagnostics.

### DatabaseAvailabilityState

- **Purpose**: Track whether one logical database is successfully loaded, temporarily unavailable, or serving from its last known metadata snapshot.
- **Fields**:
  - `database`
  - `startupValidated`
  - `liveReachable`
  - `lastSuccessfulMetadataLoadAtMillis`
  - `lastAvailabilityFailureAtMillis`
  - `lastFailureReason`
  - `servingSnapshotOnly`
- **Validation rules**:
  - A database enters `startupValidated=true` only after initial metadata loading succeeds.
  - `servingSnapshotOnly=true` is allowed only after a successful startup and a later runtime failure.
  - Runtime unavailability for one database must not change availability state for unrelated databases.

### MetadataSnapshot

- **Purpose**: Capture the most recent successful discovery projection for one logical database.
- **Fields**:
  - `database`
  - `databaseType`
  - `generatedAtMillis`
  - `schemas`
  - `metadataObjects`
  - `runtimeDescriptor`
- **Validation rules**:
  - A snapshot belongs to exactly one logical database.
  - Failed refresh attempts must not replace an existing successful snapshot with empty data.
  - `runtimeDescriptor` carries only runtime facts such as version and default schema, and must not duplicate capability or resource inventory state.

### RuntimeDatabaseDescriptor

- **Purpose**: Capture one logical database's runtime facts for capability assembly and discovery behavior.
- **Fields**:
  - `databaseVersion`
  - `defaultSchema`
- **Validation rules**:
  - `databaseVersion` may be blank when the connected backend does not expose a reliable version string.
  - `defaultSchema` is derived from the live connection or discovered schemas and may be blank.
  - Runtime facts must not duplicate database capability or metadata inventory data.

### SessionRoutingContext

- **Purpose**: Represent the per-session routing and transaction state needed for direct multi-database execution.
- **Fields**:
  - `sessionId`
  - `boundDatabase`
  - `autocommit`
  - `transactionState`
  - `savepoints`
  - `closed`
- **Validation rules**:
  - `boundDatabase` is empty until the session executes or begins work on one logical database.
  - An active transaction may bind at most one logical database.
  - Closing the session rolls back pending work on the bound database and clears savepoints.

### DirectExecutionRequest

- **Purpose**: Carry one validated MCP execution request into the direct runtime routing layer.
- **Fields**:
  - `sessionId`
  - `database`
  - `databaseType`
  - `schema`
  - `sql`
  - `maxRows`
  - `timeoutMs`
  - `availabilityState`
- **Validation rules**:
  - `database` is mandatory and must match a loaded logical database binding.
  - `sql` must already satisfy MCP single-statement and unsupported-command rules.
  - Requests targeting an unavailable database must fail with the unified availability semantics before execution begins.

### DirectExecutionResult

- **Purpose**: Capture the routed backend outcome before it is normalized into the MCP public result models.
- **Fields**:
  - `database`
  - `statementClass`
  - `resultKind`
  - `columns`
  - `rows`
  - `affectedRows`
  - `statementAck`
  - `backendFailure`
  - `refreshScope`
- **Validation rules**:
  - Successful execution populates exactly one result variant.
  - `backendFailure` must map to the MCP unified error surface.
  - `refreshScope` is emitted only for committed DDL or DCL changes that affect metadata visibility.

### RefreshScope

- **Purpose**: Describe the visibility and replacement boundary of a committed metadata-affecting change.
- **Fields**:
  - `database`
  - `changeType`
  - `sessionVisibleImmediately`
  - `globalVisibleDeadlineMillis`
  - `replaceOnlyTargetSnapshot`
- **Validation rules**:
  - `database` identifies exactly one logical database.
  - `replaceOnlyTargetSnapshot` is always `true` for V1 direct multi-database runtime.
  - Global visibility continues to use the 60-second SLA window.

### RedactedDiagnosticRecord

- **Purpose**: Provide operator-visible startup or runtime diagnostics without exposing secrets.
- **Fields**:
  - `database`
  - `phase`
  - `status`
  - `message`
  - `redactedContext`
  - `suggestedAction`
- **Validation rules**:
  - `database` is mandatory for database-scoped diagnostics.
  - `redactedContext` may identify the target binding, but must exclude raw passwords and credential-bearing connection strings.
  - Diagnostics for one database must not be reported as failures of unrelated databases.

## Relationships

- One `DirectRuntimeTopology` governs many `LogicalDatabaseBinding` entries.
- One `LogicalDatabaseBinding` has one active `DatabaseAvailabilityState` and zero or one current `MetadataSnapshot`.
- One `MetadataSnapshot` contains many metadata objects for one logical database only.
- One `SessionRoutingContext` binds at most one logical database during an active transaction.
- One `DirectExecutionRequest` yields exactly one `DirectExecutionResult`.
- One successful metadata-affecting `DirectExecutionResult` emits one `RefreshScope`.
- One startup or runtime failure emits one or more `RedactedDiagnosticRecord` entries.

## State Transitions

### Logical database availability lifecycle

- `configured -> validating`: startup begins validating one logical database binding.
- `validating -> loaded`: initial metadata loading succeeds and the database becomes routable.
- `validating -> startup_failed`: required inputs, dependency checks, or initial metadata loading fail.
- `loaded -> temporarily_unavailable`: runtime connectivity or execution access fails after successful startup.
- `temporarily_unavailable -> snapshot_only`: discovery continues from the last successful snapshot while live backend access remains unavailable.
- `snapshot_only -> loaded`: the next successful refresh restores live backend access.

### Session routing lifecycle

- `unbound -> bound`: the session executes a request or begins a transaction on one logical database.
- `bound -> in_transaction`: `BEGIN` or `START TRANSACTION` succeeds.
- `in_transaction -> bound`: `COMMIT` or `ROLLBACK` succeeds.
- `bound -> unbound`: session work ends without an active transaction.
- `in_transaction -> closed`: session closes and pending work is rolled back.

## Derived Rules

- The direct multi-database feature keeps the baseline MCP public contract and changes only runtime topology and availability behavior.
- Logical database names are operator-defined route keys and do not need to match native database, catalog, or schema names.
- Startup validation is all-or-nothing for V1, but runtime unavailability after startup is isolated per logical database.
- Metadata replacement is per logical database; a failed refresh must preserve the last successful snapshot for that database.
- Secret-bearing connection details are runtime inputs only and must not appear in public MCP outputs or diagnostics.
