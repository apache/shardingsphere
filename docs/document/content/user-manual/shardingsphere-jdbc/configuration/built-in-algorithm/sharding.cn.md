+++
title = "分片算法"
weight = 1
+++

## 自动分片算法

### 取模分片算法

类型：MOD

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| sharding-count | int       | 分片数量 |

### 哈希取模分片算法

类型：HASH_MOD

可配置属性：

| *属性名称*      | *数据类型* | *说明*  |
| -------------- | --------- | ------- |
| sharding-count | int       | 分片数量 |

### 基于分片容量的范围分片算法

类型：VOLUME_RANGE

可配置属性：

| *属性名称*       | *数据类型* | *说明*                      |
| --------------- | --------- | -------------------------- |
| range-lower     | long      | 范围下界，超过边界的数据会报错 |
| range-upper     | long      | 范围上界，超过边界的数据会报错 |
| sharding-volume | long      | 分片容量                    |

### 基于分片边界的范围分片算法

类型：BOUNDARY_RANGE

可配置属性：

| *属性名称*       | *数据类型* | *说明*                            |
| --------------- | --------- | --------------------------------- |
| sharding-ranges | String    | 分片的范围边界，多个范围边界以逗号分隔 |

### 自动时间段分片算法

类型：AUTO_INTERVAL

可配置属性：

| *属性名称*        | *数据类型* | *说明*                                          |
| ---------------- | --------- | ----------------------------------------------- |
| datetime-lower   | String    | 分片的起始时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss |
| datetime-upper   | String    | 分片的结束时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss |
| sharding-seconds | long      | 单一分片所能承载的最大时间，单位：秒                |

## 标准分片算法

Apache ShardingSphere 内置的标准分片算法实现类包括：

### 行表达式分片算法

使用 Groovy 的表达式，提供对 SQL 语句中的 `=` 和 `IN` 的分片操作支持，只支持单分片键。
对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的 Java 代码开发，如: `t_user_$->{u_id % 8}` 表示 `t_user` 表根据 `u_id` 模 8，而分成 8 张表，表名称为 `t_user_0` 到 `t_user_7`。
详情请参见[行表达式](/cn/features/sharding/concept/inline-expression/)。

类型：INLINE

可配置属性：

| *属性名称*                                 | *数据类型* | *说明*                                              | *默认值* |
| ----------------------------------------- | --------- | --------------------------------------------------- | ------- |
| algorithm-expression                      | String    | 分片算法的行表达式                                    | -       |
| allow-range-query-with-inline-sharding (?)| boolean   | 是否允许范围查询。注意：范围查询会无视分片策略，进行全路由 | false   |

### 时间范围分片算法

类型：INTERVAL

可配置属性：

| *属性名称*                    | *数据类型* | *说明*                                                                           | *默认值* |
| ---------------------------- | --------- | -------------------------------------------------------------------------------- | ------- |
| datetime-pattern             | String    | 分片键的时间戳格式，必须遵循 Java DateTimeFormatter 的格式。例如：yyyy-MM-dd HH:mm:ss | -       |
| datetime-lower               | String    | 时间分片下界值，格式与 `datetime-pattern` 定义的时间戳格式一致                        | -       |
| datetime-upper (?)           | String    | 时间分片上界值，格式与 `datetime-pattern` 定义的时间戳格式一致                        | 当前时间 |
| sharding-suffix-pattern      | String    | 分片数据源或真实表的后缀格式，必须遵循 Java DateTimeFormatter 的格式。例如：yyyyMM     | -       |
| datetime-interval-amount (?) | int       | 分片键时间间隔，超过该时间间隔将进入下一分片                                          | 1       |
| datetime-interval-unit (?)   | String    | 分片键时间间隔单位，必须遵循 Java ChronoUnit 的枚举值。例如：MONTHS                   | DAYS    |

## 复合分片算法

Apache ShardingSphere 暂无内置复合分片算法。

## Hint 分片算法

Apache ShardingSphere 暂无内置 Hint 分片算法。
