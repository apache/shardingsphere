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

## Tools

The V1 public tool set is fixed to:

- `list_databases()`
- `list_schemas(database)`
- `list_tables(database, schema, search?, page_size?, page_token?)`
- `list_views(database, schema, search?, page_size?, page_token?)`
- `list_columns(database, schema, object_type, object_name, search?, page_size?, page_token?)`
- `list_indexes(database, schema, table, search?, page_size?, page_token?)`
- `search_metadata(database?, schema?, query, object_types?, page_size?, page_token?)`
- `describe_table(database, schema, table)`
- `describe_view(database, schema, view)`
- `get_capabilities(database?)`
- `execute_query(database, sql, max_rows?, timeout_ms?)`

### Tool rules

- `object_type` is limited to `table` or `view`.
- `execute_query` accepts one statement only.
- `search_metadata` may search all loaded logical databases when `database` is omitted.
- If `schema` is provided without `database`, the request returns `invalid_request`.
- Tools honor capability boundaries and request validation before execution.

## Result Models

### `result_set`

- Used for query-style statements.
- Must include:
  - `result_kind`
  - `columns`
  - `rows`
  - `truncated`

### `update_count`

- Used for DML statements.
- Must include:
  - `result_kind`
  - `affected_rows`

### `statement_ack`

- Used for DDL, DCL, transaction-control statements, and other non-result statements.
- Must include:
  - `result_kind`
  - `statement_type`
  - `status`
  - `message`

### Result invariants

- Each `execute_query` call returns exactly one result object.
- Multi-result-set behavior is out of scope for V1.
- Result fields must normalize column metadata, null handling, complex values, timestamps, and truncation semantics.

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
