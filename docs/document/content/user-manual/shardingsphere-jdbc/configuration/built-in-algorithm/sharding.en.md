+++
title = "Sharding Algorithm"
weight = 1
+++

## Auto Sharding Algorithm

### Modulo Sharding Algorithm

Type: MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
| -------------- | ---------- | -------------- |
| sharding-count | int        | Sharding count |

### Hash Modulo Sharding Algorithm

Type: HASH_MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
| -------------- | ---------- | -------------- |
| sharding-count | int        | Sharding count |

### Volume Based Range Sharding Algorithm

Type: VOLUME_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                          |
| --------------- | ---------- | ------------------------------------------------------ |
| range-lower     | long       | Range lower bound, throw exception if lower than bound |
| range-upper     | long       | Range upper bound, throw exception if upper than bound |
| sharding-volume | long       | Sharding volume                                        |

### Boundary Based Range Sharding Algorithm

Type: BOUNDARY_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                                     |
| --------------- | ---------- | ----------------------------------------------------------------- |
| sharding-ranges | String     | Range of sharding border, multiple boundaries separated by commas |

### Auto Interval Sharding Algorithm

Type: AUTO_INTERVAL

Attributes:

| *Name*           | *DataType* | *Description*                                               |
| ---------------- | ---------- | ----------------------------------------------------------- |
| datetime-lower   | String     | Shard datetime begin boundary, pattern: yyyy-MM-dd HH:mm:ss |
| datetime-upper   | String     | Shard datetime end boundary, pattern: yyyy-MM-dd HH:mm:ss   |
| sharding-seconds | long       | Max seconds for the data in one shard                       |

## Standard Sharding Algorithm

Apache ShardingSphere built-in standard sharding algorithm are:

### Inline Sharding Algorithm

With Groovy expressions, `InlineShardingStrategy` provides single-key support for the sharding operation of `=` and `IN` in SQL. 
Simple sharding algorithms can be used through a simple configuration to avoid laborious Java code developments. 
For example, `t_user_$->{u_id % 8}` means table t_user is divided into 8 tables according to u_id, with table names from `t_user_0` to `t_user_7`.
Please refer to [Inline Expression](/en/features/sharding/concept/inline-expression/) for more details.

Type: INLINE

Attributes:

| *Name*                                    | *DataType* | *Description*                                                                                            | *Default Value* |
| ----------------------------------------- | ---------- | -------------------------------------------------------------------------------------------------------- | --------------- |
| algorithm-expression                      | String     | Inline expression sharding algorithm                                                                     | -               |
| allow-range-query-with-inline-sharding (?)| boolean    | Whether range query is allowed. Note: range query will ignore sharding strategy and conduct full routing | false           |

### Interval Sharding Algorithm

Type: INTERVAL

Attributes:

| *Name*                       | *DataType* | *Description*                                                                                                         | *Default Value* |
| ---------------------------- | ---------- | --------------------------------------------------------------------------------------------------------------------- | --------------- |
| datetime-pattern             | String     | Timestamp pattern of sharding value, must can be transformed to Java LocalDateTime. For example: yyyy-MM-dd HH:mm:ss  | -               |
| datetime-lower               | String     | Datetime sharding lower boundary, pattern is defined `datetime-pattern`                                               | -               |
| datetime-upper (?)           | String     | Datetime sharding upper boundary, pattern is defined `datetime-pattern`                                               | Now             |
| sharding-suffix-pattern      | String     | Suffix pattern of sharding data sources or tables, must can be transformed to Java LocalDateTime. For example: yyyyMM | -               |
| datetime-interval-amount (?) | int        | Interval of sharding value                                                                                            | 1               |
| datetime-interval-unit (?)   | String     | Unit of sharding value interval, must can be transformed to Java ChronoUnit's Enum value. For example: MONTHS         | DAYS            |

## Complex Sharding Algorithm

There is no built-in complex sharding algorithm in Apache ShardingSphere.

## Hint Sharding Algorithm

There is no built-in hint sharding algorithm in Apache ShardingSphere.
