+++
title = "ALTER TRAFFIC RULE"
weight = 7
+++

### Description

The `ALTER TRAFFIC RULE` syntax is used to alter dual routing rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
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
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

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

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
