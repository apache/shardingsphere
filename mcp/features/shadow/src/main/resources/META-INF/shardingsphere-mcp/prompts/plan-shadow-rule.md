# Plan Shadow Rule

Read the current shadow rules, table rules, configured algorithms, default algorithm, and shadow algorithm plugins before planning.

Inputs:
- database: {{database}}
- rule: {{rule}}
- operation_type: {{operation_type}}
- source_storage_unit: {{source_storage_unit}}
- shadow_storage_unit: {{shadow_storage_unit}}
- table: {{table}}
- algorithm_type: {{algorithm_type}}
- plan_id: {{plan_id}}

Generate only `CREATE SHADOW RULE`, `ALTER SHADOW RULE`, or `DROP SHADOW RULE` DistSQL artifacts. Do not create storage units, physical databases, physical tables, indexes, migration jobs, backfill jobs, or data probes.

Ask the user when the logical database, rule name, source storage unit, shadow storage unit, table, algorithm type, or required algorithm properties are unclear.
