# Plan Sharding Table Rule

Inputs:
- database: {{database}}
- operation_type: {{operation_type}}
- algorithm_type: {{algorithm_type}}
- plan_id: {{plan_id}}

Plan only ShardingSphere sharding table rule DistSQL.
Use this workflow before raw side-effect SQL when the user asks in natural language to create or drop database/table sharding rules, including generated key rules such as Snowflake.

Before planning:
- Read existing table rules, algorithm plugins, and key-generate algorithm plugins for the logical database.
- Preserve existing rule details that the user did not ask to change.
- Do not create physical databases, physical tables, indexes, storage units, physical data-change jobs, or data probes.

Ask before planning when any required rule input is unclear:
- logical table and actual data nodes
- strategy type and required sharding column or columns
- sharding algorithm type and required properties
- key generator, auditor, or generated key column when the requested rule needs them

Stop after returning a plan_id with reviewable DistSQL artifacts, or after listing the missing inputs.
