+++
title = "ALTER SQL_FEDERATION RULE"
weight = 9
+++

### Description

The `ALTER SQL_FEDERATION RULE` syntax is used to modify the federated query configuration.
Set `PROVIDER_TYPE=NONE` to disable SQL federation, or select another installed provider to enable it.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterSQLFederationRule ::=
  'ALTER' 'SQL_FEDERATION' 'RULE' sqlFederationRuleDefinition

sqlFederationRuleDefinition ::=
  '(' allQueryUseSQLFederation? (','? executionPlanCache)? (','? providerType)? ')'

allQueryUseSQLFederation ::=
  'ALL_QUERY_USE_SQL_FEDERATION' '=' boolean_

executionPlanCache ::=
  'EXECUTION_PLAN_CACHE' '(' cacheOption ')'

providerType ::=
  'PROVIDER_TYPE' '=' (identifier | string)

cacheOption ::=
  ('INITIAL_CAPACITY' '=' initialCapacity)? (',' 'MAXIMUM_SIZE' '=' maximumSize)?

initialCapacity ::=
  int

maximumSize ::=
  int

boolean_ ::=
  TRUE | FALSE
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Alter SQL Federation rule

```sql
ALTER SQL_FEDERATION RULE (
  ALL_QUERY_USE_SQL_FEDERATION=TRUE,
  EXECUTION_PLAN_CACHE(INITIAL_CAPACITY=1024, MAXIMUM_SIZE=65535),
  PROVIDER_TYPE=CALCITE
);
```

### Reserved word

`ALTER`、`SQL_FEDERATION`、`RULE`、`ALL_QUERY_USE_SQL_FEDERATION`、`EXECUTION_PLAN_CACHE`、`PROVIDER_TYPE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`

### Related links

- [Related links](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
