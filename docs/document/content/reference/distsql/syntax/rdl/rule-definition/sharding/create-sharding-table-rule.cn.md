+++
title = "CREATE SHARDING TABLE RULE"
weight = 2
+++

## 描述

`CREATE SHARDING TABLE RULE` 语法用于为当前所选逻辑库添加分片规则

### 语法定义

```sql
CreateShardingTableRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'RULE' ( tableDefinition | autoTableDefinition ) ( ',' ( tableDefinition | autoTableDefinition ) )*

tableDefinition ::= 
   tableName '(' 'DATANODES' '(' dataNode ( ',' dataNode )* ')'  ( ',' 'DATABASE_STRATEGY' '(' strategyDefinition ')' )?  ( ',' 'TABLE_STRATEGY' '(' strategyDefinition ')' )?  ( ',' 'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')' )? ( ',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')' )? ')'

autoTableDefinition ::=
    tableName '(' 'STORAGE_UNITS' '(' storageUnit ( ',' storageUnit )*  ')' ',' 'SHARDING_COLUMN' '=' columnName ',' algorithmDefinition ( ',' 'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')' )? ( ',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')' )? ')'

strategyDefinition ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' | 'SHARDING_COLUMNS' ) '=' columnName ',' algorithmDefinition

keyGenerateStrategyDefinition ::= 
  'KEY_GENERATE_STRATEGY' '(' 'COLUMN' '=' columnName ',' ( 'KEY_GENERATOR' '=' algorihtmName | algorithmDefinition ) ')' 

auditStrategyDefinition ::= 
  'AUDIT_STRATEGY' '(' 'AUDITORS' '=' '[' auditorName ',' auditorName ']' ',' 'ALLOW_HINT_DISABLE' '=' 'TRUE | FALSE' ')'
  |
  'AUDIT_STRATEGY' '(' '[' 'NAME' '=' auditorName ',' algorithmDefinition ']' ',' '[' 'NAME' '=' auditorName ',' algorithmDefinition ']' ')'

algorithmDefinition ::=
  ('SHARDING_ALGORITHM' '=' algorithmName | 'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  )

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )* 
    
tableName ::=
  identifier

storageUnit ::=
  identifier

columnName ::=
  identifier
    
auditorName ::=
  identifier

algorithmName ::=
  identifier
    
algorithmType ::=
  string
```

### 补充说明

- `tableDefinition` 为标准分片规则定义；`autoTableDefinition`
  为自动分片规则定义。标准分片规则和自动分片规则可参考[数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)；
- 当使用标准分片时：
    - `DATANODES` 只能使用已经添加到当前逻辑库的资源，且只能使用 INLINE 表达式指定需要的资源；
    - `DATABASE_STRATEGY`、`TABLE_STRATEGY` 表示分库和分表策略，均为可选项，未配置时使用默认策略；
    - `strategyDefinition` 中属性 `TYPE` 用于指定[分片算法](/cn/features/sharding/concept/sharding/#自定义分片算法)的类型，目前仅支持 `STANDARD`
      、`COMPLEX`。使用 `COMPLEX` 时需要用 `SHARDING_COLUMNS` 指定多个分片键。
- 当使用自动分片时：
    - `STORAGE_UNITS` 只能使用已经添加到当前逻辑库的资源，可通过枚举或 INLINE 表达式指定需要的资源；
    - 只能使用自动分片算法，可参考[自动分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/#自动分片算法)。
- `algorithmType` 为分片算法类型，分片算法类型请参考[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)；
- 自动生成的算法命名规则为  `tableName` _ `strategyType` _ `algorithmType`；
- 自动生成的主键策略命名规则为 `tableName` _ `strategyType；
- `KEY_GENERATE_STRATEGY`
  用于指定主键生成策略，为可选项，关于主键生成策略可参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)。
- `AUDIT_STRATEGY`
  用于指定分配审计生成策略，为可选项，关于分片审计生成策略可参考[分片审计](/cn/user-manual/common-config/builtin-algorithm/audit/)。

### 示例

#### 1.标准分片规则

- ##### 指定分片算法创建标准分片规则

```sql
-- 创建分片算法
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- 指定分片算法创建分片规则
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    DATABASE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM=database_inline),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline)
);
```

- ##### 在默认分库策略下，通过指定分片算法创建标准分片规则

```sql
-- 创建分片算法
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- 创建默认分库策略
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=database_inline
);

-- 指定分片算法创建分片规则
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline)
);
```

- ##### 在默认分库分表策略下，通过指定分片算法创建标准分片规则

```sql
-- 创建分片算法
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- 创建默认分库策略
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=database_inline
);

-- 创建默认分表策略
CREATE DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline
);

-- 创建分片规则
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}")
);
```

- ##### 创建标准分片规则的同时创建分片算法

```sql
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    DATABASE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="ds_${order_id % 2}"))))
);
```

#### 2.自动分片规则

- ##### 创建自动分片规则

```sql
CREATE SHARDING TABLE RULE t_order (
    STORAGE_UNITS(ds_0, ds_1),
    SHARDING_COLUMN=order_id, TYPE(NAME="MOD", PROPERTIES("sharding-count"="4"))
);
```

### 保留字

`CREATE`、`SHARDING`、`TABLE`、`RULE`、`DATANODES`、`DATABASE_STRATEGY`、`TABLE_STRATEGY`、`KEY_GENERATE_STRATEGY`、`STORAGE_UNITS`、`SHARDING_COLUMN`、`TYPE`、`SHARDING_COLUMN`、`KEY_GENERATOR`、`SHARDING_ALGORITHM`、`COLUMN`、`NAME`、`PROPERTIES`、`AUDIT_STRATEGY`、`AUDITORS`、`ALLOW_HINT_DISABLE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING ALGORITHM](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-algorithm/)
- [CREATE DEFAULT_SHARDING STRATEGY](/cn/reference/distsql/syntax/rdl/rule-definition/create-default-sharding-strategy/)
