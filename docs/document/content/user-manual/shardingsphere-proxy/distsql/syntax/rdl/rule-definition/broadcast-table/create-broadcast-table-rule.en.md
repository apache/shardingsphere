+++
title = "CREATE BROADCAST TABLE RULE"
weight = 1
+++

## Description

The `CREATE BROADCAST TABLE RULE` syntax is used to create broadcast table rules for tables that need to be
broadcast (broadcast tables)

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateBroadcastTableRule ::=
  'CREATE' 'BROADCAST' 'TABLE' 'RULE' ifNotExists? tableName (',' tableName)* 

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `tableName` can use an existing table or a table that will be created;
- `ifNotExists` clause is used for avoid `Duplicate Broadcast rule` error.

### Example

#### Create broadcast table rule

```sql
-- Add t_province, t_city to broadcast table rules
CREATE BROADCAST TABLE RULE t_province, t_city;
```

#### Create broadcast table rule with `ifNotExists` clause

```sql
CREATE BROADCAST TABLE RULE IF NOT EXISTS t_province, t_city;
```

### Reserved word

`CREATE`, `BROADCAST`, `TABLE`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
