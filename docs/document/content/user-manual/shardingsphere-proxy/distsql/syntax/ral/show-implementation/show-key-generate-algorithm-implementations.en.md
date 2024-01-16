+++
title = "SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS"
weight = 6
+++

### Description

The `"SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS"` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showKeyGenerateAlgorithmImplementations ::=
  'SHOW' 'KEY' 'GENERATE' 'ALGORITHM' 'IMPLEMENTATIONS'
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

- Query all the implementations for `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm` interface

```sql
SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS;
+-------------------------------+-----------+------------------------------------------------------------------------------------+
| name                          | type      | class_path                                                                         |
+-------------------------------+-----------+------------------------------------------------------------------------------------+
| UUIDKeyGenerateAlgorithm      | UUID      | org.apache.shardingsphere.keygen.uuid.algorithm.UUIDKeyGenerateAlgorithm           |
| SnowflakeKeyGenerateAlgorithm | SNOWFLAKE | org.apache.shardingsphere.keygen.snowflake.algorithm.SnowflakeKeyGenerateAlgorithm |
+-------------------------------+-----------+------------------------------------------------------------------------------------+
2 rows in set (0.05 sec)
```

### Reserved word

`SHOW`, `KEY`, `GENERATE`, `ALGORITHM`, `IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
