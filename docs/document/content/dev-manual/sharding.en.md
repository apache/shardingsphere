+++
pre = "<b>6.7. </b>"
title = "Data Sharding"
weight = 7
chapter = true
+++

## ShardingAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAlgorithm.java)

### Definition

Sharding Algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| BoundaryBasedRangeShardingAlgorithm     | Boundary based range sharding algorithm                                 | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/BoundaryBasedRangeShardingAlgorithm.java) |
| VolumeBasedRangeShardingAlgorithm       | Volume based range sharding algorithm                                   | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/VolumeBasedRangeShardingAlgorithm.java) |
| ComplexInlineShardingAlgorithm          | Complex inline sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.complex.ComplexInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/complex/ComplexInlineShardingAlgorithm.java) |
| AutoIntervalShardingAlgorithm           | Mutable interval sharding algorithm                                     | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/AutoIntervalShardingAlgorithm.java) |
| ClassBasedShardingAlgorithm             | Class based sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/classbased/ClassBasedShardingAlgorithm.java) |
| HintInlineShardingAlgorithm             | Hint inline sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/hint/HintInlineShardingAlgorithm.java) |
| IntervalShardingAlgorithm               | Fixed interval sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/IntervalShardingAlgorithm.java) |
| HashModShardingAlgorithm                | Hash modulo sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/HashModShardingAlgorithm.java) |
| InlineShardingAlgorithm                 | Inline sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/InlineShardingAlgorithm.java) |
| ModShardingAlgorithm                    | Modulo sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/ModShardingAlgorithm.java) |
| CosIdModShardingAlgorithm               | Modulo sharding algorithm provided by CosId                             | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.mod.CosIdModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/mod/CosIdModShardingAlgorithm.java) |
| CosIdIntervalShardingAlgorithm          | Fixed interval sharding algorithm provided by CosId                     | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.CosIdIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/interval/CosIdIntervalShardingAlgorithm.java) |
| CosIdSnowflakeIntervalShardingAlgorithm | Snowflake key-based fixed interval sharding algorithm provided by CosId | [`org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.CosIdSnowflakeIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/sharding/interval/CosIdSnowflakeIntervalShardingAlgorithm.java) |

## KeyGenerateAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-api/src/main/java/org/apache/shardingsphere/sharding/spi/KeyGenerateAlgorithm.java)

### Definition

Distributed Key Generating Algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| SnowflakeKeyGenerateAlgorithm      | Snowflake key generate algorithm                   | [`org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/keygen/SnowflakeKeyGenerateAlgorithm.java) |
| UUIDKeyGenerateAlgorithm           | UUID key generate algorithm                        | [`org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/keygen/UUIDKeyGenerateAlgorithm.java) |
| CosIdKeyGenerateAlgorithm          | CosId key generate algorithm                       | [`org.apache.shardingsphere.sharding.cosid.algorithm.keygen.CosIdKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/keygen/CosIdKeyGenerateAlgorithm.java) |
| CosIdSnowflakeKeyGenerateAlgorithm | Snowflake key generate algorithm provided by CosId | [`org.apache.shardingsphere.sharding.cosid.algorithm.keygen.CosIdSnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-cosid/src/main/java/org/apache/shardingsphere/sharding/cosid/algorithm/keygen/CosIdSnowflakeKeyGenerateAlgorithm.java) |
| NanoIdKeyGenerateAlgorithm         | NanoId key generate algorithm                      | [`org.apache.shardingsphere.sharding.nanoid.algorithm.keygen.NanoIdKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-plugin/shardingsphere-sharding-nanoid/src/main/java/org/apache/shardingsphere/sharding/nanoid/algorithm/keygen/NanoIdKeyGenerateAlgorithm.java) |

## ShardingAuditAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAuditAlgorithm.java)

### Definition

Sharding audit algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| DMLShardingConditionsShardingAuditAlgorithm      | Prohibit DML auditing algorithm without sharding conditions   | [`org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/algorithm/audit/DMLShardingConditionsShardingAuditAlgorithm.java) |

## DatetimeService

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datetime.DatetimeService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-datetime/shardingsphere-infra-datetime-spi/src/main/java/org/apache/shardingsphere/infra/datetime/DatetimeService.java)

### Definition

Obtain the current date for routing definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| DatabaseDatetimeService | Get the current time from the database for routing           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-datetime/shardingsphere-infra-datetime-type/shardingsphere-database-datetime/src/main/java/org/apache/shardingsphere/datetime/database/DatabaseDatetimeService.java) |
| SystemDatetime          | Get the current time from the application system for routing | [`org.apache.shardingsphere.datetime.system.SystemDatetimeService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-datetime/shardingsphere-infra-datetime-type/shardingsphere-system-datetime/src/main/java/org/apache/shardingsphere/datetime/system/SystemDatetimeService.java) |
