+++
title = "CREATE SHADOW RULE"
weight = 2
+++

## 描述

`CREATE SHADOW RULE` 语法用于创建影子库压测规则。

### 语法定义

```sql
CreateShadowRule ::=
  'CREATE' 'SHADOW' 'RULE' shadowDefinition ( ',' shadowDefinition )*

shadowDefinition ::=
  ruleName '(' storageUnitMapping shadowTableRule ( ',' shadowTableRule )* ')'
    
storageUnitMapping ::=
    'SOURCE' '=' storageUnitName ',' 'SHADOW' '=' storageUnitName

shadowTableRule ::=
    tableName '(' shadowAlgorithm ')'
    
shadowAlgorithm ::=
    'TYPE' '('  'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' ) ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

tableName ::=
  identifier

algorithmName ::=
  identifier

shadowAlgorithmType ::=
  string
```

### 补充说明

- 重复的 `ruleName` 无法被创建；
- `storageUnitMapping` 指定源数据库和影子库的映射关系，需使用 `RDL` 管理的 `STORAGE UNIT`
  ，请参考 [存储单元](https://shardingsphere.apache.org/document/current/cn/reference/distsql/syntax/rdl/storage-unit-definition/)；
- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `algorithmName` 会根据 `ruleName`、`tableName` 和 `shadowAlgorithmType` 自动生成；
- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`。

### 示例

- 创建影子库压测规则

```sql
CREATE SHADOW RULE shadow_rule(
  SOURCE=demo_su,
  SHADOW=demo_su_shadow,
  t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

### 保留字

`CREATE`、`SHADOW`、`RULE`、`SOURCE`、`SHADOW`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [存储单元](https://shardingsphere.apache.org/document/current/cn/reference/distsql/syntax/rdl/storage-unit-definition/)