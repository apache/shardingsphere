+++
title = "Capability Catalog"
weight = 2
+++

This page explains the information that ShardingSphere-MCP can read, the actions it can perform, and the usage boundaries for different connection targets.
Clients discover the capabilities currently available from the MCP Server automatically. Users usually only need to describe the database task they want to complete.

## Capability discovery

Capability discovery lets clients confirm which databases the current MCP Server can access, which tasks it supports, and which tasks may have side effects.

| Discovery content | Purpose | User-facing result |
| --- | --- | --- |
| Basic capability list | Confirms readable information and executable actions from the current MCP Server. | The client can determine whether metadata inspection, SQL query, or governance changes are supported. |
| ShardingSphere capability summary | Summarizes runtime databases, connection targets, feature plugins, and side-effect boundaries. | Users can ask, "Which governance tasks does this logical database support?" |
| Database capability summary | Confirms SQL, transaction, schema, and metadata-object capabilities for one runtime database. | Users can ask, "Does this database support read-only queries and rule planning?" |

Capability discovery represents what the current MCP Server actually makes available. Whether a capability is actually useful still depends on whether `runtimeDatabases` connects to ShardingSphere-Proxy or to a regular database.
Clients select available capabilities according to the connection target.

### Connecting to ShardingSphere-Proxy

Use this mode to inspect ShardingSphere logical database structure, read governance rule state, execute controlled SQL, or create reviewable governance change plans through feature plugins.
In this mode, logical metadata, logical SQL, DistSQL, rule state, algorithm plugins, and plugin workflows can be used through MCP capabilities.

Usage limits:

- Physical metadata follows what Proxy exposes and should not be treated as the complete catalog of every underlying physical database.
- Capabilities that depend on ShardingSphere rules, algorithms, or DistSQL apply only to Proxy connections.
- Planning capabilities create reviewable plans. Business impact still needs to be reviewed before execution.

### Connecting directly to a database

Use this mode when the MCP Server should act as a controlled access path to a regular database for reading JDBC metadata, searching objects, assisting query generation, or executing restricted SQL.
In this mode, general database metadata and SQL tools are available.

Usage limits:

- ShardingSphere rules, algorithm plugins, and plugin workflows that depend on DistSQL do not apply.
- Returned metadata is database-native metadata and does not include ShardingSphere logical rule views.
- Clients must not assume that resources and tools behave exactly the same for direct database connections and Proxy connections.

## Resources

Resources provide context such as runtime status, database lists, table structure, column information, or workflow plans.
Clients read the corresponding resources as needed for the task.

| Resource URI or template | Purpose | Natural language example |
| --- | --- | --- |
| `shardingsphere://capabilities` | Reads available tasks and side-effect notes for the MCP Server. | "Which database tasks does this MCP Server support?" |
| `shardingsphere://runtime` | Reads the current transport, runtime status, and configured runtime database summary. | "Which logical databases are configured on the MCP Server?" |
| `shardingsphere://databases` | Lists runtime databases reachable by the current MCP Server. When connected to Proxy, they correspond to ShardingSphere logical databases. | "List the logical databases that can be accessed." |
| `shardingsphere://databases/{database}` | Reads one runtime database and its metadata summary. | "Show the metadata summary for `<logic-database>`." |
| `shardingsphere://databases/{database}/capabilities` | Reads SQL, transaction, schema, and metadata-object capabilities for one runtime database. | "Which SQL and metadata capabilities does `<logic-database>` support?" |
| `shardingsphere://databases/{database}/schemas` | Lists schemas or namespaces inside one runtime database. | "Show schemas in `<logic-database>`." |
| `shardingsphere://databases/{database}/schemas/{schema}` | Reads one schema or namespace. | "Show details for `<schema-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | Lists sequences in one schema. | "List sequences in `<schema-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | Reads one sequence. | "Show details for `<sequence-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | Lists tables in one schema. | "List tables in `<schema-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | Reads one table. | "Inspect the structure of `<table-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | Lists columns for one table. | "Show columns for `<table-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | Reads one table column. | "Show the type and constraints of `<column-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | Lists indexes for one table. | "Show indexes for `<table-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | Reads one table index. | "Show which columns `<index-name>` contains." |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | Lists views in one schema. | "List views in `<schema-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | Reads one view. | "Show a summary of `<view-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | Lists columns for one view. | "Show columns for `<view-name>`." |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | Reads one view column. | "Show details for view column `<column-name>`." |
| `shardingsphere://workflows/{plan_id}` | Reads the current governance change plan, clarification questions, artifacts, and next actions. | "Show the previous rule change plan and next action." |

Plugin resources, tools, prompts, and completion targets are documented on the corresponding plugin pages.

## Tools

Tools execute actions, such as searching metadata, executing SQL, or handling plugin workflow phases.
Actions with side effects should be previewed or reviewed first.

| Tool | Purpose | Natural language example | Side effects |
| --- | --- | --- | --- |
| `database_gateway_search_metadata` | Search runtime database metadata by name fragment and object type, and return resource hints for follow-up reads. | "Find tables whose names contain `order`." | None. |
| `database_gateway_validate_proxy_connectivity` | Validate a configured runtime database, including driver loading, JDBC connectivity, metadata readability, and database visibility before formal onboarding. | "Check whether configured database `logic_db` is ready before we register it." | None. |
| `database_gateway_execute_query` | Execute exactly one classifier-approved `SELECT` or `EXPLAIN ANALYZE` statement. | "Query the first 10 rows from `orders`." | None; rejects DML, DDL, DCL, transaction control, savepoints, and other side-effecting SQL. |
| `database_gateway_execute_update` | Preview or execute one SQL statement that may mutate data, metadata, rules, or transaction state. | "Preview this change SQL without executing it." | Yes; preview and confirmation are recommended first. |
| `database_gateway_apply_workflow` | Preview, execute, or export a governance change plan created by a feature plugin. | "Preview the previous encryption rule plan first." | Depends on the execution choice; preview and manual packages do not change runtime state. |
| `database_gateway_validate_workflow` | After plugin workflow execution, validate the result against visible metadata and generated artifacts. | "Validate whether the previous masking rule has taken effect." | None. |

Plugin tools are documented on the corresponding plugin pages.

### Proxy preflight validation output

`database_gateway_validate_proxy_connectivity` returns a structured validation payload with these top-level fields:

- `response_mode`
- `status`
- `database`
- `checks`
- `category`
- `recovery`

Common failure categories include `missing_jdbc_driver`, `authentication_failed`, `authorization_failed`, `connection_timeout`, `invalid_configuration`, `database_unavailable`, `connection_failed`, and `database_not_visible`.
The `recovery` field follows the same secret-safe runtime recovery style used by runtime database connection failures.
The tool only accepts the configured `database` name. Connection details such as JDBC URL, username, password, and driver class stay in the runtime configuration.

## Prompts

Prompts provide task guidance, such as which information to read first, how to handle SQL execution boundaries, or how to recover from failure.
Users usually do not use prompts directly.

| Prompt | Purpose |
| --- | --- |
| `inspect_metadata` | Guides metadata inspection tasks to read database metadata before choosing a search tool or detail resource. |
| `safe_sql_execution` | Guides SQL execution tasks to distinguish read-only queries from side-effecting SQL. |
| `recover_workflow` | Guides recovery or re-planning after plugin workflow failure. |

Plugin prompts are documented on the corresponding plugin pages.

## Completion targets

Completion targets help clients complete database, schema, table, and column names.
For example, when a user provides only part of an object name, the client can provide candidates.

### Resource completion targets

| Target | Completed arguments |
| --- | --- |
| `shardingsphere://databases/{database}` | `database` |
| `shardingsphere://databases/{database}/schemas` | `database` |
| `shardingsphere://databases/{database}/schemas/{schema}` | `database`, `schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | `database`, `schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | `database`, `schema`, `table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | `database`, `schema`, `table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`, `schema`, `table`, `column` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | `database`, `schema`, `table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | `database`, `schema`, `table`, `index` |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | `database`, `schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | `database`, `schema`, `sequence` |
| `shardingsphere://workflows/{plan_id}` | `plan_id` |

### Prompt completion targets

| Target | Completed arguments |
| --- | --- |
| `inspect_metadata` | `database`, `schema` |
| `safe_sql_execution` | `database`, `schema` |
| `recover_workflow` | `plan_id` |

Plugin prompt completion targets are documented on the corresponding plugin pages.
