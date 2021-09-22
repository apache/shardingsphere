+++
title = "影子库压测"
weight = 6
+++

## 定义

```sql
CREATE SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW ALGORITHM shadowAlgorithm [, shadowAlgorithm] ...

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName] ...

shadowRuleDefinition: ruleName(resourceMapping, shadowTableRule [, shadowTableRule] ...)

resourceMapping: SOURCE=resourceName, SHADOW=resourceName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: ([algorithmName, ] TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...)))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

- `Shadow` please refer to [Shadow](https://shardingsphere.apache.org/document/current/cn/features/shadow/)
- `ruleName` Duplicate name cannot be created
- `resourceMapping` specifies the mapping relationship between the source database and the shadow library. You need to use the `Resource` resource managed by RDL, please refer to [Resources](https:shardingsphere.apache.orgdocumentcurrentcnfeaturesdist-sqlsyntaxrdlrdl-resource)
- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time
- If `algorithmName` is not filled in, it will be automatically generated according to `ruleName`, `tableName` and `shadowAlgorithmType` (recommended database naming convention)
- `TYPE` currently supports `COLUMN_REGEX_MATCH` and `SIMPLE_NOTE`
- `shadowTableRule` can be reused by different `shadowRuleDefinition`, so when executing `DROP SHADOW RULE`, the corresponding `shadowTableRule` will not be removed
- `ALTER SHADOW RULE` takes `ruleName` as the modification condition. When modifying `shadowTableRule`, the existing `shadowTableRule` of the `shadowRuleDefinition` will be overwritten
- `shadowAlgorithm` can be reused by different `shadowTableRule`, so when executing `ALTER SHADOW RULE`, the corresponding `shadowAlgorithm` will not be removed


## 示例

```sql
CREATE SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));

ALTER SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));

ALTER SHADOW ALGORITHM 
(simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar"))), 
(user_id_match_algorithm, TYPE(NAME=COLUMN_REGEX_MATCH,PROPERTIES("operation"="insert", "column"="user_id", "regex"='[1]')));

DROP SHADOW RULE shadow_rule;

DROP SHADOW ALGORITHM simple_note_algorithm;
```
