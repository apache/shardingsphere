+++
title = "Broadcast"
weight = 4
+++

The Broadcast MCP feature plugin helps users plan, review, apply, and validate broadcast table rule changes for ShardingSphere-Proxy logical databases.
It generates broadcast rule DistSQL only. It does not generate physical table DDL, index DDL, data migration, backfill, data probes, physical metadata probes, or storage unit mutation tasks.

## Prerequisites

- The target `runtimeDatabases` entry should connect to ShardingSphere-Proxy.
- The user should provide the logical database and broadcast table names.
- Existing rule state is read from Proxy-visible DistSQL resources.

## Natural language examples

- List current broadcast table rules in `logic_db`.
- Plan a broadcast rule for `config_region` and `config_feature`, then preview it without execution.
- Drop the broadcast rule for `config_region` and validate the result.

## Review checklist

- Confirm that the planned statement is `CREATE BROADCAST TABLE RULE` or `DROP BROADCAST TABLE RULE`.
- Confirm that all table names are logical table names.
- Confirm the returned `plan_id`, `resources_to_read`, `next_actions`, and `distsql_artifacts` before applying the workflow.
- Broadcast planning does not require algorithm recommendations or property requirements.
- Preview the workflow before execution and validate Proxy-visible rule state after execution.

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Does not create, alter, or inspect physical tables.
- Does not register, alter, unregister, or repair storage units.
- Object name content must not contain backticks, NUL, carriage returns, or line feeds.
