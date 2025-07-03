+++
title = "ALTER SQL_FEDERATION RULE"
weight = 9
+++

### Description

The `ALTER SQL_FEDERATION RULE` syntax is used to modify the federated query configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterSQLFederationRule ::=
  'ALTER' 'SQL_FEDERATION' 'RULE' sqlFederationRuleDefinition

sqlFederationRuleDefinition ::=
  '(' sqlFederationEnabled? (','? allQueryUseSQLFederation)? (','? executionPlanCache)? ')'

sqlFederationEnabled ::=
  'SQL_FEDERATION_ENABLED' '=' boolean_

allQueryUseSQLFederation ::=
  'ALL_QUERY_USE_SQL_FEDERATION' '=' boolean_

executionPlanCache ::=
  'EXECUTION_PLAN_CACHE' '(' cacheOption ')'

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
  SQL_FEDERATION_ENABLED=TRUE,
  ALL_QUERY_USE_SQL_FEDERATION=TRUE,
  EXECUTION_PLAN_CACHE(INITIAL_CAPACITY=1024, MAXIMUM_SIZE=65535)
);
```

### Reserved word

`ALTER`、`SQL_FEDERATION`、`RULE`、`SQL_FEDERATION_ENABLED`、`ALL_QUERY_USE_SQL_FEDERATION`、`EXECUTION_PLAN_CACHE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`

### Related links

- [Related links](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
