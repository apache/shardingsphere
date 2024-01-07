+++
title = "SHOW COMPUTE NODE MODE"
weight = 6
+++

### Description

The `SHOW COMPUTE NODE MODE` syntax is used to query current proxy instance mode configuration information.
### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowComputeNodeInfo ::=
  'SHOW' 'COMPUTE' 'NODE' 'MODE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

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

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
