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

## 基于固定时间范围的 Key-Value 语法的行表达式

`INTERVAL` 实现引入了 Key-Value 风格的属性语法，来通过单行字符串定义一组时间范围内的字符串。这通常用于简化对 `数据分片` 功能的 `actualDataNodes` 的定义。
`INTERVAL` 生成的字符串仅适用于单个真实库中的多张表。
若需要处理多个真实库中的多张表，考虑自行创建 `org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser` 的 SPI 实现类。

`INTERVAL` 实现定义多个属性的方式为 `Key1=Value1;Key2=Value2`，通过 `;` 号分割键值对，通过 `=` 号分割 `Key` 值和 `Value` 值。

此实现主动忽视了 `SP` 的时区信息，这意味着当 `DL` 和 `DU` 含有时区信息时，不会因为时区不一致而发生时区转换。

此实现键值对的顺序不敏感， 行表达式尾部不携带 `;` 号。

`INTERVAL` 实现引入了如下 Key 的键值：

1. `P` 代表 prefix 的缩写，意为结果列表单元的前缀, 通常代表真实表的前缀格式。
2. `SP` 代表 suffix pattern 的缩写，意为结果列表单元的后缀的时间戳格式, 通常代表真实表的后缀格式，必须遵循 Java DateTimeFormatter 的格式。
例如：yyyyMMdd，yyyyMM 或 yyyy 等。
3. `DIA` 代表 datetime interval amount 的缩写，意为结果列表单元的时间间隔。
4. `DIU` 代表 datetime interval unit 的缩写，意为分片键时间间隔单位，必须遵循 Java `java.time.temporal.ChronoUnit#toString()` 的枚举值。例如：`Months`。
5. `DL` 代表 datetime lower 的缩写，意为时间下界值，格式与 `SP` 定义的时间戳格式一致。
6. `DU` 代表 datetime upper 的缩写，意为时间上界值，格式与 `SP` 定义的时间戳格式一致。
7. `C` 代表 chronology 的缩写，意为日历系统，必须遵循 Java `java.time.chrono.Chronology#getId()` 的格式。
例如：`Japanese`，`Minguo`，`ThaiBuddhist`。存在默认值为 `ISO`。

对于 `C` 的 Key 对应的 Value 是否可用，这取决于 JVM 所处的系统环境。
这意味着如果用户需要设置 `C=Japanese`，则可能需要在应用的启动类调用 `java.util.Locale.setDefault(java.util.Locale.JAPAN);` 以修改系统环境。
讨论两种 JVM 环境。

1. Hotspot JVM 在 RunTime 决定 `java.util.Locale.getDefault()` 的返回值。

2. GraalVM Native Image 在 BuildTime 决定 `java.util.Locale.Locale.getDefault()` 的返回值，与 Hotspot JVM 的表现并不一致，参考 https://github.com/oracle/graal/issues/8022 。

类型：INTERVAL

用例：

- `<INTERVAL>P=t_order_;SP=yyyy_MMdd;DIA=1;DIU=Days;DL=2023_1202;DU=2023_1204` 将被转化为 `t_order_2023_1202, t_order_2023_1203, t_order_2023_1204`
- `<INTERVAL>P=t_order_;SP=yyyy_MM;DIA=1;DIU=Months;DL=2023_10;DU=2023_12` 将被转化为 `t_order_2023_10, t_order_2023_11, t_order_2023_12`
- `<INTERVAL>P=t_order_;SP=yyyy;DIA=1;DIU=Years;DL=2021;DU=2023` 将被转化为 `t_order_2021, t_order_2022, t_order_2023`
- `<INTERVAL>P=t_order_;SP=HH_mm_ss_SSS;DIA=1;DIU=Millis;DL=22_48_52_131;DU=22_48_52_133` 将被转化为 `t_order_22_48_52_131, t_order_22_48_52_132, t_order_22_48_52_133`
- `<INTERVAL>P=t_order_;SP=yyyy_MM_dd_HH_mm_ss_SSS;DIA=1;DIU=Days;DL=2023_12_04_22_48_52_131;DU=2023_12_06_22_48_52_131` 将被转化为 `t_order_2023_12_04_22_48_52_131, t_order_2023_12_05_22_48_52_131, t_order_2023_12_06_22_48_52_131`
- `<INTERVAL>P=t_order_;SP=MM;DIA=1;DIU=Months;DL=10;DU=12` 将被转化为 `t_order_10, t_order_11, t_order_12`
- `<INTERVAL>P=t_order_;SP=GGGGyyyy_MM_dd;DIA=1;DIU=Days;DL=平成0001_12_05;DU=平成0001_12_06;C=Japanese` 将被转化为 `t_order_平成0001_12_05, t_order_平成0001_12_06`
- `<INTERVAL>P=t_order_;SP=GGGGyyy_MM_dd;DIA=1;DIU=Days;DL=平成001_12_05;DU=平成001_12_06;C=Japanese` 将被转化为 `t_order_平成001_12_05, t_order_平成001_12_06`
- `<INTERVAL>P=t_order_;SP=GGGGy_MM_dd;DIA=1;DIU=Days;DL=平成1_12_05;DU=平成1_12_06;C=Japanese` 将被转化为 `t_order_平成1_12_05, t_order_平成1_12_06`

## 基于 GraalVM Truffle 的 Espresso 实现的使用 Groovy 语法的行表达式

此为可选实现，你需要在自有项目的 `pom.xml` 主动声明如下依赖。并且请确保自有项目通过 OpenJDK 21+ 或其下游发行版编译。

由于 https://www.graalvm.org/jdk21/reference-manual/espresso/faq/#does-java-running-on-truffle-run-on-hotspot-too 的限制，
当此模块在非 GraalVM Native Image 的环境中被使用时，仅在 System Property `os.arch` 为 `amd64` 的 Linux 上就绪。

Truffle 与 JDK 的向后兼容性矩阵位于 https://medium.com/graalvm/40027a59c401 。

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
        <version>24.1.2</version>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>java</artifactId>
        <version>24.1.2</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

`ESPRESSO` 仍为实验性模块，其允许在 GraalVM Native Image 下通过 GraalVM Truffle 的 Espresso 实现来使用带 Groovy 语法的行表达式。

语法部分与 `GROOVY` 实现规则相同。

类型：ESPRESSO

用例：

- `<ESPRESSO>t_order_${1..3}` 将被转化为 `t_order_1, t_order_2, t_order_3`
- `<ESPRESSO>${['online', 'offline']}_table${1..3}` 将被转化为 `online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3`

## 自定义实现

用户总是可以自行创建 `org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser` 的实现类，
以覆盖更复杂的场景，包括连接至远程 `ElasticSearch` 集群执行 `ES|QL` 以获得 `java.util.List<String>`。

考虑一个简单的 SPI 实现类，

```java
package org.example;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
public final class CustomInlineExpressionParserFixture implements InlineExpressionParser {
    private String inlineExpression;
    @Override
    public void init(final Properties props) {
        inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
    }
    @Override
    public List<String> splitAndEvaluate() {
        if ("spring".equals(inlineExpression)) {
            return Arrays.asList("t_order_2024_01", "t_order_2024_02");
        }
        return Arrays.asList("t_order_2024_03", "t_order_2024_04");
    }
    @Override
    public Object getType() {
        return "CUSTOM.FIXTURE";
    }
}
```

并在项目的 classpath 添加 `META-INF/services/org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser`文件，

```
org.example.CustomInlineExpressionParserFixture
```

此时对于 ShardingSphere 配置文件中的 `actualDataNodes`，
1. 若配置为 `<CUSTOM.FIXTURE>spring`，将被转化为 `t_order_2024_01, t_order_2024_02`。
2. 若配置为 `<CUSTOM.FIXTURE>summer`，将被转化为 `t_order_2024_03, t_order_2024_04`。

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
