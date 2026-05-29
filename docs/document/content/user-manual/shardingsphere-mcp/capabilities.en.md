+++
title = "Capability Catalog"
weight = 2
+++

This page explains the core capabilities of ShardingSphere-MCP and how protocol methods, resource URIs, tools, and prompts relate to each other.
The documentation explains capability semantics. Clients should use MCP list methods and `shardingsphere://capabilities` to read what the current MCP Server actually exposes.

## Capability discovery

The following entries include MCP protocol methods and ShardingSphere-MCP resource URIs.
MCP protocol methods do not use the `shardingsphere://` prefix. That prefix is used only for ShardingSphere-MCP resource URIs.
Tools and prompts are invoked by name and are not resource URIs.

| Method or resource | Type | Purpose |
| --- | --- | --- |
| `tools/list` | MCP protocol method | Lists callable tools. |
| `resources/list` | MCP protocol method | Lists resources that can be read without template arguments. |
| `resources/templates/list` | MCP protocol method | Lists parameterized resource URI templates. Clients fill the template before reading it through `resources/read`. |
| `resources/read` | MCP protocol method | Reads one concrete resource URI. Reading `shardingsphere://capabilities` returns the ShardingSphere domain capability catalog. |
| `prompts/list` | MCP protocol method | Lists available prompts. |
| `completion/complete` | MCP protocol method | Gets completion candidates for resources, prompts, or arguments. |
| `shardingsphere://capabilities` | ShardingSphere-MCP resource URI | Reads the ShardingSphere domain capability catalog. |

## Capability Availability

ShardingSphere-MCP exposes one protocol surface, but runtime availability depends on the target configured in `runtimeDatabases`.

- When connected to ShardingSphere-Proxy, logical metadata, logical SQL, DistSQL, encryption or masking rules, and algorithm plugin capabilities are available. Physical metadata still follows what Proxy exposes.
- When connected to a physical database, general JDBC metadata and SQL execution capabilities are available. ShardingSphere rules, algorithm plugins, and workflows that depend on DistSQL do not apply.
- Clients should read `shardingsphere://runtime` and `shardingsphere://databases/{database}/capabilities` before assuming that every resource or tool behaves the same for every connection target.

## Resources

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

Feature plugin resources are documented on the corresponding plugin pages.

## Tools

| Tool | Purpose | Side effects |
| --- | --- | --- |
| `database_gateway_search_metadata` | Search runtime database metadata by name fragment and object type, and return resource hints for follow-up reads. | None. |
| `database_gateway_execute_query` | Execute exactly one classifier-approved `SELECT` or `EXPLAIN ANALYZE` statement. | None; rejects DML, DDL, DCL, transaction control, savepoints, and other side-effecting SQL. |
| `database_gateway_execute_update` | Preview or execute one SQL statement that may mutate data, metadata, rules, or transaction state. | Yes; requires explicit `execution_mode=preview` or `execution_mode=execute`. |
| `database_gateway_apply_workflow` | Preview, execute, or export a manual package for a planned workflow. | Depends on `execution_mode`; `preview` and `manual-only` do not change runtime state. |
| `database_gateway_validate_workflow` | Validate a planned or applied workflow against visible metadata and generated artifacts. | None. |
| `database_gateway_plan_encrypt_rule` | Plan data encryption rule changes for Proxy logical databases and generate reviewable DDL, DistSQL, index plans, and validation steps. | None; creates a plan only. |
| `database_gateway_plan_mask_rule` | Plan data masking rule changes for Proxy logical databases and generate reviewable DistSQL and validation steps. | None; creates a plan only. |

## Prompts

| Prompt | Purpose |
| --- | --- |
| `inspect_metadata` | Guide the model to read database metadata before choosing a search tool or detail resource. |
| `safe_sql_execution` | Guide the model to choose the correct SQL tool for read-only queries or side-effecting SQL. |
| `recover_workflow` | Guide the model to recover or re-plan after workflow failure or unavailable `plan_id`. |
| `plan_encrypt_rule` | Guide the model to read logical metadata, available algorithms, and existing rules before planning data encryption. |
| `plan_mask_rule` | Guide the model to read logical metadata, available algorithms, and existing rules before planning data masking. |

## Completion targets

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
| `plan_encrypt_rule` | `database`, `schema`, `table`, `column`, `algorithm_type`, `assisted_query_algorithm_type`, `like_query_algorithm_type`, `plan_id` |
| `plan_mask_rule` | `database`, `schema`, `table`, `column`, `algorithm_type`, `plan_id` |
