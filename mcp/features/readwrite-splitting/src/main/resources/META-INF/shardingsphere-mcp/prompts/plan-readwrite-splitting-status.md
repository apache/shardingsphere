Plan a ShardingSphere readwrite-splitting storage-unit status workflow.

Inputs:
- database: {{database}}
- rule: {{rule}}
- storage_unit: {{storage_unit}}
- target_status: {{target_status}}
- plan_id: {{plan_id}}

Before calling the planning tool, read current status from shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}/status.
Ask the user for the rule name, read storage unit, or target status when any input is missing.
Return after database_gateway_plan_readwrite_splitting_status produces a plan_id or a clarification response.
Do not create, alter, unregister, or repair storage units, and do not generate physical DDL, index DDL, migration, backfill, data probes, or physical metadata probes.
