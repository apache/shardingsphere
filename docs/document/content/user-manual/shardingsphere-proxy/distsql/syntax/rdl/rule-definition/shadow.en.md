+++
title = "Shadow"
weight = 6
+++

## Syntax

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
- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time
- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`
- `shadowTableRule` can be reused by different `shadowRuleDefinition`, so when executing `DROP SHADOW RULE`, the corresponding `shadowTableRule` will not be removed
- `shadowAlgorithm` can be reused by different `shadowTableRule`, so when executing `ALTER SHADOW RULE`, the corresponding `shadowAlgorithm` will not be removed
- When creating shadow rule, `algorithmName` will be automatically generated according to `ruleName`, `tableName`, `shadowAlgorithmType` and algorithm collection index. The default name is `default_shadow_algorithm`.


## Example

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
