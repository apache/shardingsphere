+++
title = "影子库压测"
weight = 6
+++

## 语法说明

```sql
CREATE SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName] ...

CREATE DEFAULT SHADOW ALGORITHM shadowAlgorithm

ALTER DEFAULT SHADOW ALGORITHM shadowAlgorithm

SHOW DEFAULT SHADOW ALGORITHM

SHOW SHADOW ALGORITHMS

shadowRuleDefinition: ruleName(resourceMapping, shadowTableRule [, shadowTableRule] ...)

resourceMapping: SOURCE=resourceName, SHADOW=resourceName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: ([algorithmName, ] TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...)))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

### 参数解释

| 名称                  | 数据类型       | 说明     |
|:--------------------|:-----------|:-------|
| ruleName            | IDENTIFIER | 规则名称   |
| resourceName        | IDENTIFIER | 数据库名称  |
| tableName           | IDENTIFIER | 影子表名称  |
| algorithmName       | IDENTIFIER | 影子算法名称 |
| shadowAlgorithmType | STRING     | 影子算法类型 |

### 注意事项

- 重复的`ruleName`无法被创建；
- `resourceMapping` 指定源数据库和影子库的映射关系，需使用 RDL 管理的 `resource` ，请参考 [数据源资源](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/)；
- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `algorithmName` 未指定时会根据 `ruleName`、`tableName` 和 `shadowAlgorithmType` 自动生成；
- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`；
- `shadowTableRule` 能够被不同的 `shadowRuleDefinition` 复用，因此在执行 `DROP SHADOW RULE` 时，对应的 `shadowTableRule` 不会被移除；
- `shadowAlgorithm` 能够被不同的 `shadowTableRule` 复用，因此在执行 `ALTER SHADOW RULE` 时，对应的 `shadowAlgorithm` 不会被移除。

## 示例

```sql
CREATE SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))),(TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))));

ALTER SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))),(TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))));

CREATE DEFAULT SHADOW ALGORITHM (simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar")));

ALTER DEFAULT SHADOW ALGORITHM (simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="false", "foo"="bar")));

DROP SHADOW RULE shadow_rule;

DROP SHADOW ALGORITHM simple_hint_algorithm;
```
