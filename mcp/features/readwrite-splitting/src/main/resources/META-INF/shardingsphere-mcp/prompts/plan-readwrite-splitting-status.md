Plan a ShardingSphere readwrite-splitting storage-unit status workflow.

Inputs:
- database: {{database}}
- rule: {{rule}}
- storage_unit: {{storage_unit}}
- target_status: {{target_status}}
- plan_id: {{plan_id}}

Before calling the planning tool, read current status from shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}/status.
Ask the user for the rule name, read storage unit, or target status when any input is missing.
The status change requires a Cluster-mode ShardingSphere Proxy; the planning tool verifies the current Proxy mode before generating DistSQL.
Return after database_gateway_plan_readwrite_splitting_status produces a planned, failed, or clarification response.
If the plan reports `WF-MODE-002`, explain that Cluster mode is required and stop without calling apply or repeating the planning tool.
Do not create, alter, unregister, or repair storage units, and do not generate physical DDL, index DDL, migration, backfill, data probes, or physical metadata probes.
