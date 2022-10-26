+++
pre = "<b>5.7. </b>"
title = "Data Sharding"
weight = 7
chapter = true
+++

## ShardingAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAlgorithm.java)

### Definition

Sharding Algorithm definition

### Implementation classes

| *Configuration Type*     | *Auto Create Tables* |       *Description*                                                     | *Fully-qualified class name* |
| ------------------------ | -------------------- | ----------------------------------------------------------------------- | ---------------------------- |
| MOD                      | Y                    | Modulo sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/ModShardingAlgorithm.java) |
| HASH_MOD                 | Y                    | Hash modulo sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/HashModShardingAlgorithm.java) |
| BOUNDARY_RANGE           | Y                    | Boundary based range sharding algorithm                                 | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/BoundaryBasedRangeShardingAlgorithm.java) |
| VOLUME_RANGE             | Y                    | Volume based range sharding algorithm                                   | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/VolumeBasedRangeShardingAlgorithm.java) |
| AUTO_INTERVAL            | Y                    | Mutable interval sharding algorithm                                     | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/AutoIntervalShardingAlgorithm.java) |
| INTERVAL                 | N                    | Fixed interval sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/IntervalShardingAlgorithm.java) |
| CLASS_BASED              | N                    | Class based sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/classbased/ClassBasedShardingAlgorithm.java) |
| INLINE                   | N                    | Inline sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/InlineShardingAlgorithm.java) |
| COMPLEX_INLINE           | N                    | Complex inline sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.complex.ComplexInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/complex/ComplexInlineShardingAlgorithm.java) |
| HINT_INLINE              | N                    | Hint inline sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/hint/HintInlineShardingAlgorithm.java) |
| COSID_MOD                | N                    | Modulo sharding algorithm provided by CosId                             | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.mod.CosIdModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/mod/CosIdModShardingAlgorithm.java) |
| COSID_INTERVAL           | N                    | Fixed interval sharding algorithm provided by CosId                     | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.CosIdIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/interval/CosIdIntervalShardingAlgorithm.java) |
| COSID_INTERVAL_SNOWFLAKE | N                    | Snowflake key-based fixed interval sharding algorithm provided by CosId | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.CosIdSnowflakeIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/interval/CosIdSnowflakeIntervalShardingAlgorithm.java) |

## KeyGenerateAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/KeyGenerateAlgorithm.java)

### Definition

Distributed Key Generating Algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                                      | *Fully-qualified class name* |
| -------------------- | -------------------------------------------------- | ---------------------------- |
| SNOWFLAKE            | Snowflake key generate algorithm                   | [`org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/keygen/SnowflakeKeyGenerateAlgorithm.java) |
| UUID                 | UUID key generate algorithm                        | [`org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/keygen/UUIDKeyGenerateAlgorithm.java) |
| NANOID               | NanoId key generate algorithm                      | [`org.apache.shardingsphere.sharding.nanoid.algorithm.keygen.NanoIdKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/nanoid/src/main/java/org/apache/shardingsphere/sharding/nanoid/algorithm/keygen/NanoIdKeyGenerateAlgorithm.java) |
| COSID                | CosId key generate algorithm                       | [`org.apache.shardingsphere.sharding.cosid.algorithm.keygen.CosIdKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/keygen/CosIdKeyGenerateAlgorithm.java) |
| COSID_SNOWFLAKE      | Snowflake key generate algorithm provided by CosId | [`org.apache.shardingsphere.sharding.cosid.algorithm.keygen.CosIdSnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/plugin/cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/keygen/CosIdSnowflakeKeyGenerateAlgorithm.java) |

## ShardingAuditAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAuditAlgorithm.java)

### Definition

Sharding audit algorithm definition

### Implementation classes

| *Configuration Type*    | *Description*                                                 | *Fully-qualified class name* |
| ----------------------- | ------------------------------------------------------------- | ---------------------------- |
| DML_SHARDING_CONDITIONS | Prohibit DML auditing algorithm without sharding conditions   | [`org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/audit/DMLShardingConditionsShardingAuditAlgorithm.java) |

## DatetimeService

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datetime.DatetimeService`](https://github.com/apache/shardingsphere/blob/master/infra/datetime/spi/src/main/java/org/apache/shardingsphere/infra/datetime/DatetimeService.java)

### Definition

Obtain the current date for routing definition

### Implementation classes

| *Configuration Type*    | *Description*                                                | *Fully-qualified class name* |
| ----------------------- | ------------------------------------------------------------ | ---------------------------- |
| DatabaseDatetimeService | Get the current time from the database for routing           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/infra/datetime/type/database/src/main/java/org/apache/shardingsphere/datetime/database/DatabaseDatetimeService.java) |
| SystemDatetime          | Get the current time from the application system for routing | [`org.apache.shardingsphere.datetime.system.SystemDatetimeService`](https://github.com/apache/shardingsphere/blob/master/infra/datetime/type/system/src/main/java/org/apache/shardingsphere/datetime/system/SystemDatetimeService.java) |
