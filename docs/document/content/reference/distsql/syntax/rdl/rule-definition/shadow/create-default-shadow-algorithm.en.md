+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 4
+++

## Description

The `CREATE DEFAULT SHADOW ALGORITHM` syntax is used to create a default shadow algorithm.

### Syntax

```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' 'NAME' '=' algorithmName
    
algorithmName ::=
  identifier
```

### Example

#### Create a shadow algorithm

```sql
CREATE DEFAULT SHADOW ALGORITHM NAME = simple_hint_algorithm;
```

### Reserved word

`CREATE`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `NAME`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
