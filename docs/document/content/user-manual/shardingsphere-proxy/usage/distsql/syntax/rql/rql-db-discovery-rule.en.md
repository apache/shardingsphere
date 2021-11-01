+++
title = "DB Discovery"
weight = 5
+++

## Definition

```sql
SHOW DB_DISCOVERY RULES [FROM schemaName]
```

## Description

| Column                | Description                            |
| --------------------- | -------------------------------------- |
| name                  | Rule name                              |
| dataSourceNames       | Data source name list                  |
| primaryDataSourceName | Primary data source name               |
| discoverType          | Database discovery service type        |
| discoverProps         | Database discovery service parameters  |

## Example

```sql
mysql> show db_discovery rules from database_discovery_db;
+-------+---------------------+--------------------------+---------------+------------------------------------------------------------------------------------------------------------+
| name  | data_source_names   | primary_data_source_name | discover_type | discover_props                                                                                             |
+-------+---------------------+--------------------------+---------------+------------------------------------------------------------------------------------------------------------+
| pr_ds | ds_0, ds_1, ds_2    | ds_0                     | MGR           | keepAliveCron=0/50 * * * * ?, zkServerLists=localhost:2181, groupName=b13df29e-90b6-11e8-8d1b-525400fc3996 |
+-------+---------------------+--------------------------+---------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```
