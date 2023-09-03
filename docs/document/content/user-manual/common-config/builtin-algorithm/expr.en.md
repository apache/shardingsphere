+++
title = "Row Value Expressions"
weight = 7
+++

## Row Value Expressions that uses the Groovy syntax

Type: GROOVY

Attributes:

None

## Row Value Expressions that uses a standard list

Type: PURELIST

Attributes:

None

## Procedure

When using attributes that require the use of `Row Value Expressions`, such as in the `data sharding` feature, it is 
sufficient to indicate the Type Name of the specific SPI implementation under the `actualDataNodes` attribute.

If the `Row Value Expressions` does not indicate the Type Name of the SPI, the SPI implementation of `GROOVY` will be 
used by default.

## Sample

```yaml
rules:
- !SHARDING
  tables:
    t_order: 
      actualDataNodes: <PURELIST>ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1
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

## Related References

- [Core Concept](/docs/document/content/features/sharding/concept.en.md)
- [Data Sharding](/docs/document/content/dev-manual/sharding.en.md)
