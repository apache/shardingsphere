+++
title = "SHOW MIGRATION LIST"
weight = 7

+++

### 描述

`SHOW MIGRATION LIST` 语法用于查询数据迁移作业列表。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMigrationList ::=
  'SHOW' 'MIGRATION' 'LIST'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列              | 说明           |
|----------------|--------------|
| id             | 数据迁移作业ID     |
| tables         | 迁移表          |
| active         | 数据迁移作业状态     |
| create_time    | 数据迁移作业创建时间   |
| stop_time      | 数据迁移作业停止时间   |
| job_item_count | 数据迁移作业分片数量   |
| job_sharding_nodes | 数据迁移作业分片运行节点 |

### 示例

- 查询数据迁移作业列表

```sql
SHOW MIGRATION LIST;
```

```sql
mysql> SHOW MIGRATION LIST;
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
| id                                         | tables              | active | create_time         | stop_time | job_item_count | job_sharding_nodes |
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
| j0102p00001d029afca1fd960d567fed6cddc9b4a2 | source_ds.t_order   | true   | 2022-10-31 18:18:24 |           | 1              | 10.7.5.76@-@27808  |
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
4 rows in set (0.06 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`LIST`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)