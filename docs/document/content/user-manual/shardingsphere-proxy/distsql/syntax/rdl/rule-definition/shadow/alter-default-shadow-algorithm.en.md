+++
title = "ALTER DEFAULT SHADOW ALGORITHM"
weight = 5
+++

## Description

The `ALTER DEFAULT SHADOW ALGORITHM` syntax is used to alter a default shadow algorithm.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterDefaultShadowAlgorithm ::=
  'ALTER' 'DEFAULT' 'SHADOW' 'ALGORITHM' shadowAlgorithm 

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

- `algorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SQL_HINT`.

### Example

- Alter default shadow algorithm

```sql
ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME="SQL_HINT");
```

### Reserved word

`ALTER`, `DEFAULT`, `SHADOW`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
