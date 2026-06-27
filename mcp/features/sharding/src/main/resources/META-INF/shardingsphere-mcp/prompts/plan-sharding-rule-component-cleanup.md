# Plan Sharding Rule Component Cleanup

Inputs:
- database: {{database}}
- operation_type: {{operation_type}}
- plan_id: {{plan_id}}

Plan cleanup only with `DROP SHARDING ALGORITHM`, `DROP SHARDING KEY GENERATOR`, or `DROP SHARDING AUDITOR`. First read unused and used-by resources; do not plan cleanup when the component is still referenced.
