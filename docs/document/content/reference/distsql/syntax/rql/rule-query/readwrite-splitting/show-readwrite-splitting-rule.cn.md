+++
title = "SHOW READWRITE_SPLITTING RULES"
weight = 2
+++

### 描述

`SHOW READWRITE_SPLITTING RULES` 语法用于查询指定逻辑库中的读写分离规则。

### 语法

```
ShowReadWriteSplittingRule::=
  'SHOW' 'READWRITE_SPLITTING' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                              | 说明                                     |
| ------------------------------- | --------------------------------------- |
| name                            | 读写分离规则名称                          |
| auto_aware_data_source_name     | 自动发现数据源名称（配置动态读写分离规则显示）|
| write_data_source_query_enabled | 读库全部下线，主库是否承担读流量            |
| write_data_source_name          | 写数据源名称                             |
| read_data_source_names          | 读数据源名称列表                          |
| load_balancer_type              | 负载均衡算法类型                          |
| load_balancer_props             | 负载均衡算法参数                          |


### 示例

- 查询指定逻辑库中的读写分离规则

```sql
SHOW READWRITE_SPLITTING RULES FROM sharding_db;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULES FROM sharding_db;
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
| name       | auto_aware_data_source_name | write_data_source_query_enabled | write_data_source_name | read_data_source_names | load_balancer_type | load_balancer_props |
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
| ms_group_0 |                             |                                 | resource_1             | ds_0,ds_1              | random             |                     |
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的读写分离规则

```sql
SHOW READWRITE_SPLITTING RULES;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULES;
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
| name       | auto_aware_data_source_name | write_data_source_query_enabled | write_data_source_name | read_data_source_names | load_balancer_type | load_balancer_props |
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
| ms_group_0 |                             |                                 | resource_1             | ds_0,ds_1              | random             |                     |
+------------+-----------------------------+---------------------------------+------------------------+------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`READWRITE_SPLITTING`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

