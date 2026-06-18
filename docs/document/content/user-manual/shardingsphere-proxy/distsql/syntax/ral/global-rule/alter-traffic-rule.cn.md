+++
title = "ALTER TRAFFIC RULE"
weight = 7
+++

## 描述

`ALTER TRAFFIC RULE` 语法用于修改双路由规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterTrafficRule ::=
  'ALTER' 'TRAFFIC' 'RULE'  '(' 'LABELS' '(' lableName ')' ',' trafficAlgorithmDefinition ',' loadBalancerDefinition ')'

lableName ::=
  identifier

trafficAlgorithmDefinition ::=
  'TRAFFIC_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' trafficAlgorithmTypeName (',' propertiesDefinition)? ')' ')'

loadBalancerDefinition ::=
  'LOAD_BALANCER' '(' 'TYPE' '(' 'NAME' '=' loadBalancerName (',' propertiesDefinition)? ')' ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

trafficAlgorithmTypeName ::=
  string

loadBalancerTypeName ::=
  string

key ::= 
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `TRAFFIC_ALGORITHM` 支持 `SQL_MATCH` 与 `SQL_HINT` 两种类型

- `LOAD_BALANCER` 支持 `RANDOM` 与 `ROUND_ROBIN` 两种类型

### 示例

- 修改双路由规则

```sql
ALTER TRAFFIC RULE sql_match_traffic ( 
  LABELS (OLTP),
  TRAFFIC_ALGORITHM(TYPE(NAME="SQL_MATCH",PROPERTIES("sql" = "SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5;"))),
  LOAD_BALANCER(TYPE(NAME="RANDOM")));
```

### 保留字

`ALTER`、`TRAFFIC`、`RULE`、`LABELS`、`TYPE`、`NAME`、`PROPERTIES`、`TRAFFIC_ALGORITHM`、`LOAD_BALANCER`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
