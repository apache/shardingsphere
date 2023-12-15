+++
title = "SHOW SHARDING ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### 描述

`SHOW SHARDING ALGORITHM IMPLEMENTATIONS` 语法用于查询 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` 接口所有具体的实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showShardingAlgorithmImplementations ::=
  'SHOW' 'SHARDING' 'ALGORITHM' 'IMPLEMENTATIONS'
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

### 保留字

`SHOW`、`SHARDING`、`ALGORITHM`、`IMPLEMENTATIONS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)