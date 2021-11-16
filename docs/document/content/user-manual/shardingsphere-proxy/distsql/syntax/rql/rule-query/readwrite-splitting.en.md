+++
title = "Readwrite-Splitting"
weight = 3
+++

## Syntax

```sql
SHOW READWRITE_SPLITTING RULES [FROM schemaName]
```

## Return Value Description

| Column                      | Description                          |
| --------------------------- | ------------------------------------ |
| name                        | Rule name                            |
| auto_aware_data_source_name | Auto-Aware discovery data source name（Display configuration dynamic readwrite splitting rules）|
| write_data_source_name      | Write data source name                |
| read_data_source_names      | Read data source name list            |
| load_balancer_type          | Load balance algorithm type           |
| load_balancer_props         | Load balance algorithm parameter      |

## Example

*Static Readwrite Splitting Rules*
```sql
mysql> show readwrite_splitting rules;
+------------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| name       | auto_aware_data_source_name | write_data_source_name | read_data_source_names | load_balancer_type | load_balancer_props |
+------------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| ms_group_0 | NULL                        | ds_primary             | ds_slave_0, ds_slave_1 | random             |                     |
+------------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
1 row in set (0.00 sec)
```

*Dynamic Readwrite Splitting Rules*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| name  | auto_aware_data_source_name | write_data_source_name | read_data_source_names | load_balancer_type | load_balancer_props |
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| pr_ds | ms_group_0                  | NULL                   |                        | random             | read_weight=2:1     |
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

*Static Readwrite Splitting Rules And Dynamic Readwrite Splitting Rules*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| name  | auto_aware_data_source_name | write_data_source_name | read_data_source_names | load_balancer_type | load_balancer_props |
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
| pr_ds | ms_group_0                  | write_ds               | read_ds_0, read_ds_1   | random             | read_weight=2:1     |
+-------+-----------------------------+------------------------+------------------------+--------------------+---------------------+
1 row in set (0.00 sec)
```
