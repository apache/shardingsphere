+++
title = "Readwrite-Splitting"
weight = 3
+++

## Definition

```sql
SHOW READWRITE_SPLITTING RULES [FROM schemaName]               
```

## Description

| Column                  | Description                          |
| ----------------------- | ------------------------------------ |
| name                    | Rule name                            |
| autoAwareDataSourceName | Auto-Aware discovery data source name（Display configuration dynamic read-write separation rules）|
| writeDataSourceName     | Write data source name                |
| readDataSourceNames     | Read data source name list            |
| loadBalancerType        | Load balance algorithm type           |
| loadBalancerProps       | Load balance algorithm parameter      |

## Example

*Static Read-write Separation Rules*
```sql
mysql> show readwrite_splitting rules;
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
| name       | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames      | loadBalancerType | loadBalancerProps      |
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
| ms_group_0 | NULL                    | ds_primary          |  ds_slave_0, ds_slave_1  | random           |                        |
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
1 row in set (0.00 sec)
```

*Dynamic Read-write Separation Rules*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
| name  | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames | loadBalancerType | loadBalancerProps      |
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
| pr_ds | ms_group_0              | NULL                |                     | random           |  read_weight=2:1}      |
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
1 row in set (0.01 sec)
```

*Static Read-write Separation Rules And Dynamic Read-write Separation Rules*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
| name  | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames    | loadBalancerType | loadBalancerProps      |
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
| pr_ds | ms_group_0              | write_ds            |  read_ds_0, read_ds_1  | random           |  read_weight=2:1       |
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
1 row in set (0.00 sec)
```
