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

## Public Surface

| Capability | How to call | When to use |
| --- | --- | --- |
| `database_gateway_plan_mask_rule` | Call through `tools/call`. | When a user asks to create or adjust a masking rule. It creates `plan_id`, DistSQL, and validation steps. |
| `database_gateway_apply_workflow` | Call through `tools/call` with the `plan_id` returned by planning. | Preview the plan, execute reviewed artifacts, or export a manual package. |
| `database_gateway_validate_workflow` | Call through `tools/call` with the same `plan_id`. | After automatic or manual execution, validate rule state, logical metadata, and SQL executability. |
| `shardingsphere://features/mask/algorithms` | Read through `resources/read`. | Before planning, inspect masking algorithm types and required properties visible through Proxy. |
| `shardingsphere://features/mask/databases/{database}/rules` | Fill `{database}` and read through `resources/read`. | Before altering rules, inspect existing masking rules in the logical database. |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | Fill `{database}` and `{table}`, then read through `resources/read`. | Inspect one table's masking rules or keep sibling column rules on the same table. |
| `plan_mask_rule` | Get through `prompts/get`. | When a client wants to guide the model to read table metadata, algorithms, and existing rules before calling the planning tool. |
| `plan_mask_rule` completion | Get candidates through `completion/complete`. | Completes `database`, `schema`, `table`, `column`, `algorithm_type`, or `plan_id`. |

## Minimum input

For creating or altering a masking rule, the planning tool mainly uses these inputs:

| Argument | Required | Purpose |
| --- | --- | --- |
| `database` | Required | Logical database name exposed by ShardingSphere-Proxy. |
| `table` | Required | Logical table to configure. |
| `column` | Required | Logical column to configure. |
| `schema` | Optional | Schema or namespace. Recommended for multi-schema logical databases. |
| `natural_language_intent` | Recommended | Describes the masking target, such as retained phone-number digits or replacement character. MCP uses it to infer planning intent when rule details are not explicit. |
| `operation_type` | Optional | Rule operation type. This page documents `create` and `alter` only. If omitted, MCP infers it from natural language and existing rules. |
| `algorithm_type` | Optional | Masking algorithm type. Omit it if you want MCP to recommend one from available algorithms. |
| `primary_algorithm_properties` | Required by algorithm | Masking algorithm properties, such as retained characters and replacement character. The required properties come from the algorithm resource. |

## Plan a mask rule

Planning a masking rule means calling `database_gateway_plan_mask_rule`.
It creates a reviewable plan only and does not modify the database directly.

```json
{
  "jsonrpc": "2.0",
  "id": "mask-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "<logic-database>",
      "table": "orders",
      "column": "phone",
      "natural_language_intent": "Mask phone as a phone number and keep the first 3 and last 4 characters",
      "algorithm_type": "KEEP_FIRST_N_LAST_M",
      "primary_algorithm_properties": {
        "first-n": "3",
        "last-m": "4",
        "replace-char": "*"
      }
    }
  }
}
```

Typical result:

- Returns `plan_id`.
- `status` is `planned` or `clarifying`.
- `distsql_artifacts` contains `CREATE/ALTER MASK RULE`.
- `ddl_artifacts` is normally empty.
- `index_plan` is normally empty.

If the natural language input does not identify the algorithm clearly or required algorithm properties are missing, MCP returns `clarifying`.
Continue with the same `plan_id` and provide fields requested by `clarification_questions`.

## Apply and validate

After the planning tool returns `plan_id`, use the common workflow tools for apply and validation.

Preview first:

```json
{
  "name": "database_gateway_apply_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}",
    "execution_mode": "preview"
  }
}
```

Execute after reviewing artifacts:

```json
{
  "name": "database_gateway_apply_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}",
    "execution_mode": "review-then-execute"
  }
}
```

Validate:

```json
{
  "name": "database_gateway_validate_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}"
  }
}
```

Validation focuses on:

- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Logical column and rule validation are based on what Proxy exposes. Direct physical database connections can execute ordinary SQL only and do not represent masking rule state.
- The planner accepts ordinary unquoted logical database, schema, table, and column names to reduce ambiguity in generated SQL. This is not a ShardingSphere SQL capability limit.
