+++
title = "SHOW COMPUTE NODE INFO"
weight = 5
aliases = ["/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-compute-node-info/"]
+++

### Description

The `SHOW COMPUTE NODE INFO` syntax is used to query information about the current compute node.
### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowComputeNodeInfo ::=
  'SHOW' 'COMPUTE' 'NODE' 'INFO'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns        | Description           |
|----------------|-----------------------|
| instance_id    | compute node ID     |
| host           | host address          |
| port           | Proxy port number; `-1` for JDBC |
| status         | compute node status |
| mode_type      | compute node operating mode   |
| worker_id      | worker id             |
| version        | version               |

### Example

- Query information about the current compute node

```sql
SHOW COMPUTE NODE INFO;
```

```sql
mysql> SHOW COMPUTE NODE INFO;
+--------------------------------------+-----------+------+--------+-----------+-----------+---------------------------+
| instance_id                          | host      | port | status | mode_type | worker_id | version                   |
+--------------------------------------+-----------+------+--------+-----------+-----------+---------------------------+
| 3e84d33e-cb97-42f2-b6ce-f78fea0ded89 | 127.0.0.1 | 3307 | OK     | Cluster   | -1        | ${latest.release.version} |
+--------------------------------------+-----------+------+--------+-----------+-----------+---------------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `COMPUTE`, `NODE`, `INFO`

### Related links

- [Reserved word](/en/user-manual/distsql/syntax/reserved-word/)
