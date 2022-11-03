+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 5
+++

## Description

The `CREATE DEFAULT SHADOW ALGORITHM` syntax is used to create a default shadow algorithm.

### Syntax

```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' shadowAlgorithm 

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' ( 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ) ')' ')'
    
shadowAlgorithmType ::=
  string
```

### Supplement

- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`.

### Example

- Create default shadow algorithm

```sql
CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar");
```

### Reserved word

`CREATE`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
