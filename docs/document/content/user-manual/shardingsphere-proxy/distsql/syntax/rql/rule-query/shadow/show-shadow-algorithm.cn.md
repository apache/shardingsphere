+++
title = "SHOW SHADOW ALGORITHMS"
weight = 3
+++

### 描述

`SHOW SHADOW ALGORITHMS` 语法用于查询指定逻辑库中的影子算法。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowEncryptAlgorithm::=
  'SHOW' 'SHADOW' 'ALGORITHMS' ('FROM' databaseName)?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                     | 说明     |
|-----------------------|--------|
| shadow_algorithm_name | 影子算法名称 |
| type                  | 算法类型   |
| props                 | 算法参数   |
| is_default            | 是否默认   |

### 示例

- 查询指定逻辑库中的影子算法

```sql
SHOW SHADOW ALGORITHMS FROM shadow_db;
```

```sql
mysql> SHOW SHADOW ALGORITHMS FROM shadow_db;
+-------------------------+-------------+-----------------------------------------+------------+
| shadow_algorithm_name   | type        | props                                   | is_default |
+-------------------------+-------------+-----------------------------------------+------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 | false      |
+-------------------------+-------------+-----------------------------------------+------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的影子算法

```sql
SHOW SHADOW ALGORITHMS;
```

```sql
mysql> SHOW SHADOW ALGORITHMS;
+-------------------------+-------------+-----------------------------------------+------------+
| shadow_algorithm_name   | type        | props                                   | is_default |
+-------------------------+-------------+-----------------------------------------+------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 | false      |
+-------------------------+-------------+-----------------------------------------+------------+
1 row in set (0.00 sec)
```


### 保留字

`SHOW`、`SHADOW`、`ALGORITHMS`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

