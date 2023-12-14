+++
title = "SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### Description

The `SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showReadQueryLoadBalanceAlgorithmImplementations ::=
  'SHOW' 'READ' 'QUERY' 'LOAD' 'BALANCE' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns     | Description                           |
|-------------|---------------------------------------|
| name        | class name of the implementation      |
| type        | type of the implementation            |
| class_path  | full class name of the implementation |

### Example

- Query all the implementations for `org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm` interface

```sql
SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS;
+-----------------------------------------+-------------+------------------------------------------------------------------------------------------------------------+
| name                                    | type        | class_path                                                                                                 |
+-----------------------------------------+-------------+------------------------------------------------------------------------------------------------------------+
| RoundRobinReadQueryLoadBalanceAlgorithm | ROUND_ROBIN | org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RoundRobinReadQueryLoadBalanceAlgorithm |
| RandomReadQueryLoadBalanceAlgorithm     | RANDOM      | org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm     |
| WeightReadQueryLoadBalanceAlgorithm     | WEIGHT      | org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReadQueryLoadBalanceAlgorithm     |
+-----------------------------------------+-------------+------------------------------------------------------------------------------------------------------------+
3 rows in set (0.03 sec)
```

### Reserved word

`SHOW`、`READ`、`QUERY`、`LOAD`、`BALANCE`、`ALGORITHM`、`IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
