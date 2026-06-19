# Plan Shadow Algorithm Cleanup

Read configured shadow algorithms, shadow table rules, default shadow algorithm, and shadow rules before cleanup planning.

Inputs:
- database: {{database}}
- algorithm_name: {{algorithm_name}}
- plan_id: {{plan_id}}

Generate `DROP SHADOW ALGORITHM` only when the target algorithm is configured and is not referenced by table rules or the default shadow algorithm. Do not remove shadow rules, storage units, physical databases, physical tables, indexes, migration jobs, backfill jobs, or data probes.

Ask the user when the logical database or algorithm name is unclear, or when the algorithm is still referenced.
