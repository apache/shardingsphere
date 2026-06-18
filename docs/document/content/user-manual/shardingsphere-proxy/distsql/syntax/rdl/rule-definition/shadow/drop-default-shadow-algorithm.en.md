+++
title = "DROP DEFAULT SHADOW ALGORITHM"
weight = 6
+++

## Description

The `DROP DEFAULT SHADOW ALGORITHM` syntax is used to drop default shadow algorithm for specified database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDefaultShadowAlgorithm ::=
  'DROP' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifExists? ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

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
- `ifExists` clause used for avoid `Default shadow algorithm not exists` error.

### Example

- Drop default shadow algorithm for specified database

```sql
DROP DEFAULT SHADOW ALGORITHM FROM shadow_db;
```

- Drop default shadow algorithm for current database

```sql
DROP DEFAULT SHADOW ALGORITHM;
```

- Drop default shadow algorithm with `ifExists` clause

```sql
DROP DEFAULT SHADOW ALGORITHM IF EXISTS;
```

### Reserved word

`DROP`, `DEFAULT`, `SHODOW`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
