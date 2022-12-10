+++
title = "SHOW COMPUTE NODE INFO"
weight = 2
+++

### Description

The `SHOW COMPUTE NODE INFO` syntax is used to query current proxy instance information.
### Syntax

```sql
ShowComputeNodeInfo ::=
  'SHOW' 'COMPUTE' 'NODE' 'INFO'
```

### Return Value Description

| Columns        | Description           |
|----------------|-----------------------|
| instance_id    | proxy instance id     |
| host           | host address          |
| port           | port number           |
| status         | proxy instance status |
| mode_type      | proxy instance mode   |
| worker_id      | worker id             |
| labels         | labels                |

### Example

- Query current proxy instance information

```sql
SHOW COMPUTE NODE INFO;
```

```sql
mysql> SHOW COMPUTE NODE INFO;
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| instance_id                          | host          | port | status | mode_type | worker_id | labels |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| 734bb036-b15d-4af0-be87-2372d8b6a0cd | 192.168.5.163 | 3307 | OK     | Cluster   | -1        |        |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `COMPUTE`, `NODE`, `INFO`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
