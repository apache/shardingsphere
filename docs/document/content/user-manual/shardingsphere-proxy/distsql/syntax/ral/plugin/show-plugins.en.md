+++
title = "SHOW PLUGINS OF SPI"
weight = 1
+++

### Description

The `SHOW PLUGINS OF interfaceClass` syntax is used to query all the implementations of an interface.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showPluginImplementations ::=
  'SHOW' 'PLUGINS' 'OF' interfaceClass

interfaceClass ::=
  string
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

- Query all the implementations for `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` interface

```sql
SHOW PLUGINS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm'
```

```sql
SHOW PLUGINS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm';
+----------------+--------------+-------------+
| type           | type_aliases | description |
+----------------+--------------+-------------+
| MOD            |              |             |
| HASH_MOD       |              |             |
| VOLUME_RANGE   |              |             |
| BOUNDARY_RANGE |              |             |
| AUTO_INTERVAL  |              |             |
| INTERVAL       |              |             |
| CLASS_BASED    |              |             |
| INLINE         |              |             |
| COMPLEX_INLINE |              |             |
| HINT_INLINE    |              |             |
+----------------+--------------+-------------+
10 rows in set (0.52 sec)
```

### Supplement

For some commonly used interface implementations, ShardingSphere provides syntax sugar functions to simplify operations.

The currently provided syntax sugar are as follows:

- Show implementations of `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`: [SHOW SHARDING ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-sharding-algorithm-plugins/)
- Show implementations of `org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm`: [SHOW LOAD BALANCE ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-load-balance-algorithm-plugins/)
- Show implementations of `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`: [SHOW ENCRYPT ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-encrypt-algorithm-plugins/)
- Show implementations of `org.apache.shardingsphere.mask.spi.MaskAlgorithm`: [SHOW MASK ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-mask-algorithm-plugins/)
- Show implementations of `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`: [SHOW SHADOW ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-shadow-algorithm-plugins/)
- Show implementations of `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm`: [SHOW KEY GENERATE ALGORITHM PLUGINS](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-key-generate-algorithm-plugins/)

### Reserved word

`SHOW`, `PLUGINS`, `OF`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
