+++
title = "Data Encryption"
weight = 1
+++

The Data Encryption MCP feature helps MCP clients plan encryption requirements into DDL, DistSQL, index plans, and validation steps executable through ShardingSphere-Proxy.
It does not implement encryption algorithms inside the MCP Server. It generates and applies encryption rule changes for ShardingSphere logical databases.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct physical database connections. A physical database usually does not understand ShardingSphere encryption DistSQL and cannot expose Proxy-visible encryption algorithm plugins or rule state.
- The target logical table and column should be discoverable through JDBC metadata exposed by Proxy. This metadata should not be treated as a complete physical database catalog.

## Public Surface

| Capability | How to call | When to use |
| --- | --- | --- |
| `database_gateway_plan_encrypt_rule` | Call through `tools/call`. | When a user asks to create or adjust an encryption rule. It creates `plan_id`, DDL, DistSQL, index suggestions, and validation steps. |
| `database_gateway_apply_workflow` | Call through `tools/call` with the `plan_id` returned by planning. | Preview the plan, execute reviewed artifacts, or export a manual package. |
| `database_gateway_validate_workflow` | Call through `tools/call` with the same `plan_id`. | After automatic or manual execution, validate rule state, logical metadata, and SQL executability. |
| `shardingsphere://features/encrypt/algorithms` | Read through `resources/read`. | Before planning, inspect encryption algorithm types and required properties visible through Proxy. |
| `shardingsphere://features/encrypt/databases/{database}/rules` | Fill `{database}` and read through `resources/read`. | Before altering rules, inspect existing encryption rules in the logical database. |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | Fill `{database}` and `{table}`, then read through `resources/read`. | Inspect one table's encryption rules or keep sibling column rules on the same table. |
| `plan_encrypt_rule` | Get through `prompts/get`. | When a client wants to guide the model to read table metadata, algorithms, and existing rules before calling the planning tool. |
| `plan_encrypt_rule` completion | Get candidates through `completion/complete`. | Completes `database`, `schema`, `table`, `column`, `algorithm_type`, `assisted_query_algorithm_type`, `like_query_algorithm_type`, or `plan_id`. |

## Minimum input

For creating or altering an encryption rule, the planning tool mainly uses these inputs:

| Argument | Required | Purpose |
| --- | --- | --- |
| `database` | Required | Logical database name exposed by ShardingSphere-Proxy. |
| `table` | Required | Logical table to configure. |
| `column` | Required | Logical column to configure. |
| `schema` | Optional | Schema or namespace. Recommended for multi-schema logical databases. |
| `natural_language_intent` | Recommended | Describes whether reversible encryption, equality query, or LIKE query support is needed. MCP uses it to infer planning intent when rule details are not explicit. |
| `operation_type` | Optional | Rule operation type. This page documents `create` and `alter` only. If omitted, MCP infers it from natural language and existing rules. |
| `algorithm_type` | Optional | Primary encryption algorithm type. Omit it if you want MCP to recommend one from available algorithms. |
| `primary_algorithm_properties` | Required by algorithm | Primary encryption algorithm properties, such as an AES key. The required properties come from the algorithm resource. |
| `allow_index_ddl` | Optional | Whether physical index plans may be generated for assisted-query columns. |

## Plan an encryption rule

Planning an encryption rule means calling `database_gateway_plan_encrypt_rule`.
It creates a reviewable plan only and does not modify the database directly.

```json
{
  "jsonrpc": "2.0",
  "id": "encrypt-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_encrypt_rule",
    "arguments": {
      "database": "<logic-database>",
      "table": "orders",
      "column": "status",
      "natural_language_intent": "Encrypt status reversibly with equality query support and without LIKE query support",
      "algorithm_type": "AES",
      "primary_algorithm_properties": {
        "aes-key-value": "${AES_KEY_VALUE}"
      }
    }
  }
}
```

Typical result:

- Returns `plan_id`.
- `status` is `planned` or `clarifying`.
- `derived_column_plan` describes derived column names.
- `ddl_artifacts` may contain physical column DDL.
- `distsql_artifacts` contains `CREATE/ALTER ENCRYPT RULE`.
- `index_plan` may contain assisted-query indexes.

If the response returns `clarifying`, continue with the same `plan_id`.
Secret fields are not echoed in plain text. Obtain them through a secret manager, protected environment variable, or controlled operations channel before continuing.

## Derived column rules

- `*_cipher` stores ciphertext and is the default derived column for encryption rules.
- If equality query is required, `*_assisted_query` is generated. Its index plan is generated when index DDL is allowed.
- If LIKE query is required, `*_like_query` is generated for LIKE query scenarios.
- If a default column name conflicts, the system appends a numeric suffix and returns the final name in `derived_column_plan`.
- Validation checks rules, logical metadata, and generated artifacts. It does not replace human review of the real physical table structure.

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

- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- MCP generates derived column, index, and column type suggestions from logical metadata exposed by Proxy. It does not inspect every physical database directly. Review generated DDL against the real physical table structure before applying it.
- The planner accepts ordinary unquoted logical database, schema, table, and column names to reduce ambiguity in generated SQL. This is not a ShardingSphere SQL capability limit.
