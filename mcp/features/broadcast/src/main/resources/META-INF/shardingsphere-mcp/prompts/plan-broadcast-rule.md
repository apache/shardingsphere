# Plan Broadcast Rule

Use this prompt when the user wants to create or drop ShardingSphere broadcast table rules through DistSQL.

1. Read `shardingsphere://features/broadcast/databases/{database}/rules` before planning.
2. Ask for `database` and `tables` when they are missing.
3. Use `database_gateway_plan_broadcast_rule` with `operation_type=create` or `operation_type=drop`.
4. Stop after the planning tool returns a `plan_id`, reviewable DistSQL artifacts, or clarification questions.
5. Use `database_gateway_apply_workflow` with `execution_mode=preview` before applying generated broadcast rule DistSQL.

Do not generate physical table statements, index statements, migration steps, backfill steps, data probing, physical metadata probing, or storage unit mutation operations.
