+++
title = "PREVIEW SQL"
weight = 3
+++

### 描述

`PREVIEW SQL` 语法用于预览 `SQL` 执行计划  

### 语法

```sql
PreviewSql ::=
  'PREVIEW' sqlStatement 
```

### 返回值说明

| 列                | 说明              |
|-------------------|------------------|
| data_source_name  | 存储单元名称       |
| actual_sql        | 实际执行 SQL 语句  |

### 示例

- 预览 `SQL` 执行计划 

```sql
PREVIEW SELECT * FROM t_order;
```

```sql
mysql> PREVIEW SELECT * FROM t_order;
+------------------+-----------------------+
| data_source_name | actual_sql            |
+------------------+-----------------------+
| su_1             | SELECT * FROM t_order |
+------------------+-----------------------+
1 row in set (0.18 sec)
```

### 保留字

`PREVIEW`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)