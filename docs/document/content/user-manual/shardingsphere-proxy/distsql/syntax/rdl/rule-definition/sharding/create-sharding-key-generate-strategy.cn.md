+++
title = "CREATE SHARDING KEY GENERATE STRATEGY"
weight = 7
+++

## 描述

`CREATE SHARDING KEY GENERATE STRATEGY` 语法用于为当前所选逻辑库创建分片主键生成策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateShardingKeyGenerateStrategy ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' ifNotExists? keyGenerateStrategyName '(' keyGenerateStrategyDefinition ')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

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

- `columnKeyGenerateStrategyDefinition` 用于定义基于逻辑表和列的主键生成策略；
- `sequenceKeyGenerateStrategyDefinition` 用于定义基于序列名称的主键生成策略；
- `keyGenerateAlgorithmDefinition` 支持两种方式：
  - 通过 `TYPE(NAME=..., PROPERTIES(...))` 直接内联定义主键生成算法；
  - 通过 `GENERATOR=...` 引用已有的分片主键生成器；
- 当使用内联算法定义时，系统会自动生成并关联对应的分片主键生成器；
- `algorithmType` 为主键生成算法类型，详细信息请参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)；
- `ifNotExists` 子句用于避免出现 `Duplicate sharding key generate strategy` 错误。

### 示例

- 创建列维度的分片主键生成策略，并内联定义算法

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
TYPE(NAME="snowflake",PROPERTIES("worker-id"=1))
);
```

- 创建列维度的分片主键生成策略，并引用已有主键生成器

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

- 创建序列维度的分片主键生成策略

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_sequence_strategy (
SEQUENCE="order_seq",
TYPE(NAME="uuid")
);
```

- 使用 `ifNotExists` 子句创建分片主键生成策略

```sql
CREATE SHARDING KEY GENERATE STRATEGY IF NOT EXISTS order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

### 保留字

`CREATE`、`SHARDING`、`KEY`、`GENERATE`、`STRATEGY`、`TABLE`、`COLUMN`、`SEQUENCE`、`TYPE`、`NAME`、`PROPERTIES`、`GENERATOR`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW SHARDING KEY GENERATE STRATEGY](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generate-strategy/)
