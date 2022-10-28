+++
title = "ALTER DEFAULT SHADOW ALGORITHM"
weight = 6
+++

## Description

The `ALTER DEFAULT SHADOW ALGORITHM` syntax is used to alter a default shadow algorithm.

### Syntax

```sql
AlterDefaultShadowAlgorithm ::=
  'ALTER' 'DEFAULT' 'SHADOW' 'ALGORITHM' shadowAlgorithm 

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' ( 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ) ')' ')'
    
shadowAlgorithmType ::=
  string
```

### Supplement

- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`.

### Example

- Alter default shadow algorithm

```sql
ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar");
```

### Reserved word

`ALTER`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
