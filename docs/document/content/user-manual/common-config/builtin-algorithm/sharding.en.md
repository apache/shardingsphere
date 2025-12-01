+++
title = "Sharding Algorithm"
weight = 2
+++

## Background

ShardingSphere built-in algorithms provide a variety of sharding algorithms, which can be divided into automatic sharding algorithms, standard sharding algorithms, composite sharding algorithms, and hint sharding algorithms, and can meet the needs of most business scenarios of users.

Additionally, considering the complexity of business scenarios, the built-in algorithm also provides a way to customize the sharding algorithm. Users can complete complex sharding logic by writing java code.

It should be noted that the sharding logic of the automatic sharding algorithm is automatically managed by ShardingSphere and needs to be used by configuring the autoTables sharding rules.

## Parameters

### Auto Sharding Algorithm

#### Modulo Sharding Algorithm

Type: MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
|----------------|------------|----------------|
| sharding-count | int        | Sharding count |

#### Hash Modulo Sharding Algorithm

Type: HASH_MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
|----------------|------------|----------------|
| sharding-count | int        | Sharding count |

#### Volume Based Range Sharding Algorithm

Type: VOLUME_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                          |
|-----------------|------------|--------------------------------------------------------|
| range-lower     | long       | Range lower bound, throw exception if lower than bound |
| range-upper     | long       | Range upper bound, throw exception if upper than bound |
| sharding-volume | long       | Sharding volume                                        |

#### Boundary Based Range Sharding Algorithm

Type: BOUNDARY_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                                     |
|-----------------|------------|-------------------------------------------------------------------|
| sharding-ranges | String     | Range of sharding border, multiple boundaries separated by commas |

#### Auto Interval Sharding Algorithm

Type: AUTO_INTERVAL

Attributes:

| *Name*           | *DataType* | *Description*                                                                                                                                                     |
|------------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| datetime-lower   | String     | Shard datetime begin boundary, pattern: yyyy-MM-dd HH:mm:ss                                                                                                       |
| datetime-upper   | String     | Shard datetime end boundary, pattern: yyyy-MM-dd HH:mm:ss                                                                                                         |
| sharding-seconds | long       | Max seconds for the data in one shard, allows sharding key timestamp format seconds with time precision, but time precision after seconds is automatically erased |

### Standard Sharding Algorithm

Apache ShardingSphere built-in standard sharding algorithm are:

#### Inline Sharding Algorithm

With Groovy expressions that uses the default implementation of the `InlineExpressionParser` SPI, 
`InlineShardingStrategy` provides single-key support for the sharding operation of `=` and `IN` in SQL.
Simple sharding algorithms can be used through a simple configuration to avoid laborious Java code developments.
For example, `t_user_$->{u_id % 8}` means table t_user is divided into 8 tables according to u_id, with table names from `t_user_0` to `t_user_7`.
Please refer to [Inline Expression](/en/dev-manual/sharding/#implementation-classes) for more details.

Type: INLINE

Attributes:

| *Name*                                     | *DataType* | *Description*                                                                                            | *Default Value* |
|--------------------------------------------|------------|----------------------------------------------------------------------------------------------------------|-----------------|
| algorithm-expression                       | String     | Inline expression sharding algorithm                                                                     | -               |
| allow-range-query-with-inline-sharding (?) | boolean    | Whether range query is allowed. Note: range query will ignore sharding strategy and conduct full routing | false           |

#### Interval Sharding Algorithm

This algorithm actively ignores the time zone information of `datetime-pattern`. 
This means that when `datetime-lower`, `datetime-upper` and the incoming shard key contain time zone information, time zone conversion will not occur due to time zone inconsistencies.

When the shard key passed in is `java.time.Instant` or `java.util.Date`, there is a special case.
It will carry the system's time zone information and convert it into a string format of `datetime-pattern` before the next sharding.

Type: INTERVAL

Attributes:

| *Name*                       | *DataType* | *Description*                                                                                                                                                                                                                    | *Default Value* |
|------------------------------|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| datetime-pattern             | String     | Timestamp pattern of sharding value, must can be transformed to Java LocalDateTime. For example: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd or HH:mm:ss etc. But GGGGy-MM etc. related to `java.time.chrono.JapaneseDate` are not supported | -               |
| datetime-lower               | String     | Datetime sharding lower boundary, pattern is defined `datetime-pattern`                                                                                                                                                          | -               |
| datetime-upper (?)           | String     | Datetime sharding upper boundary, pattern is defined `datetime-pattern`                                                                                                                                                          | Now             |
| sharding-suffix-pattern      | String     | Suffix pattern of sharding data sources or tables, must can be transformed to Java LocalDateTime, must be consistent with `datetime-interval-unit`. For example: yyyyMM                                                          | -               |
| datetime-interval-amount (?) | int        | Interval of sharding value, after which the next shard will be entered                                                                                                                                                           | 1               |
| datetime-interval-unit (?)   | String     | Unit of sharding value interval, must can be transformed to Java ChronoUnit's Enum value. For example: MONTHS                                                                                                                    | DAYS            |

### Complex Sharding Algorithm

#### Complex Inline Sharding Algorithm

Please refer to [Inline Expression](/en/features/sharding/concept/#row-value-expressions) for more details.

Type: COMPLEX_INLINE

| *Name*                                     | *DataType* | *Description*                                                                                            | *Default Value* |
|--------------------------------------------|------------|----------------------------------------------------------------------------------------------------------|-----------------|
| sharding-columns (?)                       | String     | sharding column names                                                                                    | -               |
| algorithm-expression                       | String     | Inline expression sharding algorithm                                                                     | -               |
| allow-range-query-with-inline-sharding (?) | boolean    | Whether range query is allowed. Note: range query will ignore sharding strategy and conduct full routing | false           |

### Hint Sharding Algorithm

#### Hint Inline Sharding Algorithm

Please refer to [Inline Expression](/en/features/sharding/concept/#row-value-expressions) for more details.

Type: COMPLEX_INLINE

| *Name*               | *DataType* | *Description*                        | *Default Value* |
|----------------------|------------|--------------------------------------|-----------------|
| algorithm-expression | String     | Inline expression sharding algorithm | ${value}        |


### Class Based Sharding Algorithm

Realize custom extension by configuring the sharding strategy type and algorithm class name.
`CLASS_BASED` allows additional custom properties to be passed into the algorithm class. The passed properties can be retrieved through the `java.util.Properties` class instance with the property name `props`. 
Refer to Git's `org.apache.shardingsphere.example.extension.sharding.algortihm.classbased.fixture.ClassBasedStandardShardingAlgorithmFixture`.

Type：CLASS_BASED

Attributes：

| *Name*             | *DataType* | *Description*                                                                |
|--------------------|------------|------------------------------------------------------------------------------|
| strategy           | String     | Sharding strategy type, support STANDARD, COMPLEX or HINT (case insensitive) |
| algorithmClassName | String     | Fully qualified name of sharding algorithm                                   |

## Procedure

1. When using data sharding, configure the corresponding data sharding algorithm under the shardingAlgorithms attribute.

## Sample

```yaml
rules:
- !SHARDING
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_item_inline
      keyGenerateStrategy:
        column: order_item_id
        keyGeneratorName: snowflake
    t_account:
      actualDataNodes: ds_${0..1}.t_account_${0..1}
      tableStrategy:
        standard:
          shardingAlgorithmName: t_account_inline
      keyGenerateStrategy:
        column: account_id
        keyGeneratorName: snowflake
  defaultShardingColumn: account_id
  bindingTables:
    - t_order,t_order_item
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  defaultTableStrategy:
    none:
  
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
    t_order_item_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_item_${order_id % 2}
    t_account_inline:
      type: INLINE
      props:
        algorithm-expression: t_account_${account_id % 2}
  keyGenerators:
    snowflake:
      type: SNOWFLAKE

- !BROADCAST
  tables:
    - t_address
```

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
- [Developer Guide: Data Sharding](/en/dev-manual/sharding/)
