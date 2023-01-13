+++
title = "ADD SHARDING HINT DATABASE_VALUE"
weight = 3
+++

### Description

The `ADD SHARDING HINT DATABASE_VALUE` syntax is used to add sharding database value to specified table for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'DATABASE_VALUE' shardingHintDatabaseValueDefinition
  
  shardingHintDatabaseValueDefinition ::=
    tableName '=' databaseShardingValue

  tableName ::=
    identifier

  databaseShardingValue ::=
    int
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Add the database sharding value for specified table

```sql
ADD SHARDING HINT DATABASE_VALUE t_order = 100;
```

### Reserved word

`ADD`, `SHARDING`, `HINT`, `DATABASE_VALUE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
