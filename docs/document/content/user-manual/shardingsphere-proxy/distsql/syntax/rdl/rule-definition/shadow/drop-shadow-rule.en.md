+++
title = "DROP SHADOW RULE"
weight = 3
+++

## Description

The `DROP SHADOW RULE` syntax is used to drop shadow rules from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` clause is used to avoid `Shadow rule not exists` error.

### Example

- Drop shadow rule

```sql
DROP SHADOW RULE shadow_rule;
```

- Drop multiple shadow rules

```sql
DROP SHADOW RULE shadow_rule, shadow_rule_1;
```

- Drop shadow rule with `ifExists` clause

```sql
DROP SHADOW RULE IF EXISTS shadow_rule;
```

### Reserved word

`DROP`, `SHADOW`, `RULE`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
