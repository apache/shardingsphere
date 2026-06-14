# Feature Spec: MCP DistSQL-backed Governance Metadata Resources

## Background

MCP users need to inspect ShardingSphere Proxy governance metadata before planning or validating rule changes.
Existing MCP resources cover logical database metadata such as databases, schemas, tables, columns, indexes and sequences.
They do not expose several Proxy-visible governance objects that existing workflows already require.

The first reported failing scenario is "cannot obtain data source information".
In ShardingSphere Proxy terminology, the MCP-safe target is storage unit metadata.
It is not raw physical datasource credentials or unrestricted JDBC connection probing.
Proxy already provides dedicated DistSQL for these governance queries.
Therefore this specification limits the scope to exposing existing DistSQL-backed read-only metadata through MCP resources and search capabilities.

The implementation must not add new DistSQL grammar, must not expose raw `SHOW` passthrough, and must not maintain MCP-only knowledge for capabilities that cannot be backed by existing DistSQL.

Relevant existing DistSQL documentation:

- `SHOW STORAGE UNITS [FROM databaseName] [LIKE pattern]`: `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/storage-unit-query/show-storage-units.en.md`
- `SHOW RULES USED STORAGE UNIT storageUnitName [FROM databaseName]`: `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-rules-used-storage-unit.en.md`
- `SHOW SINGLE TABLE tableName [FROM databaseName]` and `SHOW SINGLE TABLES [LIKE pattern] [FROM databaseName]`:
  `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/single-table/show-single-table.en.md`
- `SHOW DEFAULT SINGLE TABLE STORAGE UNIT [FROM databaseName]`:
  `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/single-table/show-deafult-single-table-storage-unit.en.md`

Current MCP evidence:

- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml` exposes logical metadata search types but not `storage_unit`.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/SQLStatementSafetyValidator.java` rejects metadata introspection SQL such as `SHOW`.
  Users cannot safely work around missing resources through `database_gateway_execute_query`.
- Rule workflow descriptors and services already use storage unit fields for readwrite-splitting, shadow and sharding planning.
  MCP cannot currently discover or validate those names through a first-class resource.

This spec is created on the current branch. No branch switch is required or allowed for this requirement work.

## User Scenarios

### Scenario 1

An MCP client prepares a readwrite-splitting rule and needs to choose existing write and read storage units.
The client reads the database storage unit resource, sees available storage unit names and connection metadata without secrets, and uses those names in the rule workflow.

### Scenario 2

An MCP client investigates whether a storage unit can be removed, renamed, or reused.
The client reads the storage unit used-by resource and receives the rule types and rule names that currently reference that storage unit.

### Scenario 3

An MCP client answers a user question asking which storage unit a single table is located on.
The client reads the single table resource and receives the table name and `storage_unit_name` from DistSQL.

### Scenario 4

An MCP client checks the default storage unit for new single tables in a logical database.
The client reads the default single table storage unit resource and receives the configured storage unit name.

### Scenario 5

A user asks MCP to execute `SHOW STORAGE UNITS` through the SQL query tool.
The SQL safety validator continues to reject metadata introspection SQL.
The recovery guidance points the client to the storage unit MCP resources instead of recommending raw SQL execution.

## Functional Requirements

- FR-001: MCP MUST expose a read-only storage unit list resource for a logical database backed by `SHOW STORAGE UNITS FROM databaseName`.
- FR-002: The storage unit list capability MUST support name filtering backed by the existing `LIKE` clause or by exact MCP-side filtering over the DistSQL result.
- FR-003: MCP MUST expose a read-only storage unit detail resource for a logical database and storage unit name.
  Detail lookup MUST be backed by `SHOW STORAGE UNITS` and MUST return no result or a typed not-found error when no exact storage unit matches.
- FR-004: MCP metadata search MUST include `storage_unit` as a supported object type and return storage unit matches from the DistSQL-backed storage unit metadata.
- FR-005: MCP MUST expose a read-only storage unit used-by resource backed by `SHOW RULES USED STORAGE UNIT storageUnitName FROM databaseName`.
- FR-006: The storage unit used-by resource MUST return rule `type` and `name` as provided by DistSQL and MUST preserve duplicate rows when DistSQL returns duplicates.
- FR-007: MCP MUST expose a read-only single table list resource backed by `SHOW SINGLE TABLES FROM databaseName`.
- FR-008: MCP MUST expose a read-only single table detail resource backed by `SHOW SINGLE TABLE tableName FROM databaseName`.
- FR-009: The single table resources MUST expose `table_name` and `storage_unit_name` from DistSQL so clients can answer table-to-storage-unit questions.
- FR-010: MCP MUST expose a read-only default single table storage unit resource backed by `SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM databaseName`.
- FR-011: Rule planning and validation guidance for readwrite-splitting, shadow and sharding workflows SHOULD include the new storage unit resources in `resources_to_read`.
  This applies when storage unit names are missing, ambiguous, or need validation.
- FR-012: Metadata introspection SQL recovery guidance SHOULD recommend the relevant MCP resource when a rejected SQL statement maps to one of the supported DistSQL-backed resources in this spec.
- FR-013: MCP MUST NOT expose a general-purpose `SHOW` execution tool as part of this feature.
- FR-014: MCP MUST NOT introduce new DistSQL syntax, SQL parser grammar, or Proxy behavior for this feature.
- FR-015: MCP MUST NOT expose physical datasource passwords, raw credentials, tokens, or secret connection properties. Any sensitive fields in storage unit metadata MUST be redacted or omitted.
- FR-016: MCP MUST preserve the distinction between Proxy storage units and physical datasource inspection. This feature exposes Proxy-visible governance metadata only.

## Non-Functional Requirements

- NFR-001: This spec and follow-up implementation MUST stay on the current branch; the agent or contributor MUST NOT switch or create Git branches for this task unless explicitly instructed later.
- NFR-002: The implementation MUST keep `database_gateway_execute_query` restricted to its existing safe SQL contract and MUST NOT relax SQL safety validation for metadata introspection.
- NFR-003: The implementation MUST use existing MCP query facade or equivalent existing Proxy DistSQL query infrastructure instead of opening a new direct physical datasource connection path.
- NFR-004: Resource payloads MUST be stable and machine-readable, using field names that match DistSQL output where practical.
- NFR-005: Errors MUST be actionable. Missing database, missing storage unit, unsupported DistSQL on older Proxy targets, and query failure MUST be distinguishable.
- NFR-006: Tests for new public production types or handlers MUST follow repository testing rules and use mocks for query facade interactions.
- NFR-007: Documentation or descriptors added for this feature MUST describe supported resources, backing DistSQL, non-goals and sensitive-field handling.

## Acceptance Criteria

- AC-001: Given a logical database with registered storage units, reading `shardingsphere://databases/{database}/storage-units` returns rows backed by `SHOW STORAGE UNITS FROM {database}`.
- AC-002: Given a storage unit name that exists, reading `shardingsphere://databases/{database}/storage-units/{storageUnit}` returns exactly that storage unit's metadata.
- AC-003: Given a storage unit name that does not exist, the storage unit detail resource returns a typed not-found response or an empty result according to existing MCP resource conventions.
- AC-004: Given `database_gateway_search_metadata` with object type `storage_unit`, the result includes matching storage units and does not include unrelated logical table or column objects.
- AC-005: Given a storage unit used by rules, reading `shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules` returns rows from `SHOW RULES USED STORAGE UNIT`.
  The payload includes the `type` and `name` columns.
- AC-006: Given single tables in a logical database, reading `shardingsphere://databases/{database}/single-tables` returns table-to-storage-unit mappings from `SHOW SINGLE TABLES`.
- AC-007: Given a single table name, reading `shardingsphere://databases/{database}/single-tables/{table}` returns the table's `storage_unit_name` from `SHOW SINGLE TABLE`.
- AC-008: Given a configured default single table storage unit, reading `shardingsphere://databases/{database}/single-table/default-storage-unit` returns `storage_unit_name`.
- AC-009: Given a rejected metadata introspection SQL such as `SHOW STORAGE UNITS`, MCP recovery guidance points to storage unit resources instead of suggesting raw SQL execution.
- AC-010: Given rule workflow planning with missing storage unit names, the response includes the storage unit list resource in `resources_to_read`.
- AC-011: Given storage unit metadata containing sensitive connection properties, returned MCP payloads redact or omit those sensitive values.
- AC-012: Unit tests cover successful query, empty result, query failure, not-found handling, sensitive-field redaction, metadata search integration and recovery guidance for the new resource family.

## Edge Cases

- DistSQL returns no rows for a database that exists: MCP returns an empty resource payload and does not treat the database as missing.
- DistSQL is unsupported by an older Proxy target: MCP returns an explicit unsupported-capability error or fallback response consistent with existing workflow query behavior.
- No database is selected in the backend query context: MCP MUST use the database path parameter and include `FROM databaseName` in backing DistSQL.
- Storage unit names require identifier quoting: implementation MUST use the repository's existing identifier quoting or DistSQL construction utilities instead of unsafe string concatenation.
- `SHOW RULES USED STORAGE UNIT` returns duplicate rows: MCP preserves the DistSQL result unless an existing resource convention explicitly requires de-duplication.
- A storage unit has secret-like keys in `other_attributes`: MCP redacts or omits the sensitive keys while preserving non-sensitive attributes.
- A table exists as logical metadata but is not a single table: the single table detail resource returns not-found or empty result rather than inferring a storage unit from unrelated metadata.
- The SQL query tool receives `SHOW SINGLE TABLES` or `SHOW DEFAULT SINGLE TABLE STORAGE UNIT`.
  The SQL remains rejected as metadata introspection, and recovery guidance should point to the corresponding MCP resource.

## Assumptions

- The target runtime is ShardingSphere Proxy or a Proxy-compatible MCP query facade that can execute DistSQL against logical databases.
- Existing DistSQL output columns are the source of truth for resource field names unless MCP has an established naming convention that requires normalized aliases.
- This feature is read-only. Creating, altering, registering, unregistering, loading, unloading, or refreshing storage units and single tables is outside the scope.
- Existing logical metadata resources remain unchanged except for search and recovery guidance that need to point to the new governance metadata resources.
- If a backing DistSQL has database-optional syntax, MCP resources still use explicit database path parameters to avoid relying on connection state.

## Out of Scope

- New DistSQL grammar, parser changes, or Proxy-side query semantics.
- A general-purpose DistSQL or `SHOW` passthrough tool.
- Structured algorithm property templates that are not directly returned by existing DistSQL.
- Physical datasource probing, direct JDBC metadata inspection of backend databases, credential display, or connection health checks.
- Write operations such as `REGISTER STORAGE UNIT`, `UNREGISTER STORAGE UNIT`, `LOAD SINGLE TABLE`, `UNLOAD SINGLE TABLE`, or `SET DEFAULT SINGLE TABLE STORAGE UNIT`.
- Migration source storage units, unless a future requirement explicitly scopes migration MCP capabilities.

## Key Entities

- Storage Unit: Proxy-visible logical storage unit returned by `SHOW STORAGE UNITS`.
- Storage Unit Usage: Rule type and rule name rows returned by `SHOW RULES USED STORAGE UNIT`.
- Single Table Mapping: Table name and storage unit name rows returned by `SHOW SINGLE TABLE` or `SHOW SINGLE TABLES`.
- Default Single Table Storage Unit: The storage unit name returned by `SHOW DEFAULT SINGLE TABLE STORAGE UNIT`.
- Metadata Search Object: A discoverable MCP search object type that lets clients find supported metadata without issuing SQL.
