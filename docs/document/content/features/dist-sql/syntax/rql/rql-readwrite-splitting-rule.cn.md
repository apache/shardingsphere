+++
title = "读写分离"
weight = 3
+++

## 定义

```sql
SHOW READWRITE_SPLITTING RULES [FROM schemaName]               
```

## 说明

| 列                      | 说明                                  |
| ----------------------- | ------------------------------------ |
| name                    | 规则名称                               |
| autoAwareDataSourceName | 自动发现数据源名称（配置动态读写分离规则显示）|
| writeDataSourceName     | 写数据源名称                            |
| readDataSourceNames     | 读数据源名称列表                         |
| loadBalancerType        | 负载均衡算法类型                         |
| loadBalancerProps       | 负载均衡算法参数                         |

## 示例

*静态读写分离规则*
```sql
mysql> show readwrite_splitting rules;
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
| name       | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames      | loadBalancerType | loadBalancerProps      |
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
| ms_group_0 | NULL                    | ds_primary          |  ds_slave_0, ds_slave_1  | random           |                        |
+------------+-------------------------+---------------------+--------------------------+------------------+------------------------+
1 row in set (0.00 sec)
```

*动态读写分离规则*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
| name  | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames | loadBalancerType | loadBalancerProps      |
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
| pr_ds | ms_group_0              | NULL                |                     | random           |  read_weight=2:1}      |
+-------+-------------------------+---------------------+---------------------+------------------+------------------------+
1 row in set (0.01 sec)
```

*静态读写分离规则和动态读写分离规则*
```sql
mysql> show readwrite_splitting rules from readwrite_splitting_db;
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
| name  | autoAwareDataSourceName | writeDataSourceName | readDataSourceNames    | loadBalancerType | loadBalancerProps      |
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
| pr_ds | ms_group_0              | write_ds            |  read_ds_0, read_ds_1  | random           |  read_weight=2:1       |
+-------+-------------------------+---------------------+------------------------+------------------+------------------------+
1 row in set (0.00 sec)
```
