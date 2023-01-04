+++
title = "DROP MASK RULE"
weight = 4
+++

## Description

The `DROP MASK RULE` syntax is used to drop existing mask rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropEncryptRule ::=
  'DROP' 'MASK' 'RULE' ruleName (',' ruleName)*
    
ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Drop mask rule

```sql
DROP MASK RULE t_mask, t_mask_1;
```

### Reserved words

`DROP`, `MASK`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
