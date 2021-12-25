+++
title = "DB Discovery"
weight = 4
+++

## Syntax

```sql
SHOW DB_DISCOVERY RULES [FROM schemaName]

SHOW DB_DISCOVERY TYPES [FROM schemaName]

SHOW DB_DISCOVERY HEARTBEATS [FROM schemaName]
```

## Return Value Description

### DB Discovery Rule

| Column                   | Description                            |
| ------------------------ | -------------------------------------- |
| name                     | Rule name                              |
| data_source_names        | Data source name list                  |
| primary_data_source_name | Primary data source name               |
| discover_type            | Database discovery service type        |
| discover_props           | Database discovery service parameters  |


### DB Discovery Type

| Column                   | Description     |
| ------------------------ | ----------------|
| name                     | Type name       |
| type                     | Type category   |
| props                    | Type properties |

### DB Discovery Heartbeat

| Column                   | Description           |
| ------------------------ | ----------------------|
| name                     | Heartbeat name        |
| props                    | Heartbeat properties  |

## Example

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
