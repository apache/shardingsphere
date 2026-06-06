+++
title = "Protocol Surface"
weight = 1
+++

The public surface of ShardingSphere-MCP is defined by descriptors under `META-INF/shardingsphere-mcp/mcp-descriptors`.
The MCP runtime uses these descriptors to publish tools, resources, resource templates, prompts, and completions.

## Protocol capabilities

ShardingSphere-MCP targets MCP protocol revision `2025-11-25`.

Enabled:

- `resources/list`
- `resources/templates/list`
- `resources/read`
- `tools/list`
- `tools/call`
- `prompts/list`
- `prompts/get`
- `completion/complete`

Not implemented or future scope:

- Resource subscriptions.
- Resource, tool, and prompt list-changed notifications.
- ShardingSphere product logs through `notifications/message`.
- `progress`.
- `notifications/cancelled`.
- Task-augmented requests.
- MCP `icons` and `Tool.execution` fields until the MCP Java SDK boundary exposes them.

`roots` and `sampling` are client capabilities.
ShardingSphere-MCP does not require roots and does not send `sampling/createMessage` requests.

## Tools

`database_gateway_search_metadata`

- Searches logical database metadata.
- Narrows scope by `database`, `schema`, `query`, and `object_types`.
- `object_types` supports `database`, `schema`, `table`, `view`, `column`, `index`, and `sequence`.

`database_gateway_validate_proxy_connectivity`

- Validates a configured runtime database before formal onboarding.
- Required input is `database`.
- Uses the administrator-configured runtime database connection details; JDBC URL, username, password, and driver class are not tool inputs.
- Returns `status`, ordered `checks`, overall `category`, and a structured `recovery` object.
- Common failure categories include `missing_jdbc_driver`, `authentication_failed`, `authorization_failed`, `connection_timeout`, `invalid_configuration`, `database_unavailable`, `connection_failed`, and `database_not_visible`.

`database_gateway_execute_query`

- Executes one classifier-approved `SELECT` or `EXPLAIN ANALYZE`.
- Rejects DML, DDL, DCL, transaction control, savepoints, and known side-effecting query forms.
- `max_rows` range is `0..5000`; omitted or `0` uses the server default `100`.
- `timeout_ms` range is `0..300000`; `0` means no explicit timeout.

`database_gateway_execute_update`

- Previews or executes one supported side-effecting SQL statement.
- `execution_mode=preview` only classifies the SQL and previews the side-effect scope.
- `execution_mode=execute` executes the SQL after review.
- Multiple statements and banned commands are rejected.

`database_gateway_apply_workflow`

- Previews, executes, or exports an existing workflow plan in the current session.
- `execution_mode` supports `preview`, `review-then-execute`, and `manual-only`.
- `approved_steps` must use approval steps returned by preview.

`database_gateway_validate_workflow`

- Validates an existing workflow plan in the current session.
- Use after planning review or after apply to confirm runtime state.

Feature plugin planning tools:

- `database_gateway_plan_encrypt_rule`
- `database_gateway_plan_mask_rule`
- `database_gateway_plan_broadcast_rule`
- `database_gateway_plan_readwrite_splitting_rule`
- `database_gateway_plan_readwrite_splitting_status`
- `database_gateway_plan_shadow_rule`
- `database_gateway_plan_default_shadow_algorithm`
- `database_gateway_plan_shadow_algorithm_cleanup`
- `database_gateway_plan_sharding_table_rule`
- `database_gateway_plan_sharding_table_reference_rule`
- `database_gateway_plan_sharding_default_strategy`
- `database_gateway_plan_sharding_key_generator`
- `database_gateway_plan_sharding_key_generate_strategy`
- `database_gateway_plan_sharding_rule_component_cleanup`

## Resources

Runtime and capability:

- `shardingsphere://capabilities`
- `shardingsphere://runtime`
- `shardingsphere://databases`
- `shardingsphere://databases/{database}`
- `shardingsphere://databases/{database}/capabilities`

Metadata:

- `shardingsphere://databases/{database}/schemas`
- `shardingsphere://databases/{database}/schemas/{schema}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`
- `shardingsphere://databases/{database}/schemas/{schema}/views`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`

Workflow:

- `shardingsphere://workflows/{plan_id}`

Feature resources:

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/broadcast/databases/{database}/rules`
- `shardingsphere://features/broadcast/databases/{database}/rule-count`
- `shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins`
- `shardingsphere://features/readwrite-splitting/databases/{database}/rules`
- `shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}`
- `shardingsphere://features/readwrite-splitting/databases/{database}/status`
- `shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}/status`
- `shardingsphere://features/readwrite-splitting/databases/{database}/rule-count`
- `shardingsphere://features/shadow/algorithm-plugins`
- `shardingsphere://features/shadow/databases/{database}/rules`
- `shardingsphere://features/shadow/databases/{database}/rules/{rule}`
- `shardingsphere://features/shadow/databases/{database}/table-rules`
- `shardingsphere://features/shadow/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/shadow/databases/{database}/algorithms`
- `shardingsphere://features/shadow/databases/{database}/default-algorithm`
- `shardingsphere://features/shadow/databases/{database}/rule-count`
- `shardingsphere://features/sharding/algorithm-plugins`
- `shardingsphere://features/sharding/key-generate-algorithm-plugins`
- `shardingsphere://features/sharding/databases/{database}/algorithms`
- `shardingsphere://features/sharding/databases/{database}/table-rules`
- `shardingsphere://features/sharding/databases/{database}/tables/{table}/table-rule`
- `shardingsphere://features/sharding/databases/{database}/table-nodes`
- `shardingsphere://features/sharding/databases/{database}/tables/{table}/nodes`
- `shardingsphere://features/sharding/databases/{database}/table-reference-rules`
- `shardingsphere://features/sharding/databases/{database}/table-reference-rules/{rule}`
- `shardingsphere://features/sharding/databases/{database}/default-strategy`
- `shardingsphere://features/sharding/databases/{database}/key-generators`
- `shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}`
- `shardingsphere://features/sharding/databases/{database}/key-generate-strategies`
- `shardingsphere://features/sharding/databases/{database}/key-generate-strategies/{strategy}`
- `shardingsphere://features/sharding/databases/{database}/auditors`
- `shardingsphere://features/sharding/databases/{database}/unused-algorithms`
- `shardingsphere://features/sharding/databases/{database}/unused-key-generators`
- `shardingsphere://features/sharding/databases/{database}/unused-auditors`
- `shardingsphere://features/sharding/databases/{database}/algorithms/{algorithm}/table-rules`
- `shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}/table-rules`
- `shardingsphere://features/sharding/databases/{database}/auditors/{auditor}/table-rules`
- `shardingsphere://features/sharding/databases/{database}/rule-count`

## Prompts

- `inspect_metadata`: guides the model to read metadata and avoid SQL execution when the user only asks for metadata.
- `safe_sql_execution`: guides the model to distinguish read-only query from side-effecting SQL.
- `recover_workflow`: guides recovery from failed or stale workflows.
- `plan_encrypt_rule`: guides Encrypt feature workflow planning.
- `plan_mask_rule`: guides Mask feature workflow planning.
- `plan_broadcast_rule`: guides Broadcast feature workflow planning.
- `plan_readwrite_splitting_rule`: guides Readwrite-Splitting rule workflow planning.
- `plan_readwrite_splitting_status`: guides Readwrite-Splitting status workflow planning.
- `plan_shadow_rule`: guides Shadow rule workflow planning.
- `plan_default_shadow_algorithm`: guides default Shadow algorithm workflow planning.
- `plan_shadow_algorithm_cleanup`: guides unused Shadow algorithm cleanup workflow planning.
- `plan_sharding_table_rule`: guides Sharding table rule workflow planning.
- `plan_sharding_table_reference_rule`: guides Sharding table reference rule workflow planning.
- `plan_sharding_default_strategy`: guides default Sharding strategy workflow planning.
- `plan_sharding_key_generator`: guides Sharding key generator workflow planning.
- `plan_sharding_key_generate_strategy`: guides Sharding key generate strategy workflow planning.
- `plan_sharding_rule_component_cleanup`: guides unused Sharding algorithm, key generator, or auditor cleanup workflow planning.

## Completions

Completions suggest runtime names, metadata identifiers, algorithms, and workflow `plan_id` values in the current session.
Before choosing uncertain database, schema, table, column, algorithm, or `plan_id` values, clients should call `completion/complete` or read the nearest MCP resource.

## Responses and recovery

List-shaped business payloads usually contain:

- `items`
- `count`
- `has_more`
- `continuation_mode`

Large-result payloads use:

- `truncated`
- `total_count`
- `returned_count`
- `large_result_guidance`

Recoverable error payloads keep `message` and add `recovery` hints.
Common recovery cases include missing arguments, unsupported tools or resources, invalid enum values, workflow state errors, and unsafe SQL tool selection.

JSON-RPC numeric error codes are the MCP protocol error contract.
