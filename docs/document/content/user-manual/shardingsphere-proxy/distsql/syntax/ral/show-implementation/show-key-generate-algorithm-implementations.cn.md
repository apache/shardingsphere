+++
title = "SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS"
weight = 6
+++

### 描述

`SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS` 语法用于查询 `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm` 接口所有具体的实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showKeyGenerateAlgorithmImplementations ::=
  'SHOW' 'KEY' 'GENERATE' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列    | 说明      |
|------|---------|
| name | 实现类名称   |
| type | 类型      |
| class_path | 实现类完整路径 |

### 示例

- 查询 `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm` 接口的所有实现类

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

### 保留字

`SHOW`、`KEY`、`GENERATE`、`ALGORITHM`、`IMPLEMENTATIONS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)