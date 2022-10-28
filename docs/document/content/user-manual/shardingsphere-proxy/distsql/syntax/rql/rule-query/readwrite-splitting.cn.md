+++
title = "读写分离"
weight = 3
+++

## 语法说明

```sql
SHOW READWRITE_SPLITTING RULES [FROM databaseName]
```

## 参数解释

| 名称               | 数据类型     | 说明        |
|:------------------|:------------|:-----------|
| databaseName      | IDENTIFIER  | 数据库名称   |

## 返回值说明

| 列                                | 说明                                   |
|----------------------------------|----------------------------------------|
| name                             | 规则名称                                 |
| auto_aware_data_source_name      | 自动发现数据源名称（配置动态读写分离规则显示） |
| write_data_source_query_enabled  | 读库全部下线，主库是否承担读流量             |
| write_storage_unit_name          | 写数据源名称                              |
| read_storage_unit_names          | 读数据源名称列表                           |
| load_balancer_type               | 负载均衡算法类型                           |
| load_balancer_props              | 负载均衡算法参数                           |

## 示例

*静态读写分离规则*
```sql
mysql> SHOW READWRITE_SPLITTING RULES;
+------------+-----------------------------+-------------------------+-------------------------+--------------------+---------------------+
| name       | auto_aware_data_source_name | write_storage_unit_name | read_storage_unit_names | load_balancer_type | load_balancer_props |
+------------+-----------------------------+-------------------------+-------------------------+--------------------+---------------------+
| ms_group_0 |                             | ds_primary              | ds_slave_0, ds_slave_1  | random             |                     |
+------------+-----------------------------+-------------------------+-------------------------+--------------------+---------------------+
1 row in set (0.00 sec)
```

*动态读写分离规则*
```sql
mysql> SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db;
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+--------------------+---------------------+
| name         | auto_aware_data_source_name | write_data_source_query_enabled | write_storage_unit_name | read_storage_unit_names | load_balancer_type | load_balancer_props |
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+--------------------+---------------------+
| readwrite_ds | ms_group_0                  |                                 |                         |                         | random             | read_weight=2:1     |
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

*静态读写分离规则和动态读写分离规则*
```sql
mysql> SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db;
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+---------------------+---------------------+
| name         | auto_aware_data_source_name | write_data_source_query_enabled | write_storage_unit_name | read_storage_unit_names | load_balancer_type  | load_balancer_props |
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+---------------------+---------------------+
| readwrite_ds | ms_group_0                  |                                 | write_ds                | read_ds_0, read_ds_1    | random              | read_weight=2:1     |
+--------------+-----------------------------+---------------------------------+-------------------------+-------------------------+---------------------+---------------------+
1 row in set (0.00 sec)
```
