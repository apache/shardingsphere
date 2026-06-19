+++
title = "Sharding"
weight = 7
+++

The Sharding MCP feature plugin helps users plan, review, apply, and validate sharding table rules, table reference rules, default sharding strategies, key generators, key generate strategies, and safe cleanup of unused sharding components.
It generates sharding rule DistSQL only. It does not generate physical DDL, index DDL, migration, backfill, data probes, physical metadata probes, or storage unit mutation tasks.

## Prerequisites

- The target `runtimeDatabases` entry should connect to ShardingSphere-Proxy.
- Data nodes, storage units, sharding columns, algorithm types, key generators, auditors, and rule names should be supplied explicitly when required.
- Cleanup planning reads unused and used-by DistSQL resources before generating `DROP SHARDING ALGORITHM`, `DROP SHARDING KEY GENERATOR`, or `DROP SHARDING AUDITOR`.

## Natural language examples

- List sharding table rules, table nodes, algorithms, key generators, auditors, and unused components in `<logic-database>`.
- Plan a sharding table rule for `t_order` with explicit data nodes and a standard sharding strategy.
- Plan a default table sharding strategy using algorithm `t_order_inline`.
- Drop unused sharding algorithm `t_order_inline` only if Proxy-visible state proves it is unused.

## Review checklist

- Confirm that table rule plans use only sharding rule DistSQL and logical identifiers.
- Review sharding `algorithm_recommendations` and `property_requirements`; for example, inline algorithms require `algorithm-expression`, and MOD-style algorithms require `sharding-count`.
- Confirm that key generator and key generate strategy plans keep generator properties separate from sharding algorithm properties.
- Review key-generator recommendations separately from sharding algorithm recommendations. Snowflake properties are optional unless the deployment requires fixed values.
- Confirm cleanup plans include unused-state and used-by checks.
- Confirm the returned `plan_id`, `resources_to_read`, `next_actions`, and `distsql_artifacts` before applying the workflow.
- Preview the workflow before execution and validate Proxy-visible rule state after execution.

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Does not create physical tables, indexes, or storage units.
- Does not migrate or backfill existing data.
- Object name content must not contain backticks, NUL, carriage returns, or line feeds.
