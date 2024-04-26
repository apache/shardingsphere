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

## Row Value Expressions based on fixed interval that uses the Key-Value syntax

The `INTERVAL` implementation introduces a Key-Value style property syntax to define a set of time ranges of strings via a single line string. 
This is often used to simplify the definition of `actualDataNodes` for `Sharding` feature.

`INTERVAL` implements the method of defining multiple attributes as `Key1=Value1;Key2=Value2`, using `;` to separate key-value pairs, and `=` to separate `Key` values and `Value` values.

This implementation actively ignores the time zone information of `SP`, which means that when `DL` and `DU` contain time zone information, 
no time zone conversion will occur due to inconsistent time zones. 

This implementation is not sensitive to the order of key-value pairs, and the line expression does not carry the `;` sign at the end.

The `INTERVAL` implementation introduces the following Key values:

1. `P` stands for the abbreviation of prefix, which means the prefix of the result list unit, usually representing the prefix format of the real table.
2. `SP` stands for the abbreviation of suffix pattern, which means the timestamp format of the suffix of the result list unit. 
It usually represents the suffix format of the real table and must follow the format of Java DateTimeFormatter. 
For example: yyyyMMdd, yyyyMM or yyyy etc.
3. `DIA` stands for the abbreviation of datetime interval amount, which means the time interval of the result list unit.
4. `DIU` stands for the abbreviation of datetime interval unit, which means the shard key time interval unit. 
It must follow the enumeration value of Java `java.time.temporal.ChronoUnit#toString()`. For example: `Months`.
5. `DL` stands for the abbreviation of datetime lower, which means the lower bound of time. The format is consistent with the timestamp format defined by `SP`.
6. `DU` stands for the abbreviation of datetime upper, which means the upper bound value of time. The format is consistent with the timestamp format defined by `SP`.
7. `C` stands for the abbreviation of chronology, which means calendar system and must follow the format of Java `java.time.chrono.Chronology#getId()`.
For example: `Japanese`, `Minguo`, `ThaiBuddhist`. There is a default value of `ISO`.

Whether the Value corresponding to the Key of `C` is available depends on the system environment in which the JVM is located. 
This means that if the user needs to set `C=Japanese`, they may need to call `java.util.Locale.setDefault(java.util.Locale.JAPAN);` in the application's startup class to modify the system environment. 
Discuss two JVM environments.

1. Hotspot JVM determines the return value of `java.util.Locale.getDefault()` at RunTime.

2. GraalVM Native Image determines the return value of `java.util.Locale.Locale.getDefault()` at BuildTime, which is inconsistent with the performance of Hotspot JVM. 
Refer to https://github.com/oracle/graal/issues/8022 .

Type: INTERVAL

Example:

- `<INTERVAL>P=t_order_;SP=yyyy_MMdd;DIA=1;DIU=Days;DL=2023_1202;DU=2023_1204` will be converted to `t_order_2023_1202, t_order_2023_1203, t_order_2023_1204`
- `<INTERVAL>P=t_order_;SP=yyyy_MM;DIA=1;DIU=Months;DL=2023_10;DU=2023_12` will be converted to `t_order_2023_10, t_order_2023_11, t_order_2023_12`
- `<INTERVAL>P=t_order_;SP=yyyy;DIA=1;DIU=Years;DL=2021;DU=2023` will be converted to `t_order_2021, t_order_2022, t_order_2023`
- `<INTERVAL>P=t_order_;SP=HH_mm_ss_SSS;DIA=1;DIU=Millis;DL=22_48_52_131;DU=22_48_52_133` will be converted to `t_order_22_48_52_131, t_order_22_48_52_132, t_order_22_48_52_133`
- `<INTERVAL>P=t_order_;SP=yyyy_MM_dd_HH_mm_ss_SSS;DIA=1;DIU=Days;DL=2023_12_04_22_48_52_131;DU=2023_12_06_22_48_52_131` will be converted to `t_order_2023_12_04_22_48_52_131, t_order_2023_12_05_22_48_52_131, t_order_2023_12_06_22_48_52_131`
- `<INTERVAL>P=t_order_;SP=MM;DIA=1;DIU=Months;DL=10;DU=12` will be converted to `t_order_10, t_order_11, t_order_12`
- `<INTERVAL>P=t_order_;SP=GGGGyyyy_MM_dd;DIA=1;DIU=Days;DL=平成0001_12_05;DU=平成0001_12_06;C=Japanese` will be converted to `t_order_平成0001_12_05, t_order_平成0001_12_06`
- `<INTERVAL>P=t_order_;SP=GGGGyyy_MM_dd;DIA=1;DIU=Days;DL=平成001_12_05;DU=平成001_12_06;C=Japanese` will be converted to `t_order_平成001_12_05, t_order_平成001_12_06`
- `<INTERVAL>P=t_order_;SP=GGGGy_MM_dd;DIA=1;DIU=Days;DL=平成1_12_05;DU=平成1_12_06;C=Japanese` will be converted to `t_order_平成1_12_05, t_order_平成1_12_06`

## Row Value Expressions that uses the Groovy syntax based on GraalVM Truffle's Espresso implementation

This is an optional implementation. You need to actively declare the following dependencies in the `pom.xml` of your own project. 
And please make sure your own projects are compiled with OpenJDK 21+ or its downstream distribution.

Due to the limitations of https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/faq/#does-java-running-on-truffle-run-on-hotspot-too , 
this module is only ready on Linux when used in environments other than GraalVM Native Image.

Truffle's backward compatibility matrix with the JDK is located at https://medium.com/graalvm/40027a59c401 .

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-expr-espresso</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>java-community</artifactId>
        <version>24.0.0</version>
        <type>pom</type>
    </dependency>
</dependencies>
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
