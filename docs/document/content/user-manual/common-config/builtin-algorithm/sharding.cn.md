+++
title = "分片算法"
weight = 2
+++

## 背景信息

ShardingSphere 内置提供了多种分片算法，按照类型可以划分为自动分片算法、标准分片算法、复合分片算法和 Hint 分片算法，能够满足用户绝大多数业务场景的需要。此外，考虑到业务场景的复杂性，内置算法也提供了自定义分片算法的方式，用户可以通过编写 Java 代码来完成复杂的分片逻辑。
需要注意的是，自动分片算法的分片逻辑由 ShardingSphere 自动管理，需要通过配置 autoTables 分片规则进行使用。

## 参数解释

### 自动分片算法

#### 取模分片算法

类型：MOD

可配置属性：

| *属性名称*         | *数据类型* | *说明* |
|----------------|--------|------|
| sharding-count | int    | 分片数量 |


#### 哈希取模分片算法

类型：HASH_MOD

可配置属性：

| *属性名称*         | *数据类型* | *说明* |
|----------------|--------|------|
| sharding-count | int    | 分片数量 |

#### 基于分片容量的范围分片算法

类型：VOLUME_RANGE

可配置属性：

| *属性名称*          | *数据类型* | *说明*            |
|-----------------|--------|-----------------|
| range-lower     | long   | 范围下界，超过边界的数据会报错 |
| range-upper     | long   | 范围上界，超过边界的数据会报错 |
| sharding-volume | long   | 分片容量            |

#### 基于分片边界的范围分片算法

类型：BOUNDARY_RANGE

可配置属性：

| *属性名称*          | *数据类型* | *说明*                |
|-----------------|--------|---------------------|
| sharding-ranges | String | 分片的范围边界，多个范围边界以逗号分隔 |

#### 自动时间段分片算法

类型：AUTO_INTERVAL

可配置属性：

| *属性名称*           | *数据类型* | *说明*                                                  |
|------------------|--------|-------------------------------------------------------|
| datetime-lower   | String | 分片的起始时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss                   |
| datetime-upper   | String | 分片的结束时间范围，时间戳格式：yyyy-MM-dd HH:mm:ss                   |
| sharding-seconds | long   | 单一分片所能承载的最大时间，单位：秒，允许分片键的时间戳格式的秒带有时间精度，但秒后的时间精度会被自动抹去 |

### 标准分片算法

Apache ShardingSphere 内置的标准分片算法实现类包括：

#### 行表达式分片算法

使用 `InlineExpressionParser` SPI 的默认实现的 Groovy 的表达式，提供对 SQL 语句中的 `=` 和 `IN` 的分片操作支持，只支持单分片键。
对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的 Java 代码开发，如: `t_user_$->{u_id % 8}` 表示 `t_user` 表根据 `u_id` 模 8，而分成 8 张表，表名称为 `t_user_0` 到 `t_user_7`。
详情请参见[行表达式](/cn/dev-manual/sharding/#implementation-classes)。

类型：INLINE

可配置属性：

| *属性名称*                                     | *数据类型*  | *说明*                          | *默认值* |
|--------------------------------------------|---------|-------------------------------|-------|
| algorithm-expression                       | String  | 分片算法的行表达式                     |       |
| allow-range-query-with-inline-sharding (?) | boolean | 是否允许范围查询。注意：范围查询会无视分片策略，进行全路由 | false |

#### 时间范围分片算法

此算法主动忽视了 `datetime-pattern` 的时区信息。
这意味着当 `datetime-lower`, `datetime-upper` 和传入的分片键含有时区信息时，不会因为时区不一致而发生时区转换。

当传入的分片键为 `java.time.Instant` 或 `java.util.Date` 时存在特例处理，
其会携带上系统的时区信息后转化为 `datetime-pattern` 的字符串格式，再进行下一步分片。

类型：INTERVAL

可配置属性：

| *属性名称*                       | *数据类型* | *说明*                                                                                                                                          | *默认值* |
|------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------|-------|
| datetime-pattern             | String | 分片键的时间戳格式，必须遵循 Java DateTimeFormatter 的格式。例如：yyyy-MM-dd HH:mm:ss，yyyy-MM-dd 或 HH:mm:ss 等。但不支持与 `java.time.chrono.JapaneseDate` 相关的 GGGGy-MM 等 |       |
| datetime-lower               | String | 时间分片下界值，格式与 `datetime-pattern` 定义的时间戳格式一致                                                                                                     |       |
| datetime-upper (?)           | String | 时间分片上界值，格式与 `datetime-pattern` 定义的时间戳格式一致                                                                                                     | 当前时间  |
| sharding-suffix-pattern      | String | 分片数据源或真实表的后缀格式，必须遵循 Java DateTimeFormatter 的格式，必须和 `datetime-interval-unit` 保持一致。例如：yyyyMM                                                    |       |
| datetime-interval-amount (?) | int    | 分片键时间间隔，超过该时间间隔将进入下一分片                                                                                                                        | 1     |
| datetime-interval-unit (?)   | String | 分片键时间间隔单位，必须遵循 Java ChronoUnit 的枚举值。例如：MONTHS                                                                                                 | DAYS  |

### 复合分片算法

#### 复合行表达式分片算法

详情请参见[行表达式](/cn/features/sharding/concept/#行表达式)。

类型：COMPLEX_INLINE

| *属性名称*                                     | *数据类型*  | *说明*                          | *默认值* |
|--------------------------------------------|---------|-------------------------------|-------|
| sharding-columns (?)                       | String  | 分片列名称，多个列用逗号分隔。如不配置无法则不能校验    |       |
| algorithm-expression                       | String  | 分片算法的行表达式                     |       |
| allow-range-query-with-inline-sharding (?) | boolean | 是否允许范围查询。注意：范围查询会无视分片策略，进行全路由 | false |


### Hint 分片算法

#### Hint 行表达式分片算法

详情请参见[行表达式](/cn/features/sharding/concept/#行表达式)。

类型：HINT_INLINE

| *属性名称*                   | *数据类型* | *说明*      | *默认值*    |
|--------------------------|--------|-----------|----------|
| algorithm-expression (?) | String | 分片算法的行表达式 | ${value} |

### 自定义类分片算法

通过配置分片策略类型和算法类名，实现自定义扩展。
`CLASS_BASED` 允许向算法类内传入额外的自定义属性，传入的属性可以通过属性名为 `props` 的 `java.util.Properties` 类实例取出。 
参考 Git 的 `org.apache.shardingsphere.example.extension.sharding.algortihm.classbased.fixture.ClassBasedStandardShardingAlgorithmFixture` 。

类型：CLASS_BASED

可配置属性：

| *属性名称*             | *数据类型* | *说明*                                      |
|--------------------|--------|-------------------------------------------|
| strategy           | String | 分片策略类型，支持 STANDARD、COMPLEX 或 HINT（不区分大小写） |
| algorithmClassName | String | 分片算法全限定名                                  |

## 操作步骤

1. 使用数据分片时，在 shardingAlgorithms 属性下配置对应的数据分片算法即可；

## 配置示例

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

## 相关参考

- [核心特性：数据分片](/cn/features/sharding/)
- [开发者指南：数据分片](/cn/dev-manual/sharding/)
