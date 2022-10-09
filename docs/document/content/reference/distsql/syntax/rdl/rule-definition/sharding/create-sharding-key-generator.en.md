+++
title = "CREATE SHARDING KEY GENERATOR"
weight = 8
+++

## Description

The `CREATE SHARDING KEY GENERATOR` syntax is used to add a distributed primary key generator for the currently selected
logic database

### Syntax

```sql
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

keyGeneratorName ::=
  identifier
  
algorithmType ::=
  string
```

### Supplement

- `algorithmType` is the key generate algorithm type. For detailed key generate algorithm type information, please refer
  to [KEY GENERATE ALGORITHM](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen/).

### Example

#### Create a distributed primary key generator

```sql
CREATE SHARDING KEY GENERATOR snowflake_key_generator (
    TYPE(NAME="SNOWFLAKE", PROPERTIES("max-vibration-offset"="3"))
);
```

### Reserved word

`CREATE`, `SHARDING`, `KEY`, `GENERATOR`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
