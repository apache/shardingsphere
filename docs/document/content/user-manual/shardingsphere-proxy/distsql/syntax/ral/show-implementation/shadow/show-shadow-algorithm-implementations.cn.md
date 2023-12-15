+++
title = "SHOW SHADOW ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### 描述

`SHOW SHADOW ALGORITHM IMPLEMENTATIONS` 语法用于查询 `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` 接口所有具体的实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showShadowAlgorithmImplementations ::=
  'SHOW' 'SHADOW' 'ALGORITHM' 'IMPLEMENTATIONS'
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

- 查询 `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` 接口的所有实现类

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

### 保留字

`SHOW`、`SHADOW`、`ALGORITHM`、`IMPLEMENTATIONS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)