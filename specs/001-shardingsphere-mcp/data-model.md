# Data Model: ShardingSphere MCP V1

## Core Domain Entities

### ServiceCapability

- **Purpose**: Describe the protocol-wide capability surface that is independent of any single logical database.
- **Fields**:
  - `supportedResources`
  - `supportedTools`
  - `supportedStatementClasses`
- **Validation rules**:
  - Must not contain database-specific transaction or object-type differences.
  - Must stay consistent with the public resources and tools registered by `mcp/bootstrap`.

### DatabaseCapability

- **Purpose**: Describe database-specific behavior and boundaries for one logical database.
- **Fields**:
  - `database`
  - `databaseType`
  - `supportedObjectTypes`
  - `supportedStatementClasses`
  - `supportsTransactionControl`
  - `supportsSavepoint`
  - `defaultSchemaSemantics`
  - `schemaExecutionSemantics`
  - `supportsCrossSchemaSql`
  - `supportsExplainAnalyze`
- **Validation rules**:
  - Must be assembled in fixed order: transaction matrix, runtime metadata, deployment overrides.
  - `index` appears only when `supportedObjectTypes` explicitly includes it.
  - Transaction and savepoint flags must match the published V1 matrix.
  - `defaultSchemaSemantics` describes metadata/discovery semantics; `schemaExecutionSemantics` describes `execute_query.schema` semantics.

### SessionContext

- **Purpose**: Represent the lifecycle and runtime state of one MCP session.
- **Fields**:
  - `sessionId`
  - `boundDatabase`
  - `autocommit`
  - `transactionState`
  - `savepoints`
  - `transactionStartedAtMillis`
  - `closed`
- **Validation rules**:
  - `boundDatabase` is empty until a request binds the session to a database.
  - Valid transaction states are `IDLE` and `ACTIVE`.
  - Closing a session rolls back pending work and clears savepoints.
  - Session recovery is not supported after close.

### MetadataObject

- **Purpose**: Provide a normalized metadata projection for discovery and resource reads.
- **Common fields**:
  - `database`
  - `schema`
  - `objectType`
  - `name`
  - `parentObjectType`
  - `parentObjectName`
- **Specializations**:
  - `DatabaseInfo`
  - `SchemaInfo`
  - `TableInfo`
  - `ViewInfo`
  - `ColumnInfo`
  - `IndexInfo`
- **Validation rules**:
  - `table` and `view` are unique by `database + schema + name`.
  - `column` and `index` must carry parent object references.
  - `index` visibility depends on the database capability declaring `INDEX` support.

### ExecuteQueryRequest

- **Purpose**: Capture one MCP SQL execution request.
- **Fields**:
  - `sessionId`
  - `database`
  - `databaseType`
  - `schema`
  - `sql`
  - `maxRows`
  - `timeoutMs`
  - `nowMillis`
- **Validation rules**:
  - `sql` must contain exactly one statement.
  - `database` is mandatory.
  - `schema` is optional and acts as a namespace hint for unqualified object names.
  - `database` remains the only strong execution boundary.
  - Statement class is derived during validation, not provided by the caller.

### ExecutionResult

- **Purpose**: Provide a single normalized result object for each `execute_query` call.
- **Variants**:
  - `ResultSetResult`
  - `UpdateCountResult`
  - `StatementAckResult`
- **Shared fields**:
  - `resultKind`
- **Variant fields**:
  - `ResultSetResult`: `columns`, `rows`, `truncated`
  - `UpdateCountResult`: `affectedRows`
  - `StatementAckResult`: `statementType`, `status`, `message`
- **Validation rules**:
  - Only one variant may be returned for one request.
  - Result shape must align with statement class and effective capability behavior.

### AuditRecord

- **Purpose**: Capture every resource read, metadata tool call, and SQL execution event.
- **Fields**:
  - `sessionId`
  - `database`
  - `operationClass`
  - `operationDigest`
  - `successOrFailure`
  - `errorCode`
  - `transactionMarker`
  - `timestamp`
- **Validation rules**:
  - `operationClass` is limited to `resource_read`, `metadata_tool`, or `query_execution`.
  - SQL audit records must store digest, not full raw statements, when digesting rules apply.

### RefreshVisibilityState

- **Purpose**: Track committed structure and DCL changes for session-local and global visibility.
- **Fields**:
  - `globalRefreshTimes`
  - `sessionVisibleDatabases`
- **Validation rules**:
  - Structure and DCL commits mark the current session as immediately visible for the target database.
  - Global visibility timestamps use a 60-second SLA window.
  - Clearing a session removes only session-local visibility state.

### TransactionMatrixEntry

- **Purpose**: Provide the canonical V1 default behavior per supported database type.
- **Fields**:
  - `databaseType`
  - `supportsTransactionControl`
  - `supportsSavepoint`
  - `supportedObjectTypes`
  - `supportedStatementClasses`
  - `supportsExplainAnalyze`
- **Validation rules**:
  - Acts as the first input to database capability assembly.
  - Must remain consistent with published capability responses and E2E expectations.

## Relationships

- One `ServiceCapability` governs many `DatabaseCapability` projections.
- One `SessionContext` tracks one current transaction state and zero or many savepoint names.
- One `DatabaseCapability` constrains many `MetadataObject` and `ExecuteQueryRequest` evaluations.
- One `ExecuteQueryRequest` yields exactly one `ExecutionResult` and one `AuditRecord`.
- One `RefreshVisibilityState` tracks visibility timestamps per database and per session.

## State Transitions

### Session lifecycle

- `created -> open`: successful initialize flow.
- `open -> in_transaction`: `BEGIN` or `START TRANSACTION`.
- `in_transaction -> open`: `COMMIT` or `ROLLBACK`.
- `open -> closed`: explicit `DELETE /mcp` or transport shutdown.
- `in_transaction -> closed`: disconnect with rollback.

### Transaction state

- `IDLE -> ACTIVE`: explicit transaction begins on a database that supports transaction control.
- `ACTIVE -> IDLE`: `COMMIT` or `ROLLBACK` succeeds.

## Derived Rules

- Capability assembly order is fixed: matrix defaults, runtime metadata, deployment overrides.
- Savepoint availability depends on database capability and current transaction state.
- Unsupported optional objects must return `unsupported`; they must not silently disappear as empty successful results.
- Transport-level session failures use HTTP status semantics; domain-level failures map to MCP error codes.
- The built-in runtime models session, capability, audit, and refresh-visibility semantics only.
