+++
title = "DROP SHARDING KEY GENERATOR"
weight = 10
+++

## Description

The `DROP SHARDING KEY GENERATOR` syntax is used to drop sharding key generator for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingKeyGenerator ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATOR' ifExists? keyGeneratorName (keyGeneratorName)* ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

keyGeneratorName ::=
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
- `ifExists` clause is used for avoid `Sharding key generator not exists` error.

### Example

- Drop sharding key generator for specified database

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake FROM sharding_db;
```

- Drop sharding key generator for current database

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake;
```

- Drop sharding key generator with `ifExists` clause

```sql
DROP SHARDING KEY GENERATOR IF EXISTS t_order_snowflake;
```

### Reserved word

`DROP`, `SHARDING`, `KEY`, `GENERATOR`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
