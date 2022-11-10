+++
title = "SET DIST VARIABLE"
weight = 4
+++

### Description

The `SET DIST VARIABLE` syntax is used to set system variables.
### Syntax

```sql
SetDistVariable ::=
  'SET' 'DIST' 'VARIABLE' ( proxyPropertyName '=' proxyPropertyValue | 'transaction_type' '=' transactionType | 'agent_plugins_enable' '=' agentPluginsEnable )

proxyPropertyName ::= 
  identifier

proxyPropertyValue ::=
  literal

transactionType ::=
  string

agentPluginsEnable ::=
  boolean
```

### Supplement

-  `proxy_property_name` is one of [properties configuration](/en/user-manual/shardingsphere-proxy/yaml-config/props/) of `PROXY`, name is split by underscore

- `transaction_type` is use to set transaction types for current connection, supports `LOCAL`, `XA`, `BASE`

- `agent_plugins_enable` is use to set the `agent` plugins enable status, the default value is `FALSE`

### Example

- Set property configuration of `Proxy`

```sql
SET DIST VARIABLE sql_show = true;
```

- Set transaction type for current connection

```sql
SET DIST VARIABLE transaction_type = “XA”;
```

- Set `agent` plugin enable status

```sql
SET DIST VARIABLE agent_plugins_enabled = TRUE;
```

### Reserved word

`SET`, `DIST`, `VARIABLE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
