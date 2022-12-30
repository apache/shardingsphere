+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 15
+++

## Description

The `DROP SHARDING TABLE REFERENCE RULE` syntax is used to drop specified sharding table reference rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  ruleName (',' ruleName)*

ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Drop a specified sharding table reference rule

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0;
```

- Drop multiple sharding table reference rules

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0, ref_1;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
