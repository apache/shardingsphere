+++
title = "Shadow"
weight = 6
+++

The Shadow MCP feature plugin helps users plan, review, apply, and validate shadow rules, default shadow algorithms, and safe cleanup of unused shadow algorithms.
It generates shadow rule DistSQL only. It does not create shadow databases or physical tables, and it does not generate physical DDL, index DDL, migration, backfill, data probes, physical metadata probes, or storage unit mutation tasks.

## Prerequisites

- The target `runtimeDatabases` entry should connect to ShardingSphere-Proxy.
- Source and shadow storage units must already exist.
- Cleanup planning reads Proxy-visible rule, table-rule, default-algorithm, and configured-algorithm state before generating `DROP SHADOW ALGORITHM`.

## Natural language examples

- List configured shadow rules and shadow algorithm plugins in `logic_db`.
- Plan a shadow rule for table `t_order` using source storage unit `ds_0`, shadow storage unit `ds_shadow`, and a column-match algorithm.
- Drop unused shadow algorithm `shadow_by_user_id` only if Proxy-visible state proves it is unused.

## Review checklist

- Confirm that rule plans use shadow DistSQL and reference existing storage units.
- Confirm that default algorithm plans update only the default shadow algorithm.
- Review `algorithm_recommendations` before selecting a shadow algorithm. `SQL_HINT` has no required properties.
- `VALUE_MATCH` requires `operation`, `column`, and `value`; `REGEX_MATCH` requires `operation`, `column`, and `regex`.
- Confirm cleanup plans include evidence that the target algorithm is unused.
- Confirm the returned `plan_id`, `resources_to_read`, `next_actions`, and `distsql_artifacts` before applying the workflow.

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

- Supports ShardingSphere-Proxy logical databases only.
- Does not create shadow databases, physical tables, or storage units.
- Cleanup is limited to DistSQL-visible unused shadow algorithms.
- Object name content must not contain backticks, NUL, carriage returns, or line feeds.
