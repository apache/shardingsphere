+++
title = "SET SHARDING HINT DATABASE_VALUE"
weight = 3
+++

### Description

The `SET SHARDING HINT DATABASE_VALUE` syntax is used to set sharding value for database sharding only for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
SetShardingHintDatabaseValue ::=
  'SET' 'SHARDING' 'HINT' 'DATABASE_VALUE' '=' databaseShardingValue

  databaseShardingValue ::=
    int
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Set the sharding database value

```sql
SET SHARDING HINT DATABASE_VALUE = 100;
```

### Reserved word

`SET`, `SHARDING`, `HINT`, `DATABASE_VALUE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
