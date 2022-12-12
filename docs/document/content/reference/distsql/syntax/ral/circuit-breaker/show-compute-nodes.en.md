+++
title = "SHOW COMPUTE NODES"
weight = 3
+++

### Description

The `SHOW COMPUTE NODES` syntax is used to query proxy instance information.
### Syntax

```sql
ShowComputeNodes ::=
  'SHOW' 'COMPUTE' 'NODES'
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

- Query proxy instance information

```sql
SHOW COMPUTE NODES;
```

```sql
mysql> SHOW COMPUTE NODES;
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| instance_id                          | host          | port | status | mode_type | worker_id | labels |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| 734bb036-b15d-4af0-be87-2372d8b6a0cd | 192.168.5.163 | 3307 | OK     | Cluster   | -1        |        |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `COMPUTE`, `NODES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
