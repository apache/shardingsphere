+++
pre = "<b>5.3. </b>"
title = "Data Sharding"
weight = 3
chapter = true
+++

## ShardingAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAlgorithm.java)

### Definition

Sharding Algorithm definition

### Implementation classes

| *Configuration Type*     | *Auto Create Tables* | *Description*                                                           | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                          |
|--------------------------|----------------------|-------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MOD                      | Y                    | Modulo sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/ModShardingAlgorithm.java)                                                                     |
| HASH_MOD                 | Y                    | Hash modulo sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/HashModShardingAlgorithm.java)                                                             |
| BOUNDARY_RANGE           | Y                    | Boundary based range sharding algorithm                                 | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/BoundaryBasedRangeShardingAlgorithm.java)                                   |
| VOLUME_RANGE             | Y                    | Volume based range sharding algorithm                                   | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/VolumeBasedRangeShardingAlgorithm.java)                                       |
| AUTO_INTERVAL            | Y                    | Mutable interval sharding algorithm                                     | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/AutoIntervalShardingAlgorithm.java)                                         |
| INTERVAL                 | N                    | Fixed interval sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/IntervalShardingAlgorithm.java)                                                 |
| CLASS_BASED              | N                    | Class based sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/classbased/ClassBasedShardingAlgorithm.java)                                         |
| INLINE                   | N                    | Inline sharding algorithm                                               | [`org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/InlineShardingAlgorithm.java)                                                         |
| COMPLEX_INLINE           | N                    | Complex inline sharding algorithm                                       | [`org.apache.shardingsphere.sharding.algorithm.sharding.complex.ComplexInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/ComplexInlineShardingAlgorithm.java)                                          |
| HINT_INLINE              | N                    | Hint inline sharding algorithm                                          | [`org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/hint/HintInlineShardingAlgorithm.java)                                                     |

## ShardingAuditAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAuditAlgorithm.java)

### Definition

Sharding audit algorithm definition

### Implementation classes

| *Configuration Type*    | *Description*                                               | *Fully-qualified class name*                                                                                                                                                                                                                                                                      |
|-------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DML_SHARDING_CONDITIONS | Prohibit DML auditing algorithm without sharding conditions | [`org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/audit/DMLShardingConditionsShardingAuditAlgorithm.java) |

## DatetimeService

### Fully-qualified class name

[`org.apache.shardingsphere.timeservice.spi.TimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/api/src/main/java/org/apache/shardingsphere/timeservice/spi/TimestampService.java)

### Definition

Obtain the current date for routing definition

### Implementation classes

| *Configuration Type*     | *Description*                                                | *Fully-qualified class name*                                                                                                                                                                                                                                             |
|--------------------------|--------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DatabaseTimestampService | Get the current time from the database for routing           | [`org.apache.shardingsphere.timeservice.type.database.DatabaseTimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/type/database/src/main/java/org/apache/shardingsphere/timeservice/type/database/DatabaseTimestampService.java) |
| SystemTimestampService   | Get the current time from the application system for routing | [`org.apache.shardingsphere.timeservice.type.system.SystemTimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/type/system/src/main/java/org/apache/shardingsphere/timeservice/type/system/SystemTimestampService.java)           |

## InlineExpressionParser

### Fully-qualified class name

`org.apache.shardingsphere.infra.expr.core.InlineExpressionParser`

### Definition

Row Value Expressions definition

### Implementation classes

| *Configuration Type* | *Description*                                                                                        | *Fully-qualified class name*                                                   |
|----------------------|------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| GROOVY               | Row Value Expressions that uses the Groovy syntax                                                    | `org.apache.shardingsphere.infra.expr.groovy.GroovyInlineExpressionParser`     |
| LITERAL              | Row Value Expressions that uses a standard list                                                      | `org.apache.shardingsphere.infra.expr.literal.LiteralInlineExpressionParser`   |
| INTERVAL             | Row Value Expressions based on fixed interval that uses the Key-Value syntax                         | `org.apache.shardingsphere.infra.expr.interval.IntervalInlineExpressionParser` |                                                                             |
| ESPRESSO             | Row Value Expressions that uses the Groovy syntax based on GraalVM Truffle's Espresso implementation | `org.apache.shardingsphere.infra.expr.espresso.EspressoInlineExpressionParser` |
