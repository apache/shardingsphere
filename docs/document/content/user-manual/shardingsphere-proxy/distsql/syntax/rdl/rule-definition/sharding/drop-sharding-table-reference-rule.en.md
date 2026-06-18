+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 14
+++

## Description

The `DROP SHARDING TABLE REFERENCE RULE` syntax is used to drop specified sharding table reference rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` clause is used for avoid `Sharding reference rule not exists` error.
### Example

- Drop a specified sharding table reference rule

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0;
```

- Drop multiple sharding table reference rules

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0, ref_1;
```

- Drop sharding table reference rule with `ifExists` clause

```sql
DROP SHARDING TABLE REFERENCE RULE IF EXISTS ref_0;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
