+++
title = "Mask"
weight = 2
+++

The Mask MCP feature helps MCP clients plan masking requirements into DistSQL and validation steps executable through ShardingSphere-Proxy.
Mask rules apply directly to logical columns and do not generate physical derived columns used by the Encrypt feature.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- The target logical table and column should be discoverable through JDBC metadata.

## Public Surface

Planning tool:

- `database_gateway_plan_mask_rule`

Common workflow tools:

- `database_gateway_apply_workflow`
- `database_gateway_validate_workflow`

Resources:

- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

## Minimum input

For creating or altering a mask rule, provide at least:

- `database`
- `table`
- `column`
- `natural_language_intent`, or explicit `operation_type=create|alter`
- `algorithm_type`, unless you want MCP to recommend one
- `primary_algorithm_properties`
- `schema`, recommended for multi-schema logical databases

For dropping a mask rule, the minimum input is:

- `database`
- `table`
- `column`
- `operation_type=drop`

## Plan a mask rule

```json
{
  "jsonrpc": "2.0",
  "id": "mask-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "logic_db",
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

## Drop a mask rule

```json
{
  "jsonrpc": "2.0",
  "id": "mask-drop-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "logic_db",
      "table": "orders",
      "column": "phone",
      "operation_type": "drop"
    }
  }
}
```

If sibling mask columns still exist on the same table, MCP generates `ALTER MASK RULE` and keeps the sibling rules.
It generates `DROP MASK RULE` only when no mask column remains on the target table.

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Supports `create`, `alter`, and `drop`.
- Does not generate physical derived columns.
- Does not provide automatic rollback.
- Planning input accepts only standard unquoted logical identifiers.
