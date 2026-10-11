+++
title = "ALTER SQL_FEDERATION RULE"
weight = 9
+++

### 描述

`ALTER SQL_FEDERATION RULE` 语法用于修改联邦查询配置。
设置 `PROVIDER_TYPE=NONE` 可关闭联邦查询，选择其他已安装的 Provider 可开启联邦查询。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 修改联邦查询配置

```sql
ALTER SQL_FEDERATION RULE (
  ALL_QUERY_USE_SQL_FEDERATION=TRUE,
  EXECUTION_PLAN_CACHE(INITIAL_CAPACITY=1024, MAXIMUM_SIZE=65535),
  PROVIDER_TYPE=CALCITE
);
```

### 保留字

`ALTER`、`SQL_FEDERATION`、`RULE`、`ALL_QUERY_USE_SQL_FEDERATION`、`EXECUTION_PLAN_CACHE`、`PROVIDER_TYPE`、`INITIAL_CAPACITY`、`MAXIMUM_SIZE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
