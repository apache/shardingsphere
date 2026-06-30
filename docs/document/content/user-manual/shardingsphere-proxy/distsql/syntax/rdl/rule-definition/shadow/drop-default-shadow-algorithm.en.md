+++
title = "DROP DEFAULT SHADOW ALGORITHM"
weight = 6
+++

## Description

The `DROP DEFAULT SHADOW ALGORITHM` syntax is used to drop the default shadow algorithm from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropDefaultShadowAlgorithm ::=
  'DROP' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifExists?

ifExists ::=
  'IF' 'EXISTS'

```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause used for avoid `Default shadow algorithm not exists` error.

### Example

- Drop default shadow algorithm

```sql
DROP DEFAULT SHADOW ALGORITHM;
```

- Drop default shadow algorithm with `ifExists` clause

```sql
DROP DEFAULT SHADOW ALGORITHM IF EXISTS;
```

### Reserved word

`DROP`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
