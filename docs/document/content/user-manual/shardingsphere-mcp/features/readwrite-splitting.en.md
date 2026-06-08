+++
title = "Readwrite-Splitting"
weight = 5
+++

The Readwrite-Splitting MCP feature plugin helps users plan, review, apply, and validate readwrite-splitting rule changes and status changes for ShardingSphere-Proxy logical databases.
It generates readwrite-splitting DistSQL only. It does not generate storage unit registration, physical DDL, index DDL, migration, backfill, data probes, or physical metadata probes.

## Prerequisites

- The target `runtimeDatabases` entry should connect to ShardingSphere-Proxy.
- The write and read storage unit names must already exist in ShardingSphere-Proxy.
- The user should provide the logical database, rule name, write storage unit, read storage units, and load-balance algorithm intent when planning a rule.

## Natural language examples

- List readwrite-splitting rules and load-balance algorithm plugins in `<logic-database>`.
- Plan a readwrite-splitting rule named `rw_ds` with write storage unit `write_ds` and read storage units `read_ds_0, read_ds_1`.
- Disable read storage unit `read_ds_1` for rule `rw_ds`, then validate the status.

## Review checklist

- Confirm that rule plans use `CREATE`, `ALTER`, or `DROP READWRITE_SPLITTING RULE`.
- Confirm that status plans use `ALTER READWRITE_SPLITTING RULE ... ENABLE` or `DISABLE`.
- Confirm that storage unit names are existing logical storage units and are not created by the workflow.

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Does not create or repair storage units.
- Does not inspect physical datasource metadata.
- Object name content must not contain backticks, NUL, carriage returns, or line feeds.
