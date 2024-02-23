+++
title = "SHOW PLUGINS OF SPI"
weight = 7
+++

### Description

The `SHOW PLUGINS OF pluginClass` syntax is used to query all the implementations of a `SPI`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showPluginImplementations ::=
  'SHOW' 'PLUGINS' 'OF' pluginClass

pluginClass ::=
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

For some commonly used `SPI` interface implementations, ShardingSphere provides syntax sugar functions to simplify operations.

The currently provided `SPI` interfaces with syntactic sugar functions are as follows:

- Show implementations of `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`: [SHOW SHARDING ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/sharding/show-sharding-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm`: [SHOW READ QUERY LOAD BALANCE ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/readwrite-splitting/show-read-query-load-balance-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`: [SHOW ENCRYPT ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/encrypt/show-encrypt-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.mask.spi.MaskAlgorithm`: [SHOW MASK ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/mask/show-mask-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`: [SHOW SHADOW ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/shadow/show-shadow-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm`: [SHOW KEY GENERATE ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/show-key-generate-algorithm-implementations)

### Reserved word

`SHOW`, `PLUGINS`, `OF`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
