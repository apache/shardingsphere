+++
title = "影子库压测"
weight = 6
+++

## 定义

```sql
CREATE SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW ALGORITHMS shadowAlgorithm [, shadowAlgorithm]..

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName]...

shadowRuleDefinition: ruleName(dataSourceMapping, shadowTableRule [, shadowTableRule]...)

dataSourceMapping: SOURCE=dataSourceName, SHADOW=dataSourceName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm]...)

shadowAlgorithm: ([algorithmName, ]TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties]...)))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

- `ruleName` 规则名称，重复的名称无法被创建
- `dataSourceMapping` 指定源数据库和影子库的映射关系，需使用 RDL 管理的`Resource`资源，请参考 [数据源资源](https://shardingsphere.apache.org/document/current/cn/features/dist-sql/syntax/rdl/rdl-resource/)
- `shadowTableRule` 影子表的规则，与`dataSourceMapping`没有关联关系，会应用于所有的`dataSourceMapping`
- `tableName` 影子表名称，重复的名称无法被创建
- `algorithmName` 算法名称（推荐数据库命名规范）， 未填写时会根据`tableName`和`shadowAlgorithmType`自动生成
- `TYPE` 算法类型，目前内置 `COLUMN_REGEX_MATCH`和`SIMPLE_NOTE`
- `PROPERTIES` 自定义属性

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

DROP SHADOW RULE shadow_rule, shadow_rule;

DROP SHADOW ALGORITHM simple_note_algorithm;
```
