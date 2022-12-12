+++
title = "SHOW COMPUTE NODE MODE"
weight = 3
+++

### Description

The `SHOW COMPUTE NODE MODE` syntax is used to query current proxy instance mode configuration information.
### Syntax

```sql
ShowComputeNodeInfo ::=
  'SHOW' 'COMPUTE' 'NODE' 'MODE'
```

### Return Value Description

| Columns      | Description                        |
|--------------|------------------------------------|
| type         | type of proxy mode configuration   |
| repository   | type of persist repository         |
| props        | properties of persist repository   |

### Example

- Query current proxy instance mode configuration information

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

### Reserved word

`SHOW`, `COMPUTE`, `NODE`, `MODE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
