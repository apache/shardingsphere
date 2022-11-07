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

DROP DEFAULT SHADOW ALGORITHM [IF EXISTS]

SHOW DEFAULT SHADOW ALGORITHM

SHOW SHADOW ALGORITHMS

shadowRuleDefinition: ruleName(storageUnitMapping, shadowTableRule [, shadowTableRule] ...)

storageUnitMapping: SOURCE=storageUnitName, SHADOW=storageUnitName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

### 参数解释

| 名称                 | 数据类型     | 说明          |
|:--------------------|:-------------|:------------|
| ruleName            | IDENTIFIER   | 规则名称      |
| storageUnitName     | IDENTIFIER   | 存储单元名称   |
| tableName           | IDENTIFIER   | 影子表名称     |
| algorithmName       | IDENTIFIER   | 影子算法名称   |
| shadowAlgorithmType | STRING       | 影子算法类型   |

### 注意事项

- 重复的`ruleName`无法被创建；
- `storageUnitMapping` 指定源数据库和影子库的映射关系，需使用 RDL 管理的 `storage unit` ，请参考 [存储单元](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)；
- `shadowAlgorithm` 可同时作用于多个 `shadowTableRule`；
- `shadowAlgorithmType` 目前支持 `VALUE_MATCH`、`REGEX_MATCH` 和 `SIMPLE_HINT`；
- `shadowTableRule` 能够被不同的 `shadowRuleDefinition` 复用，因此在执行 `DROP SHADOW RULE` 时，对应的 `shadowTableRule` 不会被移除；
- `shadowAlgorithm` 能够被不同的 `shadowTableRule` 复用，因此在执行 `ALTER SHADOW RULE` 时，对应的 `shadowAlgorithm` 不会被移除。
- 创建规则时，会根据 `ruleName`、`tableName` 和 `shadowAlgorithmType` 和算法集合下标自动生成 `algorithmName` 。默认算法名称为 `default_shadow_algorithm`。

## 示例

```sql
CREATE SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=true, "foo"="bar")),TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]'))), 
t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"=1))));

ALTER SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=true, "foo"="bar")),TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]'))), 
t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"=1))));

DROP SHADOW RULE shadow_rule;

DROP SHADOW ALGORITHM simple_hint_algorithm;

CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=true, "foo"="bar"));

ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=false, "foo"="bar"));
    
SHOW DEFAULT SHADOW ALGORITHM;

DROP DEFAULT SHADOW ALGORITHM;
```
