+++
title = "SHOW SHADOW ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### Description

The `SHOW SHADOW ALGORITHM IMPLEMENTATIONS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showShadowAlgorithmImplementations ::=
  'SHOW' 'SHADOW' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns     | Description                           |
|-------------|---------------------------------------|
| name        | class name of the implementation      |
| type        | type of the implementation            |
| class_path  | full class name of the implementation |

### Example

- Query all the implementations for `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` interface

```sql
SHOW SHADOW ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW SHADOW ALGORITHM IMPLEMENTATIONS;
+-----------------------------------+-------------+--------------------------------------------------------------------------------------------+
| name                              | type        | class_path                                                                                 |
+-----------------------------------+-------------+--------------------------------------------------------------------------------------------+
| SQLHintShadowAlgorithm            | SQL_HINT    | org.apache.shardingsphere.shadow.algorithm.shadow.hint.SQLHintShadowAlgorithm              |
| ColumnRegexMatchedShadowAlgorithm | REGEX_MATCH | org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchedShadowAlgorithm |
| ColumnValueMatchedShadowAlgorithm | VALUE_MATCH | org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnValueMatchedShadowAlgorithm |
+-----------------------------------+-------------+--------------------------------------------------------------------------------------------+
3 rows in set (0.37 sec)
```

### Reserved word

`SHOW`, `SHADOW`, `ALGORITHM`, `IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
