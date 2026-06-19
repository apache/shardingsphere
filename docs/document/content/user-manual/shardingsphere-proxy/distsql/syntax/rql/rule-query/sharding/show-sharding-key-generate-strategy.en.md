+++
title = "SHOW SHARDING KEY GENERATE STRATEGY"
weight = 14
+++

### Description

The `SHOW SHARDING KEY GENERATE STRATEGY` syntax is used to query sharding key generate strategies in the specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingKeyGenerateStrategies ::=
  'SHOW' 'SHARDING' 'KEY' 'GENERATE' ('STRATEGY' keyGenerateStrategyName | 'STRATEGIES') ('FROM' databaseName)?

keyGenerateStrategyName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted;
- Use `SHOW SHARDING KEY GENERATE STRATEGIES` to query all sharding key generate strategies;
- Use `SHOW SHARDING KEY GENERATE STRATEGY <name>` to query the specified sharding key generate strategy.

### Return value description

| Column         | Description                         |
|----------------|-------------------------------------|
| name           | Sharding key generate strategy name |
| type           | Sharding key generate strategy type |
| table          | Logical table name                  |
| column         | Key generate column                 |
| sequence       | Sequence name                       |
| generator_name | Referenced key generator name       |
| generator_type | Referenced key generator type       |
| generator_props| Referenced key generator properties |

### Example

- Query all sharding key generate strategies in the specified database

```sql
SHOW SHARDING KEY GENERATE STRATEGIES FROM sharding_db;
```

```sql
mysql> SHOW SHARDING KEY GENERATE STRATEGIES FROM sharding_db;
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
| name                    | type     | table   | column   | sequence  | generator_name      | generator_type | generator_props  |
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
| order_id_strategy       | column   | t_order | order_id |           | snowflake_generator | snowflake      | {"worker-id":1}  |
| order_sequence_strategy | sequence |         |          | order_seq | uuid_generator      | uuid           |                  |
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
2 rows in set (0.00 sec)
```

- Query the specified sharding key generate strategy in the current database

```sql
SHOW SHARDING KEY GENERATE STRATEGY order_id_strategy;
```

```sql
mysql> SHOW SHARDING KEY GENERATE STRATEGY order_id_strategy;
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
| name              | type   | table   | column   | sequence | generator_name      | generator_type | generator_props |
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
| order_id_strategy | column | t_order | order_id |          | snowflake_generator | snowflake      | {"worker-id":1} |
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `KEY`, `GENERATE`, `STRATEGY`, `STRATEGIES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATE STRATEGY](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generate-strategy/)
