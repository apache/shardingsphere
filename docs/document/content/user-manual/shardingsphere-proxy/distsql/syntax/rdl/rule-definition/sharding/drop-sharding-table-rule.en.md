+++
title = "DROP SHARDING TABLE RULE"
weight = 3
+++

## Description

The `DROP SHARDING TABLE RULE` syntax is used to drop sharding table rules from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingTableRule ::=
  'DROP' 'SHARDING' 'TABLE' 'RULE' ifExists? ruleName (',' ruleName)*

ifExists ::=
  'IF' 'EXISTS'

ruleName ::=
  identifier

```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause is used to avoid `Sharding rule not exists` error.

### Example

- Drop multiple sharding table rules

```sql
DROP SHARDING TABLE RULE t_order, t_order_item;
```

- Drop a sharding table rule

```sql
DROP SHARDING TABLE RULE t_order;
```

- Drop sharding table rule with `ifExists` clause

```sql
DROP SHARDING TABLE RULE IF EXISTS t_order;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `RULE`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
