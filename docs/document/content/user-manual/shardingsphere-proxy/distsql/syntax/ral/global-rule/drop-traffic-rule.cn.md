+++
title = "DROP TRAFFIC RULE"
weight = 10
+++

### 描述

`DROP TRAFFIC RULE` 语法用于删除指定的双路由规则
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropTrafficRule ::=
  'DROP' 'TRAFFIC' 'RULE' ruleName (',' ruleName)?

ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 删除指定双路由规则

```sql
DROP TRAFFIC RULE sql_match_traffic;
```

- 删除多个双路由规则

```sql
DROP TRAFFIC RULE sql_match_traffic, sql_hint_traffic;
```

### 保留字

`DROP`、`TRAFFIC`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)