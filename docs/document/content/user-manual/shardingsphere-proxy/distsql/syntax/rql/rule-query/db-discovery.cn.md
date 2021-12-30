+++
title = "数据库发现"
weight = 4
+++

## 语法说明

```sql
SHOW DB_DISCOVERY RULES [FROM schemaName]

SHOW DB_DISCOVERY TYPES [FROM schemaName]

SHOW DB_DISCOVERY HEARTBEATS [FROM schemaName]
```

## 返回值说明

### DB Discovery Rule

| 列                       | 说明            |
| ------------------------ | --------------- |
| name                     | 规则名称         |
| data_source_names        | 数据源名称列表    |
| primary_data_source_name | 主数据源名称      |
| discovery_type           | 数据库发现服务类型 |
| discovery_heartbeat      | 数据库发现服务心跳 |

### DB Discovery Type

| 列                       | 说明            |
| ------------------------ | ---------------|
| name                     | 类型名称        |
| type                     | 类型种类         |
| props                    | 类型参数         |

### DB Discovery Heartbeat

| 列                       | 说明            |
| ------------------------ | ---------------|
| name                     | 心跳名称        |
| props                    | 心跳参数        |

## 示例

*DB Discovery Rule*

```sql
mysql> show db_discovery rules;
+------------+-------------------+--------------------------+------------------------------------------------------------------+------------------------------------------------------------------+
| name       | data_source_names | primary_data_source_name | discovery_type                                                   | discovery_heartbeat                                              |
+------------+-------------------+--------------------------+------------------------------------------------------------------+------------------------------------------------------------------+
| ha_group_0 | ds_0,ds_1,ds_2    |        ds_0              | {name=ha_group_0_mgr, type=mgr, props={group-name=92504d5b-6dec}} | {name=ha_group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+------------+-------------------+--------------------------+------------------------------------------------------------------+------------------------------------------------------------------+
1 row in set (0.20 sec)
```

*DB Discovery Type*

```sql
mysql> show db_discovery types;
+----------------+------+---------------------------+
| name           | type | props                     |
+----------------+------+---------------------------+
| ha_group_0_mgr | mgr  | {group-name=92504d5b-6dec} |
+----------------+------+---------------------------+
1 row in set (0.01 sec)
```

*DB Discovery Heartbeat*

```sql
mysql> show db_discovery heartbeats;
+----------------------+-------------------------------+
| name                 | props                         |
+----------------------+-------------------------------+
| ha_group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+----------------------+-------------------------------+
1 row in set (0.01 sec)
```