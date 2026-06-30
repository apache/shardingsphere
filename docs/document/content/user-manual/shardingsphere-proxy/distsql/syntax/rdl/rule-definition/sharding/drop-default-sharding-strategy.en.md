+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 6
+++

## Description

The `DROP DEFAULT SHARDING STRATEGY` syntax is used to drop the default sharding strategy from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ifExists?

ifExists ::=
  'IF' 'EXISTS'

```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause is used for avoid `Default sharding strategy not exists` error.

### Example

- Drop default sharding table strategy

```sql
DROP DEFAULT SHARDING TABLE STRATEGY;
```

- Drop default sharding database strategy

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY;
```

- Drop default sharding table strategy with `ifExists` clause

```sql
DROP DEFAULT SHARDING TABLE STRATEGY IF EXISTS;
```

- Drop default sharding database strategy with `ifExists` clause

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY IF EXISTS;
```

### Reserved word

`DROP`, `DEFAULT`, `SHARDING`, `TABLE`, `DATABASE`, `STRATEGY`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
