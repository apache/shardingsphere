# Plan Sharding Rule Component Cleanup

Inputs:
- database: {{database}}
- operation_type: {{operation_type}}
- plan_id: {{plan_id}}

Plan cleanup only with `DROP SHARDING ALGORITHM`, `DROP SHARDING KEY GENERATOR`, or `DROP SHARDING AUDITOR`.

Before planning:
- Read the unused component resource that matches the requested component type.
- Read the matching used-by resource before dropping a named component.
- Do not plan cleanup when the component is still referenced by any table rule, strategy, or auditor binding.

Ask before planning when database, component_type, or component_name is unclear.

Stop after returning a plan_id with reviewable cleanup DistSQL, or after explaining why cleanup is unsafe.
