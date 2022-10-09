+++
title = "CREATE SHADOW ALGORITHM"
weight = 3
+++

## Description

The `CREATE SHADOW ALGORITHM` syntax is used to create a shadow algorithm.

### Syntax

```sql
CreateShadowAlgorithm ::=
  'CREATE' 'SHADOW' 'ALGORITHM' shadowAlgorithm ( ',' shadowAlgorithm )*

shadowAlgorithm ::=
  '(' ( algorithmName ',' )? 'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' ( 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ) ')' ')'
    
algorithmName ::=
  identifier

shadowAlgorithmType ::=
  string
```

### Supplement

- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time;
- If `algorithmName` is not specified, it will be automatically generated according to `ruleName`, `tableName`
  and `shadowAlgorithmType`;
- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`.

### Example

#### Create a shadow algorithm

```sql
CREATE SHADOW ALGORITHM 
  (simple_hint_algorithm, TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))), 
  (user_id_match_algorithm, TYPE(NAME="REGEX_MATCH",PROPERTIES("operation"="insert", "column"="user_id", "regex"='[1]'))
);
```

### Reserved word

`CREATE`, `SHADOW`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
