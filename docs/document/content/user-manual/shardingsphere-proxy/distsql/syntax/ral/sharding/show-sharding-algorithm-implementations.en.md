+++
title = "SHOW SHARDING ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### Description

The `SHOW SHARDING ALGORITHM IMPLEMENTATIONS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showShardingAlgorithmImplementations ::=
  'SHOW' 'SHARDING' 'ALGORITHM' 'IMPLEMENTATIONS'
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
SHOW SHARDING ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW SHARDING ALGORITHM IMPLEMENTATIONS;
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
10 rows in set (0.27 sec)
```

### Reserved word

`SHOW`、`SHARDING`、`ALGORITHM`、`IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
