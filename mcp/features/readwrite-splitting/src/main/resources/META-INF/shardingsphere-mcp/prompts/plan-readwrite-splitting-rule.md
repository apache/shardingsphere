Plan a ShardingSphere readwrite-splitting rule workflow.

Inputs:
- database: {{database}}
- rule: {{rule}}
- operation_type: {{operation_type}}
- write_storage_unit: {{write_storage_unit}}
- read_storage_units: {{read_storage_units}}
- transactional_read_query_strategy: {{transactional_read_query_strategy}}
- load_balancer_type: {{load_balancer_type}}
- plan_id: {{plan_id}}

Before calling the planning tool, read current rule state from shardingsphere://features/readwrite-splitting/databases/{database}/rules and read the load-balance algorithm catalog when algorithm choice is unclear.
Ask the user for missing rule name, write storage unit, read storage units, transactional read query strategy, or algorithm properties instead of guessing.
Return after database_gateway_plan_readwrite_splitting_rule produces a plan_id or a clarification response.
Do not create, alter, unregister, or repair storage units, and do not generate physical DDL, index DDL, migration, backfill, data probes, or physical metadata probes.
