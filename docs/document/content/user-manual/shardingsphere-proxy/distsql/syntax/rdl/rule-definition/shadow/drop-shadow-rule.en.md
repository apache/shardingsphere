+++
title = "DROP SHADOW RULE"
weight = 3
+++

## Description

The `DROP SHADOW RULE` syntax is used to drop shadow rule for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'RULE' ifExists? shadowRuleName ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

shadowRuleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted;
- `ifExists` clause is used for avoid `Shadow rule not exists` error.

### Example

- Drop shadow rule for specified database

```sql
DROP SHADOW RULE shadow_rule FROM shadow_db;
```

- Drop shadow rule for current database

```sql
DROP SHADOW RULE shadow_rule;
```

- Drop shadow rule with `ifExists` clause

```sql
DROP SHADOW RULE IF EXISTS shadow_rule;
```

### Reserved word

`DROP`, `SHODOW`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
