+++
title = "DROP BROADCAST TABLE RULE"
weight = 2
+++

## Description

The `DROP BROADCAST TABLE RULE` syntax is used to drop broadcast table rule for specified broadcast tables

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropBroadcastTableRule ::=
  'DROP' 'BROADCAST' 'TABLE' 'RULE' ifExists? tableName (',' tableName)* 

ifExists ::=
  'IF' 'EXISTS'

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `tableName` can use the table of existing broadcast rules;
- `ifExists` clause is used for avoid `Broadcast rule not exists` error.

### Example

- Drop broadcast table rule for specified broadcast table

```sql
DROP BROADCAST TABLE RULE t_province, t_city;
```

- Drop broadcast table rule with `ifExists` clause

```
DROP BROADCAST TABLE RULE IF EXISTS t_province, t_city;
```

### Reserved word

`DROP`, `BROADCAST`, `TABLE`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
