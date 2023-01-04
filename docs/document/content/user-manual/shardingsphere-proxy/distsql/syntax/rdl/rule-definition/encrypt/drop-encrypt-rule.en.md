+++
title = "DROP ENCRYPT RULE"
weight = 4
+++

## Description

The `DROP ENCRYPT RULE` syntax is used to drop an existing encryption rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' tableName (',' tableName)*
    
tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Drop an encrypt rule

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

### Reserved words

`DROP`, `ENCRYPT`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
