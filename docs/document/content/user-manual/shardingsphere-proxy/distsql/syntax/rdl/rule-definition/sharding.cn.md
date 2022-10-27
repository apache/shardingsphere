+++
title = "数据分片"
weight = 1
+++

## 语法说明

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

CREATE DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

ALTER DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

DROP DEFAULT SHARDING shardingScope STRATEGY;

DROP SHARDING ALGORITHM algorithmName [, algorithmName] ...

DROP SHARDING KEY GENERATOR [IF EXISTS] keyGeneratorName [, keyGeneratorName] ...
    
CREATE SHARDING AUDITOR auditorDefinition [, auditorDefinition] ...
    
ALTER SHARDING AUDITOR auditorDefinition [, auditorDefinition] ...
    
DROP SHARDING AUDITOR [IF EXISTS] auditorName [, auditorName] ...

shardingTableRuleDefinition:
    shardingAutoTableRule | shardingTableRule

shardingAutoTableRule:
    tableName(storageUnits, shardingColumn, algorithmDefinition [, keyGenerateDefinition] [, auditDeclaration])

shardingTableRule:
    tableName(dataNodes [, databaseStrategy] [, tableStrategy] [, keyGenerateDefinition] [, auditDeclaration])

storageUnits:
    STORAGE_UNITS(storageUnit [, storageUnit] ...)

dataNodes:
    DATANODES(dataNode [, dataNode] ...)

storageUnit:
    storageUnitName | inlineExpression

dataNode:
    dataNodeName | inlineExpression

shardingColumn:
    SHARDING_COLUMN=columnName

algorithmDefinition:
    TYPE(NAME=shardingAlgorithmType [, PROPERTIES([algorithmProperties])])

keyGenerateDefinition:
    KEY_GENERATE_STRATEGY(COLUMN=columnName, strategyDefinition)
    
auditDeclaration:
    auditDefinition | auditStrategy

auditDefinition:
    AUDIT_STRATEGY([(singleAuditDefinition),(singleAuditDefinition)], ALLOW_HINT_DISABLE=true)
    
singleAuditDefinition:
    NAME=auditor1, algorithmDefinition
    
auditStrategy:
    AUDIT_STRATEGY(AUDITORS=[auditor1,auditor2], ALLOW_HINT_DISABLE=true)

shardingScope:
    DATABASE | TABLE

databaseStrategy:
    DATABASE_STRATEGY(shardingStrategy)

tableStrategy:
    TABLE_STRATEGY(shardingStrategy)

shardingStrategy:
    TYPE=strategyType, shardingColumn, shardingAlgorithm

shardingAlgorithm:
    SHARDING_ALGORITHM(algorithmDefinition)

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties])])

shardingAlgorithmDefinition:
    shardingAlgorithmName(algorithmDefinition)

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value   

keyGeneratorDefinition: 
    keyGeneratorName (algorithmDefinition)

auditorDefinition:
    auditorName (auditorAlgorithmDefinition)
    
auditorAlgorithmDefinition:
    TYPE(NAME=auditorAlgorithmType [, PROPERTIES([algorithmProperties])])
```
- `STORAGE_UNITS` 需使用 RDL 管理的数据源资源；
- `shardingAlgorithmType` 指定自动分片算法类型，请参考  [自动分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)；
- `keyGenerateStrategyType` 指定分布式主键生成策略，请参考 [分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)；
- `auditorAlgorithmType` 指定分片审计策略，请参考 [分片审计](/cn/user-manual/common-config/builtin-algorithm/audit/)；
- 重复的 `tableName` 将无法被创建；
- `shardingAlgorithm` 能够被不同的 `Sharding Table Rule` 复用，因此在执行 `DROP SHARDING TABLE RULE` 时，对应的 `shardingAlgorithm` 不会被移除；
- 如需移除 `shardingAlgorithm`，请执行 `DROP SHARDING ALGORITHM`；
- `strategyType` 指定分片策略，请参考[分片策略](/cn/features/sharding/concept/sharding/#%E5%88%86%E7%89%87%E7%AD%96%E7%95%A5)；
- `Sharding Table Rule` 同时支持 `Auto Table` 和 `Table` 两种类型，两者在语法上有所差异，对应配置文件请参考 [数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/) ；
- 执行 `CREATE SHARDING TABLE RULE` 时，将会自动创建新的分片算法，算法命名规则为 `tableName_scope_shardingAlgorithmType`，如 `t_order_database_inline`。
- 执行 `CREATE DEFAULT SHARDING STRATEGY` 时，同样会自动创建新的分片算法，算法命名规则为 `default_scope_shardingAlgorithmType`，如 `default_database_inline`。

### Sharding Table Reference Rule

```sql
CREATE SHARDING TABLE REFERENCE RULE tableReferenceRuleDefinition [, tableReferenceRuleDefinition] ...

ALTER SHARDING TABLE REFERENCE RULE tableReferenceRuleDefinition [, tableReferenceRuleDefinition] ...

DROP SHARDING TABLE REFERENCE RULE tableReferenceRuleDefinition [, tableReferenceRuleDefinition] ...

tableReferenceRuleDefinition:
    (tableName [, tableName] ... )
```
- `ALTER` 会使用新的配置覆盖数据库内的绑定表配置

### Broadcast Table Rule

```sql
CREATE BROADCAST TABLE RULE tableName [, tableName] ...

DROP BROADCAST TABLE RULES tableName [, tableName] ...
```

## 示例

### Sharding Table Rule

*Key Generator*

```sql
DROP SHARDING KEY GENERATOR snowflake_key_generator;
```

*Auditor*

```sql
CREATE SHARDING AUDITOR sharding_key_required_auditor (
TYPE(NAME="DML_SHARDING_CONDITIONS")
);

ALTER SHARDING AUDITOR sharding_key_required_auditor (
TYPE(NAME="DML_SHARDING_CONDITIONS")
);

DROP SHARDING AUDITOR IF EXISTS sharding_key_required_auditor;
```

*Auto Table*
```sql
CREATE SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY(AUDITORS=[auditor1,auditor2],ALLOW_HINT_DISABLE=true)
);

ALTER SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1,ds_2,ds_3),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="16")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY(AUDITORS=[auditor1,auditor2],ALLOW_HINT_DISABLE=true)
);

DROP SHARDING TABLE RULE t_order;

DROP SHARDING ALGORITHM t_order_hash_mod;
```

*Table*

```sql

CREATE SHARDING TABLE RULE t_order_item (
DATANODES("ds_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}")))),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY(AUDITORS=[auditor1,auditor2],ALLOW_HINT_DISABLE=true)
);

ALTER SHARDING TABLE RULE t_order_item (
DATANODES("ds_${0..3}.t_order_item${0..3}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${user_id % 4}")))),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 4}")))),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake")),
AUDIT_STRATEGY(AUDITORS=[auditor1,auditor2],ALLOW_HINT_DISABLE=true)
);

DROP SHARDING TABLE RULE t_order_item;

DROP SHARDING ALGORITHM database_inline;

CREATE DEFAULT SHARDING DATABASE STRATEGY (
TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${order_id % 2}")))
);

ALTER DEFAULT SHARDING DATABASE STRATEGY (
TYPE="standard",SHARDING_COLUMN=another_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="ds_${another_id % 2}")))
);

DROP DEFAULT SHARDING DATABASE STRATEGY;
```

### Sharding Table Reference Rule

```sql
CREATE SHARDING TABLE REFERENCE RULE (t_order,t_order_item),(t_1,t_2);

ALTER SHARDING TABLE REFERENCE RULE (t_order,t_order_item);

DROP SHARDING TABLE REFERENCE RULE;

DROP SHARDING TABLE REFERENCE RULE (t_order,t_order_item);
```

### Broadcast Table Rule

```sql
CREATE BROADCAST TABLE RULE t_a,t_b;

DROP BROADCAST TABLE RULE t_a;
```
