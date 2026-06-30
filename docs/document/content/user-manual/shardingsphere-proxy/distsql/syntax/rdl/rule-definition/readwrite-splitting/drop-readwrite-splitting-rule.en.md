+++
title = "DROP READWRITE_SPLITTING RULE"
weight = 3
+++

## Description

The `DROP READWRITE_SPLITTING RULE` syntax is used to drop readwrite-splitting rules from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropReadwriteSplittingRule ::=
  'DROP' 'READWRITE_SPLITTING' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` clause is used for avoid `Readwrite-splitting rule not exists` error.

### Example

- Drop readwrite-splitting rule

```sql
DROP READWRITE_SPLITTING RULE ms_group_1;
```

- Drop multiple readwrite-splitting rules

```sql
DROP READWRITE_SPLITTING RULE ms_group_1, ms_group_2;
```

- Drop readwrite-splitting rule with `ifExists` clause

```sql
DROP READWRITE_SPLITTING RULE IF EXISTS ms_group_1;
```

### Reserved word

`DROP`, `READWRITE_SPLITTING`, `RULE`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
