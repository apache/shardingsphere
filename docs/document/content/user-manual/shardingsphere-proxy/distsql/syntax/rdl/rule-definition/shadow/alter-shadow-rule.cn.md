+++
title = "ALTER SHADOW RULE"
weight = 2
+++

## 描述

`ALTER SHADOW RULE` 语法用于修改影子库压测规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterShadowRule ::=
  'ALTER' 'SHADOW' 'RULE' shadowRuleDefinition (',' shadowRuleDefinition)*

shadowRuleDefinition ::=
  ruleName '(' storageUnitMapping shadowTableRule (',' shadowTableRule)* ')'
    
storageUnitMapping ::=
  'SOURCE' '=' storageUnitName ',' 'SHADOW' '=' storageUnitName

shadowTableRule ::=
  tableName '(' shadowAlgorithm ')'
    
shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' propertiesDefinition ')'

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

- `storageUnitMapping` 指定源数据库和影子库的映射关系，需使用 `RDL` 管理的 `STORAGE UNIT`
  ，请参考 [存储单元](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)；
- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `algorithmName` 会根据 `ruleName`、`tableName` 和 `shadowAlgorithmType` 自动生成；
- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SQL_HINT`。

### 示例

- 修改影子库压测规则

```sql
ALTER SHADOW RULE shadow_rule(
  SOURCE=demo_ds,
  SHADOW=demo_ds_shadow,
  t_order(TYPE(NAME="SQL_HINT")), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

### 保留字

`ALTER`、`SHADOW`、`RULE`、`SOURCE`、`SHADOW`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [存储单元](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)