+++
title = "ADD SHARDING HINT TABLE_VALUE"
weight = 5
+++

### Description

The `ADD SHARDING HINT TABLE_VALUE` syntax is used to add table sharding value to specified table for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'TABLE_VALUE' shardingHintTableValueDefinition
  
  shardingHintTableValueDefinition ::=
    tableName '=' tableShardingValue

  tableName ::=
    identifier

  tableShardingValue ::=
    int
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Add the table sharding value for specified table

```sql
ADD SHARDING HINT TABLE_VALUE t_order = 100;
```

### Reserved word

`ADD`, `SHARDING`, `HINT`, `TABLE_VALUE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
