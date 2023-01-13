+++
title = "DROP TRAFFIC RULE"
weight = 10
+++

### Description

The `DROP TRAFFIC RULE` syntax is used to drop specified dual routing rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropTrafficRule ::=
  'DROP' 'TRAFFIC' 'RULE' ruleName (',' ruleName)?

ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Drop specified traffic rule

```sql
DROP TRAFFIC RULE sql_match_traffic;
```

- Drop mutiple traffic rules
```sql
DROP TRAFFIC RULE sql_match_traffic, sql_hint_traffic;
```

### Reserved word

`DROP`, `TRAFFIC`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
