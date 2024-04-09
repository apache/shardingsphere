+++
title = "CREATE DEFAULT SHADOW ALGORITHM"
weight = 4
+++

## Description

The `CREATE DEFAULT SHADOW ALGORITHM` syntax is used to create a default shadow algorithm.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateDefaultShadowAlgorithm ::=
  'CREATE' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifNotExists? shadowAlgorithm 

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefiinition ')'
    
algorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `algorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SQL_HINT`;
- `ifNotExists` clause is used for avoid `Duplicate default shadow algorithm` error.

### Example

- Create default shadow algorithm

```sql
CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME="SQL_HINT");
```

- Create default shadow algorithm with `ifNotExist` clause

```sql
CREATE DEFAULT SHADOW ALGORITHM IF NOT EXISTS TYPE(NAME="SQL_HINT");
```

### Reserved word

`CREATE`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
