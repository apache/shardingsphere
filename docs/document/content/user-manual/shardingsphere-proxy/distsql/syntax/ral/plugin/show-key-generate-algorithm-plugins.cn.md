+++
title = "SHOW KEY GENERATE ALGORITHM PLUGINS"
weight = 7
+++

### 描述

`SHOW KEY GENERATE ALGORITHM PLUGINS` 语法用于查询 `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm` 接口的所有实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showKeyGenerateAlgorithmPlugins ::=
  'SHOW' 'KEY' 'GENERATE' 'ALGORITHM' 'PLUGINS'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列            | 说明     |
|--------------|--------|
| type         | 类型     |
| type_aliases | 类型别名   |
| description  | 描述     |

### 示例

- 查询 `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm` 接口的所有实现类

```sql
SHOW KEY GENERATE ALGORITHM PLUGINS
```

```sql
SHOW KEY GENERATE ALGORITHM PLUGINS;
+-----------+--------------+-------------+
| type      | type_aliases | description |
+-----------+--------------+-------------+
| UUID      |              |             |
| SNOWFLAKE |              |             |
+-----------+--------------+-------------+
2 rows in set (0.05 sec)
```

### 保留字

`SHOW`、`KEY`、`GENERATE`、`ALGORITHM`、`PLUGINS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
