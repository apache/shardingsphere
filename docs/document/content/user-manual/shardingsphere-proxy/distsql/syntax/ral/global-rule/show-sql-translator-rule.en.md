+++
title = "SHOW SQL_TRANSLATOR RULE"
weight = 5
+++

### Description

The `SHOW SQL_TRANSLATOR RULE` syntax is used to query SQL translator rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowSQLTranslatorRule ::=
  'SHOW' 'SQL_TRANSLATOR' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Show SQL translator rule

```sql
SHOW SQL_TRANSLATOR RULE;
```

### Reserved word

`SHOW`, `SQL_TRANSLATOR`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
