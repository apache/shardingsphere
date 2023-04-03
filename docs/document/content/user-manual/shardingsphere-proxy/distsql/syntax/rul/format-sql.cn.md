+++
title = "FORMAT SQL"
weight = 2
+++

### 描述

`FORMAT SQL` 语法用于解析并输出格式化后的 `SQL` 语句。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
FormatSql ::=
  'FORMAT' sqlStatement 
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列                | 说明         |
|------------------|------------|
| formatted_result | 格式化后的SQL语句 |

### 示例

- 解析并输出格式化后的 `SQL` 语句

```sql
FORMAT SELECT * FROM t_order;
```

```sql
mysql> FORMAT SELECT * FROM t_order;
+-------------------------+
| formatted_result        |
+-------------------------+
| SELECT *
FROM t_order; |
+-------------------------+
1 row in set (0.00 sec)
```

### 保留字

`FORMAT`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)