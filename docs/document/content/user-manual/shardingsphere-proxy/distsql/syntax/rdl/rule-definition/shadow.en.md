+++
title = "Shadow"
weight = 6
+++

## Syntax

```sql
CREATE SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

CREATE SHADOW ALGORITHM shadowAlgorithm [, shadowAlgorithm] ...

ALTER SHADOW ALGORITHM shadowAlgorithm [, shadowAlgorithm] ...

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName] ...

CREATE DEFAULT SHADOW ALGORITHM NAME = algorithmName

shadowRuleDefinition: ruleName(resourceMapping, shadowTableRule [, shadowTableRule] ...)

resourceMapping: SOURCE=resourceName, SHADOW=resourceName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: ([algorithmName, ] TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...)))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

### Parameters Explained
| name                | DateType   | Description           |
|:--------------------|:-----------|:----------------------|
| ruleName            | IDENTIFIER | Rule name             |
| resourceName        | IDENTIFIER | Resource name         |
| tableName           | IDENTIFIER | Shadow table name     |
| algorithmName       | IDENTIFIER | Shadow algorithm name |
| shadowAlgorithmType | STRING     | Shadow algorithm type | 

### Notes

-  Duplicate `ruleName` cannot be created
- `resourceMapping` specifies the mapping relationship between the source database and the shadow library. You need to use the `resource` managed by RDL, please refer to [resource](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/)
- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time
- If `algorithmName` is not specified, it will be automatically generated according to `ruleName`, `tableName` and `shadowAlgorithmType`
- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`
- `shadowTableRule` can be reused by different `shadowRuleDefinition`, so when executing `DROP SHADOW RULE`, the corresponding `shadowTableRule` will not be removed
- `shadowAlgorithm` can be reused by different `shadowTableRule`, so when executing `ALTER SHADOW RULE`, the corresponding `shadowAlgorithm` will not be removed


## Example

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

CREATE SHADOW ALGORITHM 
(simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))), 
(user_id_match_algorithm, TYPE(NAME="REGEX_MATCH",PROPERTIES("operation"="insert", "column"="user_id", "regex"='[1]')));


ALTER SHADOW ALGORITHM 
(simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="false", "foo"="bar"))), 
(user_id_match_algorithm, TYPE(NAME="VALUE_MATCH",PROPERTIES("operation"="insert", "column"="user_id", "value"='1')));

DROP SHADOW RULE shadow_rule;

DROP SHADOW ALGORITHM simple_hint_algorithm;

CREATE DEFAULT SHADOW ALGORITHM NAME = simple_hint_algorithm;
```
