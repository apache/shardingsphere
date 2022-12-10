+++
title = "SHOW MIGRATION LIST"
weight = 8

+++

### 描述

`SHOW MIGRATION LIST` 语法用于查询数据迁移作业列表

### 语法

```sql
ShowMigrationList ::=
  'SHOW' 'MIGRATION' 'LIST'
```

### 返回值说明

| 列             | 说明                        |
|----------------|----------------------------|
| id             | 数据迁移作业ID               |
| tables         | 迁移表                      |
| job_item_count | 数据迁移作业分片数量          |
| active         | 数据迁移作业状态              |
| create_time    | 数据迁移作业创建时间          |
| stop_time      | 数据迁移作业停止时间          |

### 示例

- 查询数据迁移作业列表

```sql
SHOW MIGRATION LIST;
```

```sql
mysql> SHOW MIGRATION LIST;
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
| id                                    | tables  | job_item_count | active | create_time         | stop_time           |
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
| j01013a38b0184e07c864627b5bb05da09ee0 | t_order | 1              | false  | 2022-10-31 18:18:24 | 2022-10-31 18:18:31 |
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
1 row in set (0.28 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`LIST`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)