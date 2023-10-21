+++
title = "行表达式"
weight = 10
+++

## 使用 Groovy 语法的行表达式

在配置中使用 `${ expression }` 或 `$->{ expression }` 标识 Groovy 表达式即可。 Groovy 表达式使用的是 Groovy 的语法，Groovy 能够支持的所有操作，行表达式均能够支持。
`${begin..end}` 表示范围区间； `${[unit1, unit2, unit_x]}` 表示枚举值。
行表达式中如果出现连续多个 `${ expression }` 或 `$->{ expression }` 表达式，整个表达式最终的结果将会根据每个子表达式的结果进行笛卡尔组合。

类型：GROOVY

用例：

- `<GROOVY>t_order_${1..3}` 将被转化为 `t_order_1, t_order_2, t_order_3`
- `<GROOVY>${['online', 'offline']}_table${1..3}` 将被转化为 `online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3`

## 使用标准列表的行表达式

`LITERAL` 实现将不对表达式部分做任何符号的转化，从标准列表的输入直接获得标准列表的输出。此有助于解决 GraalVM Native Image 下不便于使用 Groovy 表达式的问题。

类型：LITERAL

用例：

- `<LITERAL>t_order_1, t_order_2, t_order_3` 将被转化为 `t_order_1, t_order_2, t_order_3`
- `<LITERAL>t_order_${1..3}` 将被转化为 `t_order_${1..3}`

## 基于 GraalVM Truffle 的 Espresso 实现的使用 Groovy 语法的行表达式

此为可选实现，你需要在自有项目的 `pom.xml` 主动声明如下依赖。并且请确保自有项目通过 GraalVM CE 23.0.1 For JDK17 编译。

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-expr-espresso</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
</dependencies>
```

用户必须通过 GraalVM Updater 安装 Espresso 组件，即在 bash 执行如下命令

```bash
gu install espresso
```

`ESPRESSO` 仍为实验性模块，其允许在 GraalVM Native Image 下通过 GraalVM Truffle 的 Espresso 实现来使用带 Groovy 语法的行表达式。

语法部分与 `GROOVY` 实现规则相同。

类型：ESPRESSO

用例：

- `<ESPRESSO>t_order_${1..3}` 将被转化为 `t_order_1, t_order_2, t_order_3`
- `<ESPRESSO>${['online', 'offline']}_table${1..3}` 将被转化为 `online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3`

## 操作步骤

使用需要使用 `行表达式` 的属性时， 如在 `数据分片` 功能中， 在 `actualDataNodes` 属性下指明特定的 SPI 实现的 Type Name 即可。

若 `行表达式` 不指明 SPI 的 Type Name，默认将使用 `GROOVY` 的 SPI 实现。

## 配置示例

```yaml
rules:
- !SHARDING
  tables:
    t_order: 
      actualDataNodes: <LITERAL>ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1
      tableStrategy: 
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: <GROOVY>ds_${user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
  keyGenerators:
    snowflake:
      type: SNOWFLAKE
```

## 相关参考

- [核心概念](/cn/features/sharding/concept)
- [数据分片](/cn/dev-manual/sharding)
