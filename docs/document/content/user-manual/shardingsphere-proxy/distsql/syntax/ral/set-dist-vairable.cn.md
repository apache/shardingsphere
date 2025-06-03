+++
title = "SET DIST VARIABLE"
weight = 7
+++

### 描述

`SET DIST VARIABLE` 语法用于设置系统变量。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `proxy_property_name` 为 `PROXY` 的[属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/)，需使用下划线命名

- `agent_plugins_enabled` 为 `agent` 插件的启用状态，默认值 `FALSE`

### 示例

- 设置 `Proxy` 属性配置

```sql
SET DIST VARIABLE sql_show = true;
```

- 设置 `agent` 插件启用状态

```sql
SET DIST VARIABLE agent_plugins_enabled = TRUE;
```

### 保留字

`SET`、`DIST`、`VARIABLE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)