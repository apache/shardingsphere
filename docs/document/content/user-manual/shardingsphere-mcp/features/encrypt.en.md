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

| MCP capability | Type | Call entry | Phase | Result |
| --- | --- | --- | --- | --- |
| `database_gateway_plan_encrypt_rule` | Tool | `tools/call` | Plan creation, alteration, or deletion of encryption rules. | Returns `plan_id`, planning status, DistSQL, validation steps, and DDL, derived column, or index suggestions when applicable. |
| `database_gateway_apply_workflow` | Phase tool | `tools/call` with `plan_id`. | Preview, execute, or export a manual package after planning completes. | Returns preview artifacts, execution result, or manual execution package. |
| `database_gateway_validate_workflow` | Phase tool | `tools/call` with the same `plan_id`. | Validate results after automatic or manual execution. | Returns rule state, logical metadata, and SQL executability validation results. |
| `shardingsphere://features/encrypt/algorithms` | Resource | `resources/read` | Inspect encryption algorithms visible through Proxy before planning. | Returns algorithm types and required properties. |
| `shardingsphere://features/encrypt/databases/{database}/rules` | Resource template | Fill `{database}` and read through `resources/read`. | Inspect existing encryption rules before altering a logical database. | Returns logical database-level encryption rules. |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | Resource template | Fill `{database}` and `{table}`, then read through `resources/read`. | Inspect one table's rules or keep sibling column rules on the same table. | Returns table-level encryption rules. |
| `plan_encrypt_rule` | Prompt | `prompts/get` | Guide the model to read table metadata, algorithms, and existing rules before planning. | Returns the model prompt for encryption rule planning. |
| `plan_encrypt_rule` completion | Completion target | `completion/complete` | Fill planning arguments in a client. | Returns candidates for `database`, `schema`, `table`, `column`, algorithm types, or `plan_id`. |

## Rule planning

Rule planning is the first phase of the encryption plugin.
The model usually reads algorithm and existing-rule resources first, then calls `database_gateway_plan_encrypt_rule` to create a reviewable plan.
The planning tool does not modify the database directly. Preview, apply, and validation are handled by plugin workflow phase tools.

### Planning input

The planning tool uses these common inputs:

| Argument | Required | Purpose |
| --- | --- | --- |
| `database` | Required | Logical database name exposed by ShardingSphere-Proxy. |
| `table` | Required | Logical table to configure. |
| `column` | Required | Logical column to configure. |
| `schema` | Optional | Schema or namespace. Recommended for multi-schema logical databases. |
| `natural_language_intent` | Recommended | Describes whether reversible encryption, equality query, or LIKE query support is needed. MCP uses it to infer planning intent when rule details are not explicit. |
| `operation_type` | Optional | Rule operation type. Supported values are `create`, `alter`, and `drop`. If omitted, MCP infers it from natural language and existing rules. |
| `algorithm_type` | Optional | Primary encryption algorithm type. Omit it if you want MCP to recommend one from available algorithms. |
| `primary_algorithm_properties` | Required by algorithm | Primary encryption algorithm properties, such as an AES key. The required properties come from the algorithm resource. |
| `allow_index_ddl` | Optional | Whether physical index plans may be generated for assisted-query columns. |

Different operations focus on different inputs:

| Operation | Input focus | Planning result |
| --- | --- | --- |
| `create` | Provide the target column, encryption intent, algorithm type, and algorithm properties. If you want MCP to recommend an algorithm, start with natural-language intent. | Generates DistSQL for adding the rule, and physical derived-column DDL or index suggestions when needed. |
| `alter` | Provide the target column and the algorithm, query capability, or algorithm properties to change. | Generates DistSQL that preserves sibling column rules on the same table, and updates DDL or index suggestions when needed. |
| `drop` | Provide at least `database`, `table`, `column`, and `operation_type=drop`. | Generates `ALTER ENCRYPT RULE` when sibling encryption columns remain on the same table, or `DROP ENCRYPT RULE` when no encryption column remains on the target table. |

### Call example

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

## Derived columns and index plans

Encryption rules may need physical derived columns to store ciphertext or support queries.
MCP creates derived-column suggestions from the logical column, user intent, and existing rules, and writes the final names to `derived_column_plan`.

- `*_cipher` stores ciphertext and is the default derived column for encryption rules.
- If equality query is required, `*_assisted_query` is generated. Its index plan is generated when index DDL is allowed.
- If LIKE query is required, `*_like_query` is generated for LIKE query scenarios.
- If a default column name conflicts, the system appends a numeric suffix and returns the final name in `derived_column_plan`.

## Apply and validate

After the planning tool returns `plan_id`, use plugin workflow phase tools for apply and validation.
Preview first, then review DistSQL, DDL, index plans, and side-effect scope before execution.

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

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct physical database connections.

### Rule change boundaries

- MCP generates derived-column, index, and column-type suggestions from logical metadata exposed by Proxy. It does not inspect every physical database directly.
- Review generated DDL against the real physical table structure before applying it.
- Existing data migration or backfill is not handled.
- Automatic rollback is not provided.
- Dropping an encryption rule removes the rule only. It does not restore historical plaintext data, and physical derived columns or indexes still require manual cleanup when they are no longer needed.

### Planner input limits

- The planner accepts ordinary unquoted logical database, schema, table, and column names to reduce ambiguity in generated SQL. This is not a ShardingSphere SQL capability limit.
