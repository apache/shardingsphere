+++
title = "DB Discovery"
weight = 5
+++

## Definition

```sql
SHOW DB_DISCOVERY RULES [FROM schemaName]
```

## Description

| Column          | Description           |
| --------------- | --------------------- |
| name            | Rule name             |
| dataSourceNames | Data source name list |
| discoverType    | Database discovery service type        |
| discoverProps   | Database discovery service parameters  |

## Example

```sql
mysql> show db_discovery rules from database_discovery_db;
+-------+--------------------+--------------+-------------------------------------------------------------------------------------------------------------+
| name  | dataSourceNames    | discoverType | discoverProps                                                                                               |
+-------+--------------------+--------------+-------------------------------------------------------------------------------------------------------------+
| pr_ds |  ds_0, ds_1, ds_2  | MGR          |  keepAliveCron=0/5 * * * * ?, zkServerLists=localhost:2181, groupName=92504d5b-6dec-11e8-91ea-246e9612aaf1  |
+-------+--------------------+--------------+-------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```
