+++
title = "ALTER TRAFFIC RULE"
weight = 8
+++

## 描述

`ALTER TRAFFIC RULE` 语法用于修改双路由规则

### 语法定义

```sql
AlterTrafficRule ::=
  'ALTER' 'TRAFFIC' 'RULE'  '(' 'LABELS' '(' lableName ')' ',' trafficAlgorithmDefination ',' loadBalancerDefination ')'

lableName ::=
  identifier

trafficAlgorithmDefination ::=
  'TRAFFIC_ALGORITHM' '(' 'TYPE' '(' 'NAME' = trafficAlgorithmTypeName (',' 'PROPERTIES' '(' key '=' value (',' key '=' value)* ')')? ')' ')'

loadBalancerDefination ::=
  'LOAD_BALANCER' '(' 'TYPE' '(' 'NAME' = loadBalancerName (',' 'PROPERTIES' '(' key '=' value (',' key '=' value)* ')')? ')' ')'

trafficAlgorithmTypeName ::=
  string

loadBalancerTypeName ::=
  string

key ::= 
  string

value ::=
  string
```

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

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
