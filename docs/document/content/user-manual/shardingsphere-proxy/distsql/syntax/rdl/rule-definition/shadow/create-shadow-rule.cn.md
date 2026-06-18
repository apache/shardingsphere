+++
title = "CREATE SHADOW RULE"
weight = 1
+++

## 描述

`CREATE SHADOW RULE` 语法用于创建影子库压测规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateShadowRule ::=
  'CREATE' 'SHADOW' 'RULE' ifNotExists? shadowRuleDefinition (',' shadowRuleDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

shadowRuleDefinition ::=
  ruleName '(' storageUnitMapping shadowTableRule (',' shadowTableRule)* ')'
    
storageUnitMapping ::=
  'SOURCE' '=' storageUnitName ',' 'SHADOW' '=' storageUnitName

shadowTableRule ::=
  tableName '(' shadowAlgorithm ')'
    
shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefinition ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

tableName ::=
  identifier

algorithmName ::=
  identifier

algorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 重复的 `ruleName` 无法被创建；
- `storageUnitMapping` 指定源数据库和影子库的映射关系，需使用 `RDL` 管理的 `STORAGE UNIT`
  ，请参考 [存储单元](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)；
- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `algorithmName` 会根据 `ruleName`、`tableName` 和 `algorithmType` 自动生成；
- `algorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SQL_HINT`；
- `ifNotExists` 子句用于避免出现 `Duplicate shadow rule` 错误。

### 示例

- 创建影子库压测规则

```sql
CREATE SHADOW RULE shadow_rule(
  SOURCE=demo_ds,
  SHADOW=demo_ds_shadow,
  t_order(TYPE(NAME="SQL_HINT")), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

- 使用 `ifNotExists` 子句创建影子库压测规则

```sql
CREATE SHADOW RULE IF NOT EXISTS shadow_rule(
  SOURCE=demo_ds,
  SHADOW=demo_ds_shadow,
  t_order(TYPE(NAME="SQL_HINT")), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

### 保留字

`CREATE`、`SHADOW`、`RULE`、`SOURCE`、`SHADOW`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [存储单元](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)