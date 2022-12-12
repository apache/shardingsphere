+++
title = "SHOW DB_DISCOVERY RULES"
weight = 2
+++

### 描述

`SHOW DB_DISCOVERY RULES` 语法用于查询指定逻辑库中的数据库发现规则。

### 语法

```
ShowDatabaseDiscoveryRule::=
  'SHOW' 'DB_DISCOVERY' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                       | 说明            |
| ------------------------ | -------------- |
| group_name               | 数据库发现规则名称|
| data_source_names        | 数据源名称列表    |
| primary_data_source_name | 主数据源名称      |
| discovery_type           | 数据库发现服务类型 |
| discovery_heartbeat      | 数据库发现服务心跳 |


### 示例

- 查询指定逻辑库中的数据库发现规则

```sql
SHOW DB_DISCOVERY RULES FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY RULES FROM test1;
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_name | data_source_names | primary_data_source_name | discovery_type                                                                                    | discovery_heartbeat                                             |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_0    | ds_0,ds_1,ds_2    | ds_0                     | {name=group_0_MySQL.MGR, type=MySQL.MGR, props={group-name=558edd3c-02ec-11ea-9bb3-080027e39bd2}} | {name=group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的数据库发现规则

```sql
SHOW DB_DISCOVERY RULES;
```

```sql
mysql> SHOW DB_DISCOVERY RULES;
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_name | data_source_names | primary_data_source_name | discovery_type                                                                                    | discovery_heartbeat                                             |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_0    | ds_0,ds_1,ds_2    | ds_0                     | {name=group_0_MySQL.MGR, type=MySQL.MGR, props={group-name=558edd3c-02ec-11ea-9bb3-080027e39bd2}} | {name=group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
1 row in set (0.03 sec)
```

### 保留字

`SHOW`、`DB_DISCOVERY`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

