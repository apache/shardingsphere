+++
title = "SET DIST VARIABLE"
weight = 7
+++

### Description

The `SET DIST VARIABLE` syntax is used to set system variables.
### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
SetDistVariable ::=
  'SET' 'DIST' 'VARIABLE' (proxyPropertyName '=' proxyPropertyValue | 'agent_plugins_enabled' '=' agentPluginsEnabled)

proxyPropertyName ::= 
  identifier

proxyPropertyValue ::=
  literal

agentPluginsEnabled ::=
  boolean
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `proxy_property_name` is one of [properties configuration](/en/user-manual/shardingsphere-proxy/yaml-config/props/) of `PROXY`, name is split by underscore

- `agent_plugins_enabled` is use to set the `agent` plugins enable status, the default value is `FALSE`

### Example

- Set property configuration of `Proxy`

```sql
SET DIST VARIABLE sql_show = true;
```

- Set `agent` plugin enable status

```sql
SET DIST VARIABLE agent_plugins_enabled = TRUE;
```

### Reserved word

`SET`, `DIST`, `VARIABLE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
