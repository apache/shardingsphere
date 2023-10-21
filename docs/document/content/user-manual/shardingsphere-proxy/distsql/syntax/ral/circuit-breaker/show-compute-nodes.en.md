+++
title = "SHOW COMPUTE NODES"
weight = 3
+++

### Description

The `SHOW COMPUTE NODES` syntax is used to query compute nodes information.
### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowComputeNodes ::=
  'SHOW' 'COMPUTE' 'NODES'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns       | Description   |
|---------------|---------------|
| instance_id   | instance id   |
| instance_type | instance type |
| host          | host          |
| port          | port          |
| status        | status        |
| mode_type     | mode type     |
| worker_id     | worker id     |
| labels        | labels        |
| version       | version       |

### Example

```sql
mysql> SHOW COMPUTE NODES;
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
| instance_id                          | instance_type | host       | port | status | mode_type  | worker_id | labels | version  |
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
| 3e84d33e-cb97-42f2-b6ce-f78fea0ded89 | PROXY         | 127.0.0.1  | 3307 | OK     | Cluster    | -1        |        | 5.4.2    |
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
1 row in set (0.01 sec)
```

### Dedicated Terminology

`SHOW`, `COMPUTE`, `NODES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
