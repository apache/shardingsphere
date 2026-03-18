+++
title = "ALTER SQL_TRANSLATOR RULE"
weight = 6
+++

### Description

The `ALTER SQL_TRANSLATOR RULE` syntax is used to alter the SQL translator rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterSQLTranslatorRule ::=
  'ALTER' 'SQL_TRANSLATOR' 'RULE' '(' sqlTranslatorRuleDefinition ')'

sqlTranslatorRuleDefinition ::=
  algorithmDefinition (',' useOriginalSQLWhenTranslatingFailedClause)?

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' translatorType (',' 'PROPERTIES' '(' propertiesDefinition ')' )? ')'

useOriginalSQLWhenTranslatingFailedClause ::=
  'USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED' '=' booleanLiteral

translatorType ::=
  identifier

propertiesDefinition ::=
  property (',' property)*

property ::=
  key '=' value

booleanLiteral ::=
  'TRUE' | 'FALSE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Note

- `TYPE` declares the SQL translator implementation and its `PROPERTIES`.
- `USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED` controls whether the original SQL should be used when translation fails.

### Example

- Alter SQL translator rule with fallback on translation failure

```sql
ALTER SQL_TRANSLATOR RULE (
  TYPE(NAME='NATIVE', PROPERTIES('key'='value')),
  USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED=TRUE
);
```

- Alter SQL translator rule without specifying fallback

```sql
ALTER SQL_TRANSLATOR RULE (
  TYPE(NAME='NATIVE', PROPERTIES('key'='value'))
);
```

### Reserved word

`ALTER`, `SQL_TRANSLATOR`, `RULE`, `TYPE`, `NAME`, `PROPERTIES`, `USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
