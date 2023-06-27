+++
title = "UNLOAD SINGLE TABLE"
weight = 2
+++

## Description

The `UNLOAD SINGLE TABLE` syntax is used to unload single table.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
unloadSingleTable ::=
  'UNLOAD' 'SINGLE' 'TABLE' tableNames

tableNames ::=
  tableName (',' tableName)*

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- Unlike loading, only the table name needs to be specified when unloading a single table


### Example

- Unload specified single table

```sql
UNLOAD SINGLE TABLE t_single;
```

- Load all single tables

```sql
UNLOAD SINGLE TABLE *;
-- or
UNLOAD ALL SINGLE TABLES;
```

### Reserved word

`UNLOAD`, `SINGLE`, `TABLE`, `ALL`, `TABLES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
