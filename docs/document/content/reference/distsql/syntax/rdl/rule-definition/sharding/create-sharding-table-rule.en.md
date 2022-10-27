+++
title = "CREATE SHARDING TABLE RULE"
weight = 2
+++

## Description

The `CREATE SHARDING TABLE RULE` syntax is used to add sharding table rule for the currently selected database

### Syntax

```sql
CreateShardingTableRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'RULE' ( tableDefinition | autoTableDefinition ) ( ',' ( tableDefinition | autoTableDefinition ) )*

tableDefinition ::= 
   tableName '(' 'DATANODES' '(' dataNode ( ',' dataNode )* ')' ( ',' 'DATABASE_STRATEGY' '(' strategyDefinition ')' )? ( ',' 'TABLE_STRATEGY' '(' strategyDefinition ')' )?  ( ',' 'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')' )? ( ',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')' )? ')'

autoTableDefinition ::=
    tableName '(' 'STORAGE_UNITS' '(' storageUnitName ( ',' storageUnitName )*  ')' ',' 'SHARDING_COLUMN' '=' columnName ',' algorithmDefinition ( ',' 'KEY_GENERATE_STRATEGY' '(' keyGenerateStrategyDefinition ')' )? ( ',' 'AUDIT_STRATEGY' '(' auditStrategyDefinition ')' )? ')'

strategyDefinition ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' | 'SHARDING_COLUMNS' ) '=' columnName ',' algorithmDefinition

keyGenerateStrategyDefinition ::= 
  'KEY_GENERATE_STRATEGY' '(' 'COLUMN' '=' columnName ',' ( 'KEY_GENERATOR' '=' algorihtmName | algorithmDefinition ) ')' 

algorithmDefinition ::=
  ('SHARDING_ALGORITHM' '=' algorithmName | 'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  )

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )* 
    
tableName ::=
  identifier

storageUnitName ::=
  identifier

columnName ::=
  identifier
    
auditorName ::=
  identifier

algorithmName ::=
  identifier
    
algorithmType ::=
  string
```

### Supplement

- `tableDefinition` is defined for standard sharding table rule; `autoTableDefinition` is defined for auto sharding
  table rule. For standard sharding rules and auto sharding rule, refer
  to [Data Sharding](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/);
- use standard sharding table rule:
    - `DATANODES` can only use resources that have been added to the current database, and can only use INLINE
      expressions to specify required resources;
    - `DATABASE_STRATEGY`, `TABLE_STRATEGY` are the database sharding strategy and the table sharding strategy, which
      are optional, and the default strategy is used when not configured;
    - The attribute `TYPE` in `strategyDefinition` is used to specify the type
      of [Sharding Algorithm](/en/features/sharding/concept/sharding/#user-defined-sharding-algorithm), currently only
      supports `STANDARD`, `COMPLEX`. Using `COMPLEX` requires specifying multiple sharding columns
      with `SHARDING_COLUMNS`.
- use auto sharding table rule:
    - `STORAGE_UNITS` can only use storage units that have been added to the current database, and the required storage units can be
      specified by enumeration or INLINE expression;
    - Only auto sharding algorithm can be used, please refer
      to [Auto Sharding Algorithm](/en/user-manual/common-config/builtin-algorithm/sharding/#auto-sharding-algorithm).
- `algorithmType` is the sharding algorithm type, please refer
  to [Sharding Algorithm](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/sharding);
- The auto-generated algorithm naming rule is `tableName` _ `strategyType` _ `shardingAlgorithmType`;
- The auto-generated primary key strategy naming rule is `tableName` _ `strategyType`;
- `KEY_GENERATE_STRATEGY` is used to specify the primary key generation strategy, which is optional. For the primary key
  generation strategy, please refer
  to [Distributed Primary Key](/en/user-manual/common-config/builtin-algorithm/keygen/).
- `AUDIT_STRATEGY` is used to specify the sharding audit strategy, which is optional. For the sharding audit
  generation strategy, please refer
  to [Sharding Audit](/en/user-manual/common-config/builtin-algorithm/audit/).

### Example

#### 1.Standard sharding table rule

- ##### Create standard sharding table rule by specifying sharding algorithms

```SQL
-- create sharding algorithms
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- create a sharding rule by specifying sharding algorithms
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    DATABASE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM=database_inline),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline)
);
```

- ##### Use the default sharding database strategy, create standard sharding table rule by specifying a sharding algorithm

```sql
-- create sharding algorithms
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- create a default sharding database strategy
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=database_inline
);

-- create a sharding table rule by specifying a sharding algorithm
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline)
);
```

- ##### Use both the default sharding and the default sharding strategy, create standard sharding table rule

```SQL
-- create sharding algorithms
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
), table_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
); 

-- create a default sharding database strategy
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=database_inline
);

-- create a default sharding table strategy
CREATE DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=order_id, SHARDING_ALGORITHM=table_inline
);

-- create a sharding table rule 
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}")
);
```

- ##### Create standard sharding table rule and sharding algorithms at the same time

```sql
CREATE SHARDING TABLE RULE t_order (
    DATANODES("ds_${0..1}.t_order_${0..1}"),
    DATABASE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
    TABLE_STRATEGY(TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="ds_${order_id % 2}"))))
);
```

#### 2.Auto sharding table rule

- ##### create auto sharding table rule

```sql
CREATE SHARDING TABLE RULE t_order (
    STORAGE_UNITS(ds_0, ds_1),
    SHARDING_COLUMN=order_id, TYPE(NAME="MOD", PROPERTIES("sharding-count"="4"))
);
```

### Reserved word

`CREATE`, `SHARDING`, `TABLE`, `RULE`, `DATANODES`, `DATABASE_STRATEGY`, `TABLE_STRATEGY`, `KEY_GENERATE_STRATEGY`, `STORAGE_UNITS`, `SHARDING_COLUMN`, `TYPE`, `SHARDING_COLUMN`, `KEY_GENERATOR`, `SHARDING_ALGORITHM`, `COLUMN`, `NAME`, `PROPERTIES`, `AUDIT_STRATEGY`, `AUDITORS`, `ALLOW_HINT_DISABLE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING ALGORITHM](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-algorithm/)
- [CREATE DEFAULT_SHARDING STRATEGY](/en/reference/distsql/syntax/rdl/rule-definition/create-default-sharding-strategy/)
