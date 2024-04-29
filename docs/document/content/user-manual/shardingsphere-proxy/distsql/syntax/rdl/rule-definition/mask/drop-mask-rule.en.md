+++
title = "DROP MASK RULE"
weight = 3
+++

## Description

The `DROP MASK RULE` syntax is used to drop existing mask rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropEncryptRule ::=
  'DROP' 'MASK' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` clause used for avoid `Mask rule not exists` error.

### Example

- Drop mask rule

```sql
DROP MASK RULE t_mask, t_mask_1;
```

- Drop mask rule with `ifExists` clause

```sql
DROP MASK RULE IF EXISTS t_mask, t_mask_1;
```

### Reserved words

`DROP`, `MASK`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
