+++
title = "SHOW READ QUERY LOAD BALANCE ALGORITHM PLUGINS"
weight = 3
+++

### Description

The `SHOW READ QUERY LOAD BALANCE ALGORITHM PLUGINS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.infra.algorithm.load.balancer.core.LoadBalanceAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showReadQueryLoadBalanceAlgorithmPlugins ::=
  'SHOW' 'READ' 'QUERY' 'LOAD' 'BALANCE' 'ALGORITHM' 'PLUGINS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns      | Description  |
|--------------|--------------|
| type         | type         |
| type_aliases | type aliases |
| description  | description  |

### Example

- Query all the implementations for `org.apache.shardingsphere.infra.algorithm.load.balancer.core.LoadBalanceAlgorithm` interface

```sql
SHOW READ QUERY LOAD BALANCE ALGORITHM PLUGINS
```

```sql
SHOW READ QUERY LOAD BALANCE ALGORITHM PLUGINS;
+-------------+--------------+-------------+
| type        | type_aliases | description |
+-------------+--------------+-------------+
| ROUND_ROBIN |              |             |
| RANDOM      |              |             |
| WEIGHT      |              |             |
+-------------+--------------+-------------+
3 rows in set (0.03 sec)
```

### Reserved word

`SHOW`, `READ`, `QUERY`, `LOAD`, `BALANCE`, `ALGORITHM`, `PLUGINS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
