+++
title = "Capability Catalog"
weight = 2
+++

This page explains the core capabilities of ShardingSphere-MCP and how protocol methods, resource URIs, tools, and prompts relate to each other.
The documentation explains capability semantics. Clients should use MCP list methods and `shardingsphere://capabilities` to read what the current MCP Server actually exposes.

## Capability discovery

The following entries include MCP protocol methods and ShardingSphere-MCP resource URIs.
`tools/list`, `resources/list`, `resources/read`, `prompts/list`, and `completion/complete` are MCP JSON-RPC method names.
`shardingsphere://...` is the ShardingSphere-MCP resource URI prefix.
Method names, tool names, and prompt names do not use a URI prefix. Only resource URIs and resource templates use it.

| Method or resource | Type | Purpose |
| --- | --- | --- |
| `tools/list` | MCP protocol method | Lists callable tools. |
| `tools/call` | MCP protocol method | Calls one tool by tool name. |
| `resources/list` | MCP protocol method | Lists descriptors for resources that can be read without template arguments. It does not return resource content. |
| `resources/templates/list` | MCP protocol method | Lists parameterized resource URI templates. Clients fill the template first. |
| `resources/read` | MCP protocol method | Reads the content of one concrete resource URI. Reading `shardingsphere://capabilities` returns the ShardingSphere domain capability catalog. |
| `prompts/list` | MCP protocol method | Lists available prompts. |
| `prompts/get` | MCP protocol method | Reads one prompt and generates messages from its arguments. |
| `completion/complete` | MCP protocol method | Gets completion candidates for resources, prompts, or arguments. |
| `shardingsphere://capabilities` | ShardingSphere-MCP resource URI | Reads the ShardingSphere domain capability catalog. |

Capability discovery returns the protocol surface of the current MCP Server. Whether a capability is actually useful still depends on whether `runtimeDatabases` connects to ShardingSphere-Proxy or to a regular database.
Clients should read `shardingsphere://runtime` and `shardingsphere://databases/{database}/capabilities` before deciding which resources to read or which tools to call.

### Connecting to ShardingSphere-Proxy

Use this mode when a model needs to understand ShardingSphere logical database structure, read governance rule state, execute controlled SQL, or create reviewable governance change plans through feature plugins.
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

Resources provide context to the model, such as runtime status, database lists, table structure, column information, or workflow plans.
Clients or models read concrete resource URIs through `resources/read`; resource templates must be filled before they are read.

| Resource URI or template | Type | Purpose |
| --- | --- | --- |
| `shardingsphere://capabilities` | Resource URI | Reads resource URIs, resource templates, tools, prompts, completions, workflow relationships, and side-effect notes. |
| `shardingsphere://runtime` | Resource URI | Reads the current transport, runtime status, and configured runtime database summary. |
| `shardingsphere://databases` | Resource URI | Lists runtime databases reachable by the current MCP Server. When connected to Proxy, they correspond to ShardingSphere logical databases. |
| `shardingsphere://databases/{database}` | Resource template | Reads one runtime database and its metadata summary. |
| `shardingsphere://databases/{database}/capabilities` | Resource template | Reads SQL, transaction, schema, and metadata-object capabilities for one runtime database. |
| `shardingsphere://databases/{database}/schemas` | Resource template | Lists schemas or namespaces inside one runtime database. |
| `shardingsphere://databases/{database}/schemas/{schema}` | Resource template | Reads one schema or namespace. |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | Resource template | Lists sequences in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | Resource template | Reads one sequence. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | Resource template | Lists tables in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | Resource template | Reads one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | Resource template | Lists columns for one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | Resource template | Reads one table column. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | Resource template | Lists indexes for one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | Resource template | Reads one table index. |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | Resource template | Lists views in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | Resource template | Reads one view. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | Resource template | Lists columns for one view. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | Resource template | Reads one view column. |
| `shardingsphere://workflows/{plan_id}` | Resource template | Reads a current-session workflow plan, clarification questions, artifacts, and next actions. |

Plugin resources, tools, prompts, and completion targets are documented on the corresponding plugin pages.

## Tools

Tools execute actions, such as searching metadata, executing SQL, or handling plugin workflow phases.
Models call tools through `tools/call`. Tools with side effects require an explicit execution mode and should be previewed or reviewed first.

| Tool | Purpose | Side effects |
| --- | --- | --- |
| `database_gateway_search_metadata` | Search runtime database metadata by name fragment and object type, and return resource hints for follow-up reads. | None. |
| `database_gateway_execute_query` | Execute exactly one classifier-approved `SELECT` or `EXPLAIN ANALYZE` statement. | None; rejects DML, DDL, DCL, transaction control, savepoints, and other side-effecting SQL. |
| `database_gateway_execute_update` | Preview or execute one SQL statement that may mutate data, metadata, rules, or transaction state. | Yes; requires explicit `execution_mode=preview` or `execution_mode=execute`. |
| `database_gateway_apply_workflow` | After plugin planning returns `plan_id`, preview, execute, or export a manual package. | Depends on `execution_mode`; `preview` and `manual-only` do not change runtime state. |
| `database_gateway_validate_workflow` | After plugin workflow execution, validate the result against visible metadata and generated artifacts. | None. |

Plugin tools are documented on the corresponding plugin pages.

## Prompts

Prompts guide the model through a task, such as which resources to read first, which tool to choose, or how to recover from failure.
Clients fetch prompt content through `prompts/get` and provide it to the model for reasoning. Prompts are not commands that users need to run manually.

| Prompt | Purpose |
| --- | --- |
| `inspect_metadata` | Guide the model to read database metadata before choosing a search tool or detail resource. |
| `safe_sql_execution` | Guide the model to choose the correct SQL tool for read-only queries or side-effecting SQL. |
| `recover_workflow` | Guide the model to recover or re-plan after plugin workflow failure or unavailable `plan_id`. |

Plugin prompts are documented on the corresponding plugin pages.

## Completion targets

Completion targets help clients or models fill resource URIs, prompt arguments, or tool arguments.
For example, when a user provides only part of a database, schema, table, or column name, the client can request candidates through `completion/complete`.

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
