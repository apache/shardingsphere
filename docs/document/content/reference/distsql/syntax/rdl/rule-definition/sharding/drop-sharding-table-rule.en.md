+++
title = "DROP SHARDING TABLE RULE"
weight = 4
+++

## Description

The `DROP SHARDING TABLE RULE` syntax is used to drop sharding table rule for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingTableRule ::=
  'DROP' 'SHARDING' 'TABLE' 'RULE'  shardingRuleName (',' shardingRuleName)*  ('FROM' databaseName)?

shardingRuleName ::=
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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Drop mutiple sharding table rules for specified database.

```sql
DROP SHARDING TABLE RULE t_order, t_order_item FROM sharding_db;
```

- Drop a sharding table rule for current database.

```sql
DROP SHARDING TABLE RULE t_order;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
