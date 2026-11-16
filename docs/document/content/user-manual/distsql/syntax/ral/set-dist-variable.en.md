+++
title = "SET DIST VARIABLE"
weight = 7
aliases = ["/user-manual/shardingsphere-proxy/distsql/syntax/ral/set-dist-variable/"]
+++

### Description

The `SET DIST VARIABLE` syntax is used to set system variables.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
SetDistVariable ::=
  'SET' 'DIST' 'VARIABLE' variableName '=' variableValue

variableName ::=
  identifier

variableValue ::=
  literal
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `variableName` is a ShardingSphere property; see [common properties](/en/user-manual/common-config/props/) and [Proxy properties](/en/user-manual/shardingsphere-proxy/yaml-config/props/).
  Use underscores instead of hyphens in DistSQL variable names.

- `agent_plugins_enabled` is used to set the `agent` plugin status. Its default value is `TRUE`.

### Example

- Set a ShardingSphere property

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

- [Reserved word](/en/user-manual/distsql/syntax/reserved-word/)
