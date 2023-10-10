+++
title = "Row Value Expressions"
weight = 10
+++

## Row Value Expressions that uses the Groovy syntax

Type: GROOVY

Just use `${ expression }` or `$->{ expression }` in the configuration to identify the row expressions.
The content of row expressions uses Groovy syntax, and all operations supported by Groovy are supported by row expressions.
`${begin..end}` denotes the range interval, `${[unit1, unit2, unit_x]}` denotes the enumeration value.
If there are multiple `${ expression }` or `$->{ expression }` expressions in a row expression, the final result of the 
whole expression will be a Cartesian combination based on the result of each sub-expression.

Example:

- `<GROOVY>t_order_${1..3}` will be converted to `t_order_1, t_order_2, t_order_3`
- `<GROOVY>${['online', 'offline']}_table${1..3}` will be converted to `online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3`

## Row Value Expressions that uses a standard list

The `LITERAL` implementation will not convert any symbols to the expression part, and will directly obtain the output of
the standard list from the input of the standard list. 
This helps address the issue that Groovy expressions are inconvenient to use under GraalVM Native Image.

Type: LITERAL

Example:

- `<LITERAL>t_order_1, t_order_2, t_order_3` will be converted to `t_order_1, t_order_2, t_order_3`
- `<LITERAL>t_order_${1..3}` will be converted to `t_order_${1..3}`

## Row Value Expressions that uses the Groovy syntax based on GraalVM Truffle's Espresso implementation

This is an optional implementation, and you need to actively declare the following dependencies in the `pom.xml` of your own project.
And make sure your own project is compiled with GraalVM CE 23.0.1 For JDK17.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-expr-espresso</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
</dependencies>
```

The user must install the Espresso component via GraalVM Updater, i.e. execute the following command in bash

```bash
gu install espresso
```

`ESPRESSO` is still an experimental module that allows the use of Row Value Expressions with Groovy syntax under GraalVM
Native Image through the Espresso implementation of GraalVM Truffle.

The syntax part is the same as the `GROOVY` implementation rules.

Type: ESPRESSO

Example:

- `<ESPRESSO>t_order_${1..3}` will be converted to `t_order_1, t_order_2, t_order_3`
- `<ESPRESSO>${['online', 'offline']}_table${1..3}` will be converted to `online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3`

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

## Related References

- [Core Concept](/en/features/sharding/concept)
- [Data Sharding](/en/dev-manual/sharding)
