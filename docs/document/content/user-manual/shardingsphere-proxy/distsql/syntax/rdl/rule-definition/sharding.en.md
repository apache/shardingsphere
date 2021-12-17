+++
title = "Sharding"
weight = 1
+++

## Syntax

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

CREATE DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

CREATE SHARDING ALGORITHM shardingAlgorithmDefinition [,  shardingAlgorithmDefinition] ...

ALTER SHARDING ALGORITHM shardingAlgorithmDefinition [,  shardingAlgorithmDefinition] ...

DROP SHARDING ALGORITHM algorithmName [, algorithmName] ...

shardingTableRuleDefinition:
    shardingAutoTableRule | shardingTableRule
   
shardingAutoTableRule:
    tableName(resources COMMA shardingColumn COMMA algorithmDefinition (COMMA keyGenerateStrategy)?)
    
shardingTableRule:
    tableName(dataNodes (COMMA  databaseStrategy)? (COMMA tableStrategy)? (COMMA keyGenerateStrategy)?)

resources:
    RESOURCES(resource [, resource] ...)

dataNodes:
    DATANODES(dataNode [, dataNode] ...)

resource:
    resourceName | inlineExpression

dataNode:
    resourceName | inlineExpression

shardingColumn:
    SHARDING_COLUMN=columnName

algorithmDefinition:
    TYPE(NAME=shardingAlgorithmType [, PROPERTIES([algorithmProperties])])

keyGenerateStrategy:
    GENERATED_KEY(COLUMN=columnName, strategyDefinition)

shardingScope:
    DATABASE | TABLE

databaseStrategy:
    DATABASE_STRATEGY(shardingStrategy)

tableStrategy:
    TABLE_STRATEGY(shardingStrategy)

shardingStrategy:
    TYPE=strategyType, shardingColumn, shardingAlgorithm

shardingColumn:
    SHARDING_COLUMN=columnName
    
shardingAlgorithm:
    SHARDING_ALGORITHM=shardingAlgorithmName

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties])])

shardingAlgorithmDefinition:
    shardingAlgorithmName(algorithmDefinition)

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `RESOURCES` needs to use data source resources managed by RDL
- `shardingAlgorithmType` specifies the type of automatic sharding algorithm, please refer to [Auto Sharding Algorithm](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/sharding/)
- `keyGenerateStrategyType` specifies the distributed primary key generation strategy, please refer to [Key Generate Algorithm](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen/)
- Duplicate `tableName` will not be created
- `shardingAlgorithm` can be reused by different `Sharding Table Rule`, so when executing `DROP SHARDING TABLE RULE`, the corresponding `shardingAlgorithm` will not be removed
- To remove `shardingAlgorithm`, please execute `DROP SHARDING ALGORITHM`
- `strategyType` specifies the sharding strategy，please refer to[Sharding Strategy](/en/features/sharding/concept/sharding/#sharding-strategy)
- `Sharding Table Rule` supports both `Auto Table` and `Table` at the same time. The two types are different in syntax. For the corresponding configuration file, please refer to [Sharding](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

ALTER SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

DROP SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

bindTableRulesDefinition:
    (tableName [, tableName] ... )
```
- `ALTER` will overwrite the binding table configuration in the database with the new configuration

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

ALTER SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

DROP SHARDING BROADCAST TABLE RULES
```
- `ALTER` will overwrite the broadcast table configuration in the database with the new configuration

## Example

### Sharding Table Rule

*Auto Table*
```sql
CREATE SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1),
SHARDING_COLUMN=order_id,TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=4)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

ALTER SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1,resource_2,resource_3),
SHARDING_COLUMN=order_id,TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=16)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

DROP SHARDING TABLE RULE t_order;

DROP SHARDING ALGORITHM t_order_hash_mod;
```

*Table*

```sql
CREATE SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

ALTER SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..3}.t_order_item${0..3}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=uuid,PROPERTIES("worker-id"=123)))
);

DROP SHARDING TABLE RULE t_order_item;

CREATE DEFAULT SHARDING DATABASE STRATEGY (
TYPE = standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=algorithmsName
);

CREATE SHARDING ALGORITHM database_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="resource_${user_id % 2}"))
),table_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}"))
);

ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="resource_${user_id % 4}"))
),table_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="t_order_item_${order_id % 4}"))
);

DROP SHARDING ALGORITHM database_inline;
```

*Key Generator*

```sql
CREATE SHARDING KEY GENERATOR uuid_key_generator (
TYPE(NAME=uuid, PROPERTIES("worker-id"=123))
);

ALTER SHARDING KEY GENERATOR uuid_key_generator (
TYPE(NAME=uuid, PROPERTIES("worker-id"=123))
);

DROP SHARDING KEY GENERATOR uuid_key_generator;
```

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item),(t_1,t_2);

ALTER SHARDING BINDING TABLE RULES (t_order,t_order_item);

DROP SHARDING BINDING TABLE RULES;

DROP SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (t_b,t_a);

ALTER SHARDING BROADCAST TABLE RULES (t_b,t_a,t_3);

DROP SHARDING BROADCAST TABLE RULES;
```
