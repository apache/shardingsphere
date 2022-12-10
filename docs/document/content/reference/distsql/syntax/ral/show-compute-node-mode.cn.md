+++
title = "SHOW COMPUTE NODE MODE"
weight = 3
+++

### 描述

`SHOW COMPUTE NODE MODE` 语法用于查询当前 proxy 的模式配置信息

### 语法

```sql
ShowComputeNodeMode ::=
  'SHOW' 'COMPUTE' 'NODE' 'MODE'
```

### 返回值说明

| 列           | 说明                     |
|--------------|-------------------------|
| type         | proxy 模式类型           |
| repository   | proxy 持久化仓库类型      |
| props        | proxy 持久化仓库属性参数  |

### 示例

- 查询当前 proxy 实例模式配置信息

```sql
SHOW COMPUTE NODE MODE;
```

```sql
mysql> SHOW COMPUTE NODE MODE;
+---------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| type    | repository | props                                                                                                                                                                  |
+---------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Cluster | ZooKeeper  | {"operationTimeoutMilliseconds":500,"timeToLiveSeconds":60,"maxRetries":3,"namespace":"governance_ds","server-lists":"localhost:2181","retryIntervalMilliseconds":500} |
+---------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`COMPUTE`、`NODE`、`MODE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)