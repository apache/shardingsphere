+++
title = "ALTER TRAFFIC RULE"
weight = 8
+++

### Description

The `ALTER TRAFFIC RULE` syntax is used to alter dual routing rule.

### Syntax

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

### Supplement

- `TRAFFIC_ALGORITHM` support `SQL_MATCH` and `SQL_HINT` two types;

- `LOAD_BALANCER` support `RANDOM` and `ROUND_ROBIN` two types.

### Example

- Alter dual routing rule

```sql
 TRAFFIC RULE sql_match_traffic ( 
  LABELS (OLTP),
  TRAFFIC_ALGORITHM(TYPE(NAME="SQL_MATCH",PROPERTIES("sql" = "SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5;"))),
  LOAD_BALANCER(TYPE(NAME="RANDOM")));
```

### Reserved word

`ALTER`, `TRAFFIC`, `RULE`, `LABELS`, `TYPE`, `NAME`, `PROPERTIES`, `TRAFFIC_ALGORITHM`, `LOAD_BALANCER`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
