+++
title = "SHOW IMPLEMENTATIONS OF SPI"
weight = 7
+++

### 描述

`SHOW IMPLEMENTATIONS OF 'service provider interface'` 语法用于查询指定的 `SPI` 接口所有具体的实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showServiceProviderImplementations ::=
  'SHOW' 'IMPLEMENTATIONS' 'OF' serviceProviderInterface

serviceProviderInterface ::=
  identifier | string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列    | 说明      |
|------|---------|
| name | 实现类名称   |
| type | 类型      |
| class_path | 实现类完整路径 |

### 示例

- 查询 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` 接口的所有实现类

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

### 补充说明

针对一些常用的 `SPI` 接口实现，ShardingSphere 提供了语法糖功能，可以简化操作，目前已提供的语法糖功能的 `SPI` 接口如下：

- 查询 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` 接口实现：[SHOW SHARDING ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/sharding/show-sharding-algorithm-implementations)
- 查询 `org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm` 接口实现：[SHOW READ QUERY LOAD BALANCE ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/readwrite-splitting/show-read-query-load-balance-algorithm-implementations)
- 查询 `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` 接口实现：[SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/encrypt/show-encrypt-algorithm-implementations)
- 查询 `org.apache.shardingsphere.mask.spi.MaskAlgorithm` 接口实现：[SHOW MASK ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/mask/show-mask-algorithm-implementations)
- 查询 `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` 接口实现：[SHOW SHADOW ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/shadow/show-shadow-algorithm-implementations)
- 查询 `org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm` 接口实现：[SHOW KEY GENERATE ALGORITHM IMPLEMENTATIONS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-implementation/show-key-generate-algorithm-implementations)

### 保留字

`SHOW`、`IMPLEMENTATIONS`、`OF`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
