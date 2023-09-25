+++
title = "行表达式"
weight = 7
+++

## 使用 Groovy 语法的行表达式

类型：GROOVY

可配置属性：

无

## 使用标准列表的行表达式

类型：LITERAL

可配置属性：

无

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
        algorithm-expression: <GROOVY>t_order_${order_id % 2}
  keyGenerators:
    snowflake:
      type: SNOWFLAKE
```

## 相关参考

- [核心概念](/docs/document/content/features/sharding/concept.cn.md)
- [数据分片](/docs/document/content/dev-manual/sharding.cn.md)
