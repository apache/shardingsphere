+++
title = "ALTER SHARDING KEY GENERATE STRATEGY"
weight = 8
+++

## 描述

`ALTER SHARDING KEY GENERATE STRATEGY` 语法用于修改当前所选逻辑库的分片主键生成策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterShardingKeyGenerateStrategy ::=
  'ALTER' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' keyGenerateStrategyName '(' keyGenerateStrategyDefinition ')'

keyGenerateStrategyDefinition ::=
  columnKeyGenerateStrategyDefinition
  | sequenceKeyGenerateStrategyDefinition

columnKeyGenerateStrategyDefinition ::=
  'TABLE' '=' tableName ',' 'COLUMN' '=' columnName ',' keyGenerateAlgorithmDefinition

sequenceKeyGenerateStrategyDefinition ::=
  'SEQUENCE' '=' sequenceName ',' keyGenerateAlgorithmDefinition

keyGenerateAlgorithmDefinition ::=
  algorithmDefinition
  | 'GENERATOR' '=' keyGeneratorName

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

keyGenerateStrategyName ::=
  identifier

tableName ::=
  identifier

columnName ::=
  identifier

sequenceName ::=
  identifier | string

keyGeneratorName ::=
  identifier

algorithmType ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 语法结构与 `CREATE SHARDING KEY GENERATE STRATEGY` 保持一致；
- `ALTER` 会使用新的定义替换已有分片主键生成策略；
- `GENERATOR=...` 用于改为引用已有的分片主键生成器；
- `TYPE(NAME=..., PROPERTIES(...))` 用于直接改为新的内联算法定义；
- `algorithmType` 为主键生成算法类型，详细信息请参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)。

### 示例

- 修改列维度的分片主键生成策略，并引用已有主键生成器

```sql
ALTER SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

- 修改为序列维度的分片主键生成策略，并使用内联算法

```sql
ALTER SHARDING KEY GENERATE STRATEGY order_sequence_strategy (
SEQUENCE="order_seq",
TYPE(NAME="uuid")
);
```

### 保留字

`ALTER`、`SHARDING`、`KEY`、`GENERATE`、`STRATEGY`、`TABLE`、`COLUMN`、`SEQUENCE`、`TYPE`、`NAME`、`PROPERTIES`、`GENERATOR`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATE STRATEGY](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generate-strategy/)
