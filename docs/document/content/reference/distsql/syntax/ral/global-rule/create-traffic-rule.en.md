+++
title = "CREATE TRAFFIC RULE"
weight = 7
+++

### Description

The `CREATE TRAFFIC RULE` syntax is used to create dual routing rule.

### Syntax

```sql
CreateTrafficRule ::=
  'CREATE' 'TRAFFIC' 'RULE'  '(' 'LABELS' '(' lableName ')' ',' trafficAlgorithmDefination ',' loadBalancerDefination ')'

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

### Supplement

- `TRAFFIC_ALGORITHM` support `SQL_MATCH` and `SQL_HINT` two types;

- `LOAD_BALANCER` support `RANDOM` and `ROUND_ROBIN` two types.

### Example

- Create dual routing rule

```sql
CREATE TRAFFIC RULE sql_match_traffic ( 
  LABELS (OLTP),
  TRAFFIC_ALGORITHM(TYPE(NAME="SQL_MATCH",PROPERTIES("sql" = "SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5;"))),
  LOAD_BALANCER(TYPE(NAME="RANDOM")));
```

### Reserved word

`CREATE`, `TRAFFIC`, `RULE`, `LABELS`, `TYPE`, `NAME`, `PROPERTIES`, `TRAFFIC_ALGORITHM`, `LOAD_BALANCER`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
