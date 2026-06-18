# Plan Default Shadow Algorithm

Read the current default shadow algorithm, configured shadow algorithms, and shadow algorithm plugins before planning.

Inputs:
- database: {{database}}
- operation_type: {{operation_type}}
- algorithm_type: {{algorithm_type}}
- plan_id: {{plan_id}}

Generate only `CREATE DEFAULT SHADOW ALGORITHM`, `ALTER DEFAULT SHADOW ALGORITHM`, or `DROP DEFAULT SHADOW ALGORITHM` DistSQL artifacts. Current ShardingSphere grammar does not include a `FROM` clause for default shadow algorithm RDL, so use the logical database as workflow context and not as a rendered RDL suffix.

Ask the user when the logical database, operation, algorithm type, or required properties are unclear.
