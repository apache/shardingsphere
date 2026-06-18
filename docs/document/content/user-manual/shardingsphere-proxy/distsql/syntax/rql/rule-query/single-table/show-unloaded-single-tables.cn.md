+++
title = "SHOW UNLOADED SINGLE TABLES"
weight = 4
+++

### 描述

`SHOW UNLOADED SINGLE TABLES` 语法用于查询未加载的单表。

### 语法

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showUnloadedSingleTables::=
  'SHOW' 'UNLOADED' 'SINGLE' 'TABLES'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列                | 说明                 |
|-------------------|---------------------|
| table_name        | 单表名称             |
| storage_unit_name | 单表所在的存储单元名称 |


### 示例

- 查询未加载的单表

```sql
SHOW UNLOADED SINGLE TABLES;
```

```sql
mysql> SHOW UNLOADED SINGLE TABLES;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_single   | ds_1              |
+------------+-------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`UNLOADED`、`SINGLE`、`TABLES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

