+++
title = "SET DIST VARIABLE"
weight = 4
+++

### 描述

`SET DIST VARIABLE` 语法用于设置系统变量

### 语法

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

### 补充说明

- `proxy_property_name` 为 `PROXY` 的[属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/)，需使用下划线命名

- `transaction_type` 为当前连接的事务类型, 支持 `LOCAL`、`XA`、`BASE`

- `agent_plugins_enable` 为 `agent` 插件的启用状态，默认值 `FALSE`

### 示例

- 设置 `Proxy` 属性配置

```sql
SET DIST VARIABLE sql_show = true;
```

- 设置当前连接的事务类型

```sql
SET DIST VARIABLE transaction_type = “XA”;
```

- 设置 `agent` 插件启用状态

```sql
SET DIST VARIABLE agent_plugins_enabled = TRUE;
```

### 保留字

`SET`、`DIST`、`VARIABLE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)