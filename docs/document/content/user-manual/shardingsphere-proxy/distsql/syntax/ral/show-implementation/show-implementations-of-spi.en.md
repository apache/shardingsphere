+++
title = "SHOW IMPLEMENTATIONS OF SPI"
weight = 7
+++

### Description

The `SHOW IMPLEMENTATIONS OF 'service provider interface'` syntax is used to query all the implementations of a `SPI`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showServiceProviderImplementations ::=
  'SHOW' 'IMPLEMENTATIONS' 'OF' serviceProviderInterface

serviceProviderInterface ::=
  identifier | string
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

- Query all the implementations for `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` interface

```sql
SHOW IMPLEMENTATIONS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm'
```

```sql
SHOW IMPLEMENTATIONS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm';
+-------------------------------------+----------------+-------------------------------------------------------------------------------------------------+
| name                                | type           | class_path                                                                                      |
+-------------------------------------+----------------+-------------------------------------------------------------------------------------------------+
| ModShardingAlgorithm                | MOD            | org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm                  |
| HashModShardingAlgorithm            | HASH_MOD       | org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm              |
| VolumeBasedRangeShardingAlgorithm   | VOLUME_RANGE   | org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm   |
| BoundaryBasedRangeShardingAlgorithm | BOUNDARY_RANGE | org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm |
| AutoIntervalShardingAlgorithm       | AUTO_INTERVAL  | org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm    |
| IntervalShardingAlgorithm           | INTERVAL       | org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm        |
| ClassBasedShardingAlgorithm         | CLASS_BASED    | org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm    |
| InlineShardingAlgorithm             | INLINE         | org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm            |
| ComplexInlineShardingAlgorithm      | COMPLEX_INLINE | org.apache.shardingsphere.sharding.algorithm.sharding.inline.ComplexInlineShardingAlgorithm     |
| HintInlineShardingAlgorithm         | HINT_INLINE    | org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm          |
+-------------------------------------+----------------+-------------------------------------------------------------------------------------------------+
10 rows in set (0.52 sec)
```

### Supplement

For some commonly used `SPI` interface implementations, ShardingSphere provides syntax sugar functions to simplify operations.

The currently provided `SPI` interfaces with syntactic sugar functions are as follows:

- Show implementations of `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`: [SHOW SHARDING ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/sharding/show-sharding-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm`: [SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/readwrite-splitting/show-read-query-load-balance-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`: [SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/encrypt/show-encrypt-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.mask.spi.MaskAlgorithm`: [SHOW MASK ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/mask/show-mask-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`: [SHOW SHADOW ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/shadow/show-shadow-algorithm-implementations)
- Show implementations of  `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm`: [SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/show-key-generate-algorithm-implementations)

### Reserved word

`SHOW`, `IMPLEMENTATIONS`, `OF`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
