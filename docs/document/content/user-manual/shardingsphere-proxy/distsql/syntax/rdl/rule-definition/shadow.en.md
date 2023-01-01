+++
title = "Shadow"
weight = 6
+++

## Syntax

```sql
CREATE SHADOW RULE ifNotExistsClause? shadowRuleDefinition [, shadowRuleDefinition] ... 

ALTER SHADOW RULE shadowRuleDefinition [, shadowRuleDefinition] ... 

DROP SHADOW RULE ruleName [, ruleName] ...

DROP SHADOW ALGORITHM algorithmName [, algorithmName] ...
    
CREATE DEFAULT SHADOW ALGORITHM ifNotExistsClause? shadowAlgorithm

ALTER DEFAULT SHADOW ALGORITHM shadowAlgorithm

DROP DEFAULT SHADOW ALGORITHM [IF EXISTS]

SHOW DEFAULT SHADOW ALGORITHM

SHOW SHADOW ALGORITHMS

ifNotExistsClause:
    IF NOT EXISTS

shadowRuleDefinition: ruleName(storageUnitMapping, shadowTableRule [, shadowTableRule] ...)

storageUnitMapping: SOURCE=storageUnitName, SHADOW=storageUnitName

shadowTableRule: tableName(shadowAlgorithm [, shadowAlgorithm] ...)

shadowAlgorithm: TYPE(NAME=shadowAlgorithmType, PROPERTIES([algorithmProperties] ...))

algorithmProperties: algorithmProperty [, algorithmProperty] ... 

algorithmProperty: key=value
```

### Parameters Explained
| name                | DateType   | Description           |
|:--------------------|:-----------|:----------------------|
| ruleName            | IDENTIFIER | Rule name             |
| storageUnitName     | IDENTIFIER | Storage unit name     |
| tableName           | IDENTIFIER | Shadow table name     |
| algorithmName       | IDENTIFIER | Shadow algorithm name |
| shadowAlgorithmType | STRING     | Shadow algorithm type | 

### Notes

- Duplicate `ruleName` cannot be created
- `storageUnitMapping` specifies the mapping relationship between the source database and the shadow library. You need to use the `storage unit` managed by RDL, please refer to [storage unit](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)
- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`
- When executing `ALTER SHADOW RULE`, the corresponding `shadowAlgorithm` will not be removed
- When creating shadow rule, `algorithmName` will be automatically generated according to `ruleName`, `tableName`, `shadowAlgorithmType` and algorithm collection index. The default name is `default_shadow_algorithm`.


## Example

```sql
CREATE SHADOW RULE IF NOT EXISTS shadow_rule(
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

CREATE DEFAULT SHADOW ALGORITHM IF NOT EXISTS TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=true, "foo"="bar"));

ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"=false, "foo"="bar"));

SHOW DEFAULT SHADOW ALGORITHM;

DROP DEFAULT SHADOW ALGORITHM;
```
