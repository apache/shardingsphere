+++
title = "Capabilities"
weight = 2
+++

This page lists the protocol capabilities exposed by ShardingSphere-MCP.
The runtime source of truth is `shardingsphere://capabilities` plus the official MCP list methods.

## Discovery entry points

| Protocol capability | Purpose |
| --- | --- |
| `tools/list` | Discover callable tools. |
| `resources/list` | Discover directly readable resources. |
| `resources/templates/list` | Discover parameterized resource templates. |
| `prompts/list` | Discover available prompts. |
| `completion/complete` | Get completion candidates for resources, prompts, or arguments. |
| `resources/read` with `shardingsphere://capabilities` | Read the ShardingSphere domain capability catalog. |

## Capability Availability

ShardingSphere-MCP exposes one protocol surface, but runtime availability depends on the target configured in `runtimeDatabases`.

- When connected to ShardingSphere-Proxy, logical metadata, logical SQL, DistSQL, encryption or masking rules, and algorithm plugin capabilities are available. Physical metadata still follows what Proxy exposes.
- When connected to a physical database, general JDBC metadata and SQL execution capabilities are available. ShardingSphere rules, algorithm plugins, and workflows that depend on DistSQL do not apply.
- Clients should read `shardingsphere://runtime` and `shardingsphere://databases/{database}/capabilities` before assuming that every resource or tool behaves the same for every connection target.

## Static resources

| Resource | Purpose |
| --- | --- |
| `shardingsphere://capabilities` | Read resources, resource templates, tools, prompts, completions, workflow relationships, and side-effect notes. |
| `shardingsphere://runtime` | Read the current transport, runtime status, and configured runtime database summary. |
| `shardingsphere://databases` | List runtime databases reachable by the current MCP Server. When connected to Proxy, they correspond to ShardingSphere logical databases. |
| `shardingsphere://features/encrypt/algorithms` | List data encryption algorithm plugins visible from the current ShardingSphere-Proxy runtime. |
| `shardingsphere://features/mask/algorithms` | List data masking algorithm plugins visible from the current ShardingSphere-Proxy runtime. |

## Resource templates

| Resource template | Purpose |
| --- | --- |
| `shardingsphere://databases/{database}` | Read one runtime database and its metadata summary. |
| `shardingsphere://databases/{database}/capabilities` | Read SQL, transaction, schema, and metadata-object capabilities for one runtime database. |
| `shardingsphere://databases/{database}/schemas` | List schemas or namespaces inside one runtime database. |
| `shardingsphere://databases/{database}/schemas/{schema}` | Read one schema or namespace. |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | List sequences in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | Read one sequence. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | List tables in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | Read one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | List columns for one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | Read one table column. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | List indexes for one table. |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | Read one table index. |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | List views in one schema. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | Read one view. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | List columns for one view. |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | Read one view column. |
| `shardingsphere://workflows/{plan_id}` | Read a current-session workflow plan, clarification questions, artifacts, and next actions. |
| `shardingsphere://features/encrypt/databases/{database}/rules` | List data encryption rules in one logical database. |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | Read data encryption rules for one logical table. |
| `shardingsphere://features/mask/databases/{database}/rules` | List data masking rules in one logical database. |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | Read data masking rules for one logical table. |

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

| Target type | Target | Completed arguments |
| --- | --- | --- |
| resource | `shardingsphere://databases/{database}` | `database` |
| resource | `shardingsphere://databases/{database}/schemas` | `database` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}` | `database`, `schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables` | `database`, `schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | `database`, `schema`, `table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | `database`, `schema`, `table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`, `schema`, `table`, `column` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | `database`, `schema`, `table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | `database`, `schema`, `table`, `index` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/sequences` | `database`, `schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | `database`, `schema`, `sequence` |
| resource | `shardingsphere://workflows/{plan_id}` | `plan_id` |
| resource | `shardingsphere://features/encrypt/algorithms` | `algorithm_type`, `assisted_query_algorithm_type`, `like_query_algorithm_type` |
| resource | `shardingsphere://features/mask/algorithms` | `algorithm_type` |
| prompt | `inspect_metadata` | `database`, `schema` |
| prompt | `safe_sql_execution` | `database`, `schema` |
| prompt | `recover_workflow` | `plan_id` |
| prompt | `plan_encrypt_rule` | `database`, `schema`, `table`, `column`, `algorithm_type`, `assisted_query_algorithm_type`, `like_query_algorithm_type`, `plan_id` |
| prompt | `plan_mask_rule` | `database`, `schema`, `table`, `column`, `algorithm_type`, `plan_id` |
