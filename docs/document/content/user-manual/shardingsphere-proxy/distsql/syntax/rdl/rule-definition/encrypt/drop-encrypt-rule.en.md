+++
title = "DROP ENCRYPT RULE"
weight = 3
+++

## Description

The `DROP ENCRYPT RULE` syntax is used to drop an existing encryption rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` clause is used for avoid `Encrypt rule not exists` error.

### Example

- Drop an encrypt rule

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

- Drop encrypt with `ifExists` clause

```sql
DROP ENCRYPT RULE IF EXISTS t_encrypt, t_encrypt_2;
```

### Reserved words

`DROP`, `ENCRYPT`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
