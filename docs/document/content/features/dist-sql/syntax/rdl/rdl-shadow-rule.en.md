+++
title = "影子库压测"
weight = 6
+++

## 定义

```sql
CREATE SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW ALGORITHMS shadowAlgorithm [, shadowAlgorithm] ...

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName] ...

shadowRuleDefinition: ruleName([dataSourceMapping,] shadowTableRule [, shadowTableRule] ...)

dataSourceMapping: SOURCE=dataSourceName, SHADOW=dataSourceName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: ([algorithmName, ] TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...)))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

- Please refer to the [shadow](https://shardingsphere.apache.org/document/current/cn/features/shadow/)
- Duplicate `ruleName` cannot be created
- `dataSourceMapping` specify the mapping relationship between the source database and the shadow database, you need to use the `Resource` resource managed by RDL, please refer to [Resource](https://shardingsphere.apache.org/document/current/cn/features/dist-sql/syntax/rdl/rdl-resource/)
- `shadowTableRule` 影子表的规则，与 `dataSourceMapping` 没有关联关系，会应用于所有的 `dataSourceMapping`
- Duplicate `tableName`  cannot be created
- `shadowAlgorithm` is associated with `tableName` and can be applied to multiple `tableName` at the same time
- If `algorithmName` is not filled in, it will be automatically generated according to `tableName` and `shadowAlgorithmType`(Recommended database naming convention)
- `TYPE` currently supports `COLUMN_REGEX_MATCH` and `SIMPLE_NOTE`
- `shadowTableRule` can be reused by different `ruleName`, so when executing `DROP SHADOW RULE`, the corresponding `shadowTableRule` will not be removed
- `ALTER SHADOW RULE` uses `ruleName` and `algorithmName` as the basis for modification. When modifying, `tableName` will be overwritten, but `shadowAlgorithm` will only be added but not overwritten


## 示例

```sql
CREATE SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));



CREATE SHADOW RULE 
shadow_rule(SOURCE=demo_ds,SHADOW=demo_ds_shadow),
shadow_rule_1(SOURCE=demo_ds,SHADOW=demo_ds_shadow),
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar"))));



ALTER SHADOW RULE shadow_rule(
SOURCE=demo_ds,
SHADOW=demo_ds_shadow,
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));

ALTER SHADOW ALGORITHM 
(simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar"))), 
(user_id_match_algorithm, TYPE(NAME=COLUMN_REGEX_MATCH,PROPERTIES("operation"="insert", "column"="user_id", "regex"='[1]')));

DROP SHADOW RULE shadow_rule, shadow_rule;

DROP SHADOW ALGORITHM simple_note_algorithm;
```
