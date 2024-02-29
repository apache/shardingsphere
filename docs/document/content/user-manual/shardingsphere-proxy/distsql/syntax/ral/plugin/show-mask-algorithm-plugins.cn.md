+++
title = "SHOW MASK ALGORITHM PLUGINS"
weight = 5
+++

### 描述

`SHOW MASK ALGORITHM PLUGINS` 语法用于查询 `org.apache.shardingsphere.mask.spi.MaskAlgorithm` 接口的所有实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showMaskAlgorithmPlugins ::=
  'SHOW' 'MASK' 'ALGORITHM' 'PLUGINS'
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

- 查询 `org.apache.shardingsphere.mask.spi.MaskAlgorithm` 接口的所有实现类

```sql
SHOW MASK ALGORITHM PLUGINS
```

```sql
SHOW MASK ALGORITHM PLUGINS;
+------------------------------+--------------+-------------+
| type                         | type_aliases | description |
+------------------------------+--------------+-------------+
| MD5                          |              |             |
| KEEP_FIRST_N_LAST_M          |              |             |
| KEEP_FROM_X_TO_Y             |              |             |
| MASK_AFTER_SPECIAL_CHARS     |              |             |
| MASK_BEFORE_SPECIAL_CHARS    |              |             |
| MASK_FIRST_N_LAST_M          |              |             |
| MASK_FROM_X_TO_Y             |              |             |
| GENERIC_TABLE_RANDOM_REPLACE |              |             |
+------------------------------+--------------+-------------+
8 rows in set (0.13 sec)
```

### 保留字

`SHOW`、`MASK`、`ALGORITHM`、`PLUGINS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
