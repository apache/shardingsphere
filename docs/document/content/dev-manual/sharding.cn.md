+++
pre = "<b>5.3. </b>"
title = "数据分片"
weight = 3
chapter = true
+++

## ShardingAlgorithm

### 全限定类名

[`org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAlgorithm.java)

### 定义

分片算法

### 已知实现

| *配置标识*                   | *自动分片算法* | *详细说明*                    | *全限定类名*                                                                                                                                                                                                                                                                                                                               |
|--------------------------|----------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MOD                      | Y        | 基于取模的分片算法                 | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/ModShardingAlgorithm.java)                                                                     |
| HASH_MOD                 | Y        | 基于哈希取模的分片算法               | [`org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/mod/HashModShardingAlgorithm.java)                                                             |
| BOUNDARY_RANGE           | Y        | 基于分片边界的范围分片算法             | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/BoundaryBasedRangeShardingAlgorithm.java)                                   |
| VOLUME_RANGE             | Y        | 基于分片容量的范围分片算法             | [`org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/range/VolumeBasedRangeShardingAlgorithm.java)                                       |
| AUTO_INTERVAL            | Y        | 基于可变时间范围的分片算法             | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/AutoIntervalShardingAlgorithm.java)                                         |
| INTERVAL                 | N        | 基于固定时间范围的分片算法             | [`org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/datetime/IntervalShardingAlgorithm.java)                                                 |
| CLASS_BASED              | N        | 基于自定义类的分片算法               | [`org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/classbased/ClassBasedShardingAlgorithm.java)                                         |
| INLINE                   | N        | 基于行表达式的分片算法               | [`org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/InlineShardingAlgorithm.java)                                                         |
| COMPLEX_INLINE           | N        | 基于行表达式的复合分片算法             | [`org.apache.shardingsphere.sharding.algorithm.sharding.complex.ComplexInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/inline/ComplexInlineShardingAlgorithm.java)                                          |
| HINT_INLINE              | N        | 基于行表达式的 Hint 分片算法         | [`org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/sharding/hint/HintInlineShardingAlgorithm.java)                                                     |

## ShardingAuditAlgorithm

### 全限定类名

[`org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/api/src/main/java/org/apache/shardingsphere/sharding/spi/ShardingAuditAlgorithm.java)

### 定义

分片审计算法

### 已知实现

| *配置标识*                  | *详细说明*          | *全限定类名*                                                                                                                                                                                                                                                                                           |
|-------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DML_SHARDING_CONDITIONS | 禁止不带分片键的DML审计算法 | [`org.apache.shardingsphere.sharding.algorithm.audit.DMLShardingConditionsShardingAuditAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/algorithm/audit/DMLShardingConditionsShardingAuditAlgorithm.java) |

## DatetimeService

### 全限定类名

[`org.apache.shardingsphere.timeservice.spi.TimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/api/src/main/java/org/apache/shardingsphere/timeservice/spi/TimestampService.java)

### 定义

获取当前时间进行路由

### 已知实现

| *配置标识*                   | *详细说明*             | *全限定类名*                                                                                                                                                                                                                                                                  |
|--------------------------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DatabaseTimestampService | 从数据库中获取当前时间进行路由    | [`org.apache.shardingsphere.timeservice.type.database.DatabaseTimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/type/database/src/main/java/org/apache/shardingsphere/timeservice/type/database/DatabaseTimestampService.java) |
| SystemTimestampService   | 从应用系统时间中获取当前时间进行路由 | [`org.apache.shardingsphere.timeservice.type.system.SystemTimestampService`](https://github.com/apache/shardingsphere/blob/master/kernel/time-service/type/system/src/main/java/org/apache/shardingsphere/timeservice/type/system/SystemTimestampService.java)           |

## InlineExpressionParser

### 全限定类名

`org.apache.shardingsphere.infra.expr.core.InlineExpressionParser`

### 定义

解析行表达式

### 已知实现

| *配置标识*   | *详细说明*                                             | *全限定类名*                                                                        |
|----------|----------------------------------------------------|--------------------------------------------------------------------------------|
| GROOVY   | 使用 Groovy 语法的行表达式                                  | `org.apache.shardingsphere.infra.expr.groovy.GroovyInlineExpressionParser`     |
| LITERAL  | 使用标准列表的行表达式                                        | `org.apache.shardingsphere.infra.expr.literal.LiteralInlineExpressionParser`   |
| INTERVAL | 基于固定时间范围的 Key-Value 语法的行表达式                        | `org.apache.shardingsphere.infra.expr.interval.IntervalInlineExpressionParser` |
| ESPRESSO | 基于 GraalVM Truffle 的 Espresso 实现的使用 Groovy 语法的行表达式 | `org.apache.shardingsphere.infra.expr.espresso.EspressoInlineExpressionParser` |
