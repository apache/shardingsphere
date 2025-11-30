+++
title = "ALTER SQL_FEDERATION RULE"
weight = 9
+++

### 描述

`ALTER SQL_FEDERATION RULE` 语法用于修改联邦查询配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 修改联邦查询配置

```sql
ALTER SQL_FEDERATION RULE (
  SQL_FEDERATION_ENABLED=TRUE,
  ALL_QUERY_USE_SQL_FEDERATION=TRUE,
  EXECUTION_PLAN_CACHE(INITIAL_CAPACITY=1024, MAXIMUM_SIZE=65535)
);
```

### 保留字

`ALTER`、`SQL_FEDERATION`、`RULE`、`SQL_FEDERATION_ENABLED`、`ALL_QUERY_USE_SQL_FEDERATION`、`EXECUTION_PLAN_CACHE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
