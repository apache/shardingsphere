+++
title = "DB Discovery"
weight = 4
+++

## Syntax

```sql
SHOW DB_DISCOVERY RULES [FROM databaseName]

SHOW DB_DISCOVERY TYPES [FROM databaseName]

SHOW DB_DISCOVERY HEARTBEATS [FROM databaseName]
```

## Return Value Description

### DB Discovery Rule

| Column                   | Description                            |
| ------------------------ | -------------------------------------- |
| group_name               | Rule name                              |
| data_source_names        | Data source name list                  |
| primary_data_source_name | Primary data source name               |
| discovery_type           | Database discovery service type        |
| discovery_heartbeat      | Database discovery service heartbeat   |

### DB Discovery Type

| Column                   | Description     |
| ------------------------ | ----------------|
| name                     | Type name       |
| type                     | Type category   |
| props                    | Type properties |

### DB Discovery Heartbeat

| Column                   | Description           |
| ------------------------ | --------------------- |
| name                     | Heartbeat name        |
| props                    | Heartbeat properties  |

## Example

*DB Discovery Rule*

```sql
mysql> SHOW DB_DISCOVERY RULES;
+----------------------+-------------------+--------------------------+-----------------------------------------------------------------------------------+------------------------------------------------------------------------------+
| group_name           | data_source_names | primary_data_source_name | discovery_type                                                                    | discovery_heartbeat                                                          |
+----------------------+-------------------+--------------------------+-----------------------------------------------------------------------------------+------------------------------------------------------------------------------+
| db_discovery_group_0 | ds_0,ds_1,ds_2    |        ds_0              | {name=db_discovery_group_0_mgr, type=MySQL.MGR, props={group-name=92504d5b-6dec}} | {name=db_discovery_group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+----------------------+-------------------+--------------------------+-----------------------------------------------------------------------------------+------------------------------------------------------------------------------+
1 row in set (0.20 sec)
```

*DB Discovery Type*

```sql
mysql> SHOW DB_DISCOVERY TYPES;
+---------------------------+------------+------------------------------+
| name                      | type       | props                        |
+---------------------------+------------+------------------------------+
| db_discovery_group_0_mgr  | MySQL.MGR  | {group-name=92504d5b-6dec}   |
+---------------------------+------------+------------------------------+
1 row in set (0.01 sec)
```

*DB Discovery Heartbeat*

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS;
+--------------------------------+---------------------------------+
| name                           | props                           |
+--------------------------------+---------------------------------+
| db_discovery_group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+---------------------------------+---------------------------------+
1 row in set (0.01 sec)
```
