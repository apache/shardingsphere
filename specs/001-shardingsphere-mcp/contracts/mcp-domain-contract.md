# Contract: MCP Domain Surface

## Purpose

Define the externally visible MCP domain surface: resources, tools, result shapes, and error semantics.

## Resources

The V1 public resource set is fixed to the following paths:

- `shardingsphere://capabilities`
- `shardingsphere://databases`
- `shardingsphere://databases/{database}`
- `shardingsphere://databases/{database}/capabilities`
- `shardingsphere://databases/{database}/schemas`
- `shardingsphere://databases/{database}/schemas/{schema}`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables`
- `shardingsphere://databases/{database}/schemas/{schema}/views`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`

### Resource rules

- Resources expose stable, readable structure and capability information only.
- `shardingsphere://capabilities` returns the service-level capability surface.
- `shardingsphere://databases/{database}/capabilities` returns the database-level capability view.
- When `index` is not supported for a database, index resources return `unsupported`.
- When `sequence` is not supported for a database, sequence resources return `unsupported`.

## Tools

The V1 public tool set is fixed to:

- `search_metadata(database?, schema?, query, object_types?, page_size?, page_token?)`
- `execute_query(database, schema?, sql, max_rows?, timeout_ms?)`

### Tool rules

- `execute_query` accepts one statement only.
- `database` is the only strong execution boundary for `execute_query`.
- `schema` is an optional namespace hint for unqualified object names; its execution meaning is guided by database capability `schemaExecutionSemantics`.
- SQL explicit qualification takes precedence over request-level `schema`.
- `search_metadata` may search all loaded logical databases when `database` is omitted.
- `search_metadata.object_types` accepts only `database`, `schema`, `table`, `view`, `column`, `index`, and `sequence`; other values return `invalid_request`.
- If `schema` is provided without `database`, the request returns `invalid_request`.
- Metadata list/detail/capability discovery is exposed through `resources/read` rather than dedicated metadata tools.
- Tools honor capability boundaries and request validation before execution.

## Result Models

### `result_set`

- Used when execution returns a result set.
- Must include:
  - `result_kind`
  - `statement_class`
  - `statement_type`
  - `status`
  - `columns`
  - `rows`
  - `truncated`

### `update_count`

- Used when execution returns an update count.
- Must include:
  - `result_kind`
  - `statement_class`
  - `statement_type`
  - `status`
  - `affected_rows`
  - `truncated`

### `statement_ack`

- Used for DDL, DCL, transaction-control statements, and other non-result statements.
- Must include:
  - `result_kind`
  - `statement_class`
  - `statement_type`
  - `status`
  - `message`
  - `truncated`

### Result invariants

- Each `execute_query` call returns exactly one result object.
- Multi-result-set behavior is out of scope for V1.
- Result fields must normalize column metadata, null handling, complex values, timestamps, and truncation semantics.
- `statement_class` expresses side effects and governance semantics.
- `statement_type` expresses the primary user-visible statement type.
- `statement_class = dml` may coexist with `result_kind = result_set`.

## Error Surface

The unified domain error codes are fixed to:

- `invalid_request`
- `not_found`
- `unsupported`
- `conflict`
- `timeout`
- `unavailable`
- `transaction_state_error`
- `query_failed`

### Error rules

- Unsupported statement classes, optional objects, or transaction features return `unsupported`.
- Transaction misuse returns `transaction_state_error` or `conflict` depending on the state violation.
- Database-native errors must be mapped into this domain set rather than leaked directly.
- `timeout` and `unavailable` remain reserved unified codes for future timeout or backend-availability mapping.

## Capability Semantics

- Service capability describes protocol-wide support only.
- Database capability describes database-specific behavior and object/statement boundaries.
