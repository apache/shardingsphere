+++
title = "Data Masking"
weight = 2
+++

The Data Masking MCP feature helps MCP clients plan masking requirements into DistSQL and validation steps executable through ShardingSphere-Proxy.
Mask rules apply directly to logical columns and do not generate physical derived columns used by the Encrypt feature.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct physical database connections. A physical database usually does not understand ShardingSphere masking DistSQL and cannot expose Proxy-visible masking algorithm plugins or rule state.
- The target logical table and column should be discoverable through JDBC metadata exposed by Proxy. This metadata should not be treated as a complete physical database catalog.

## Use through natural language

Users describe the masking goal in the MCP client.
The model reads table structure, available masking algorithms, and existing rules, then creates a reviewable masking rule plan.
Users do not need to hand-write tool arguments or JSON-RPC requests.

Examples:

- Check whether `<logic-database>.orders.phone` already has a masking rule.
- Plan phone-number masking for `<logic-database>.orders.phone`, keep the first 3 and last 4 characters, and preview it without execution.
- Plan phone-number masking for column `Phone Number` in logical table `order detail`, preserving object-name case.
- Adjust the previous plan to use `*` as the replacement character.
- Confirm and execute the previous masking rule plan, then validate the result.

The model or client breaks these tasks into resource reads, rule planning, preview, execution, and validation.
Users should review DistSQL and side-effect scope before approving any side-effecting execution.

## Rule planning

Rule planning is the first phase of the masking plugin.
The model usually reads algorithm and existing-rule resources first, then calls the planning tool to create `plan_id` and a reviewable plan.
The planning tool does not modify the database directly. Preview, apply, and validation are handled by [Plugin Workflows](../plugin-workflow/) phase tools.

### Planning input

The planning tool uses these common inputs:

| Argument | Required | Purpose |
| --- | --- | --- |
| `database` | Required | Logical database name exposed by ShardingSphere-Proxy. |
| `table` | Required | Logical table to configure. |
| `column` | Required | Logical column to configure. |
| `schema` | Optional | Schema or namespace. Recommended for multi-schema logical databases. |
| `natural_language_intent` | Recommended | Describes the masking target, such as retained phone-number digits or replacement character. MCP uses it to infer planning intent when rule details are not explicit. |
| `operation_type` | Optional | Rule operation type. Supported values are `create`, `alter`, and `drop`. If omitted, MCP infers it from natural language and existing rules. |
| `algorithm_type` | Optional | Masking algorithm type. Omit it if you want MCP to recommend one from available algorithms. |
| `primary_algorithm_properties` | Required by algorithm | Masking algorithm properties, such as retained characters and replacement character. The required properties come from the algorithm resource. |

`database`, `schema`, `table`, and `column` can be ordinary identifiers or delimited identifiers wrapped in backticks, double quotes, or square brackets.
MCP preserves case, spaces, and other special characters during planning. Generated DistSQL uses backticks for quoted identifiers.
Generated validation SQL uses the target database dialect quote character, such as backticks for MySQL/MariaDB and double quotes for PostgreSQL/openGauss.
Identifiers must not contain NUL, carriage-return, or line-feed characters because they cannot be rendered as reviewable SQL.

Different operations focus on different inputs:

| Operation | Input focus | Planning result |
| --- | --- | --- |
| `create` | Provide the target column, masking intent, algorithm type, and algorithm properties. If you want MCP to recommend an algorithm, start with natural-language intent. | Generates DistSQL for adding the rule. |
| `alter` | Provide the target column and the algorithm or algorithm properties to change. | Generates DistSQL that preserves sibling column rules on the same table. |
| `drop` | Provide at least `database`, `table`, `column`, and `operation_type=drop`. | Generates `ALTER MASK RULE` when sibling masking columns remain on the same table, or `DROP MASK RULE` when no masking column remains on the target table. |

### Planning result

A typical planning result includes:

- `plan_id`, used for preview, apply, and validation.
- `status`, usually `planned` or `clarifying`.
- `distsql_artifacts`, containing `CREATE/ALTER/DROP MASK RULE`.
- `ddl_artifacts`, normally empty.
- `index_plan`, normally empty.

If the response returns `clarifying`, continue with the same `plan_id`.
Secret fields are not echoed in plain text. Obtain them through a secret manager, protected environment variable, or controlled operations channel before continuing.

## Apply and validate

After the planning tool returns `plan_id`, the model or client uses plugin workflow phase tools for apply and validation.
Preview first, then review DistSQL and side-effect scope before execution.

| Phase | User expression | Model or client action |
| --- | --- | --- |
| Preview | "Preview the previous masking rule plan without execution." | Use `database_gateway_apply_workflow` with `execution_mode=preview` to create preview results. |
| Execute | "Confirm and execute the previous plan." | After user review, use `database_gateway_apply_workflow` with `execution_mode=review-then-execute`. |
| Manual execution | "Export a manual execution package without automatic execution." | Use `database_gateway_apply_workflow` with `execution_mode=manual-only`. |
| Validate | "Validate whether the previous masking rule took effect." | Use `database_gateway_validate_workflow` to validate rule state, logical metadata, and SQL executability. |

Validation focuses on:

- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

See [Plugin Workflows](../plugin-workflow/) for workflow statuses, execution modes, and sensitive-input handling.

## MCP capability reference

This section is for custom clients, protocol debugging, or understanding the MCP calls behind model behavior.
Regular users usually only need to describe tasks in natural language.

| MCP capability | Type | Call entry | Phase | Result |
| --- | --- | --- | --- | --- |
| `database_gateway_plan_mask_rule` | Tool | `tools/call` | Plan creation, alteration, or deletion of masking rules. | Returns `plan_id`, planning status, DistSQL, and validation steps. |
| `database_gateway_apply_workflow` | Phase tool | `tools/call` with `plan_id`. | Preview, execute, or export a manual package after planning completes. | Returns preview artifacts, execution result, or manual execution package. |
| `database_gateway_validate_workflow` | Phase tool | `tools/call` with the same `plan_id`. | Validate results after automatic or manual execution. | Returns rule state, logical metadata, and SQL executability validation results. |
| `shardingsphere://features/mask/algorithms` | Resource | `resources/read` | Inspect masking algorithms visible through Proxy before planning. | Returns algorithm types and required properties. |
| `shardingsphere://features/mask/databases/{database}/rules` | Resource template | Fill `{database}` and read through `resources/read`. | Inspect existing masking rules before altering a logical database. | Returns logical database-level masking rules. |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | Resource template | Fill `{database}` and `{table}`, then read through `resources/read`. | Inspect one table's rules or keep sibling column rules on the same table. | Returns table-level masking rules. |
| `plan_mask_rule` | Prompt | `prompts/get` | Guide the model to read table metadata, algorithms, and existing rules before planning. | Returns the model prompt for masking rule planning. |
| `plan_mask_rule` completion | Completion target | `completion/complete` | Fill planning arguments in a client. | Returns candidates for `database`, `schema`, `table`, `column`, algorithm types, or `plan_id`. |

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct physical database connections.

### MCP plugin boundaries

- The MCP Server does not implement masking algorithms and does not replace the user's judgment on whether a masking strategy satisfies business compliance requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Dropping a masking rule removes the rule only. Later queries through Proxy no longer apply that masking rule to the column.

### Proxy-visible metadata boundaries

- Logical column and rule validation are based on what Proxy exposes.
- Direct physical database connections can execute ordinary SQL only and do not represent masking rule state.

### ShardingSphere feature boundaries

- Automatic rollback is not provided.

### Planner input limits

- Identifiers must not contain NUL, carriage-return, or line-feed characters because they cannot be rendered as reviewable SQL.
