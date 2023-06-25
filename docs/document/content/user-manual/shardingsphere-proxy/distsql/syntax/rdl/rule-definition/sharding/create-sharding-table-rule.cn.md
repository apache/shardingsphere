+++
title = "CREATE SHARDING TABLE RULE"
weight = 1
+++

## 描述

`CREATE SHARDING TABLE RULE` 语法用于为当前所选逻辑库添加分片规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateShardingTableRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'RULE' ifNotExists? (tableDefinition | autoTableDefinition) (',' (tableDefinition | autoTableDefinition))*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

tableDefinition ::= 
  tableName '(' 'DATANODES' '(' dataNode (',' dataNode)* ')' (','  'DATABASE_STRATEGY' '(' strategyDefinition ')')? (','  'TABLE_STRATEGY' '(' strategyDefinition ')')? (','  'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')')? (',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')')? ')'

autoTableDefinition ::=
  tableName '(' 'STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)*  ')' ',' 'SHARDING_COLUMN' '=' columnName ',' algorithmDefinition (',' 'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')')? (',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')')? ')'

strategyDefinition ::=
  'TYPE' '=' strategyType ',' ('SHARDING_COLUMN' | 'SHARDING_COLUMNS') '=' columnName ',' algorithmDefinition

keyGenerateStrategyDefinition ::= 
  'KEY_GENERATE_STRATEGY' '(' 'COLUMN' '=' columnName ',' algorithmDefinition ')' 

auditStrategyDefinition ::= 
  'AUDIT_STRATEGY' '(' algorithmDefinition (',' algorithmDefinition)* ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)?')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

tableName ::=
  identifier

dataNode ::=
  string

storageUnitName ::=
  identifier

columnName ::=
  identifier

algorithmType ::=
  identifier

strategyType ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `tableDefinition` 为标准分片规则定义；`autoTableDefinition`
  为自动分片规则定义。标准分片规则和自动分片规则可参考[数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)；
- 当使用标准分片时：
    - `DATANODES` 只能使用已经添加到当前逻辑库的资源，且只能使用 INLINE 表达式指定需要的资源；
    - `DATABASE_STRATEGY`、`TABLE_STRATEGY` 表示分库和分表策略，均为可选项，未配置时使用默认策略；
    - `strategyDefinition` 中属性 `TYPE` 用于指定[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/#自定义类分片算法)的类型，目前仅支持 `STANDARD`
      、`COMPLEX`。使用 `COMPLEX` 时需要用 `SHARDING_COLUMNS` 指定多个分片键。
- 当使用自动分片时：
    - `STORAGE_UNITS` 只能使用已经添加到当前逻辑库的资源，可通过枚举或 INLINE 表达式指定需要的资源；
    - 只能使用自动分片算法，可参考[自动分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/#自动分片算法)。
- `algorithmType` 为分片算法类型，分片算法类型请参考[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)；
- 自动生成的算法命名规则为  `tableName` _ `strategyType` _ `algorithmType`；
- 自动生成的主键策略命名规则为 `tableName` _ `strategyType；
- `KEY_GENERATE_STRATEGY`
  用于指定主键生成策略，为可选项，关于主键生成策略可参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)；
- `AUDIT_STRATEGY`
  用于指定分配审计生成策略，为可选项，关于分片审计生成策略可参考[分片审计](/cn/user-manual/common-config/builtin-algorithm/audit/)；
- `ifNotExists` 子句用于避免出现 `Duplicate sharding rule` 错误。

### 示例

#### 1.标准分片规则

```sql
CREATE SHARDING TABLE RULE t_order_item (
DATANODES("ds_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}")))),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY (TYPE(NAME="DML_SHARDING_CONDITIONS"),ALLOW_HINT_DISABLE=true)
);
```

#### 2.自动分片规则

```sql
CREATE SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY (TYPE(NAME="DML_SHARDING_CONDITIONS"),ALLOW_HINT_DISABLE=true)
);
```

#### 3.使用 `ifNotExists` 子句创建分片规则

- 标准分片规则

```sql
CREATE SHARDING TABLE RULE IF NOT EXISTS t_order_item (
DATANODES("ds_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}")))),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY (TYPE(NAME="DML_SHARDING_CONDITIONS"),ALLOW_HINT_DISABLE=true)
);
```

- 自动分片规则

```sql
CREATE SHARDING TABLE RULE IF NOT EXISTS t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY (TYPE(NAME="DML_SHARDING_CONDITIONS"),ALLOW_HINT_DISABLE=true)
);
```

### 保留字

`CREATE`、`SHARDING`、`TABLE`、`RULE`、`DATANODES`、`DATABASE_STRATEGY`、`TABLE_STRATEGY`、`KEY_GENERATE_STRATEGY`、`STORAGE_UNITS`、`SHARDING_COLUMN`、`TYPE`、`SHARDING_COLUMN`、`KEY_GENERATOR`、`SHARDING_ALGORITHM`、`COLUMN`、`NAME`、`PROPERTIES`、`AUDIT_STRATEGY`、`AUDITORS`、`ALLOW_HINT_DISABLE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE DEFAULT_SHARDING STRATEGY](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-default-sharding-strategy/)
