+++
title = "SHOW SQL_FEDERATION RULE"
weight = 8
+++

### 描述

`SHOW SQL_FEDERATION RULE` 语法用于查询联邦查询配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowSQLFederationRule ::=
  'SHOW' 'SQL_FEDERATION' 'RULE'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列        | 说明                |
|----------|-------------------|
| sql_federation_enabled    | 是否开启联邦查询          |
| all_query_use_sql_federation | 是否全部查询 SQL 使用联邦查询 |
| execution_plan_cache    | 执行计划缓存            |

### 示例

- 查询联邦查询配置

```sql
SHOW SQL_FEDERATION RULE;
```

```sql
mysql> show sql_federation rule;
+------------------------+------------------------------+-------------------------------------------+
| sql_federation_enabled | all_query_use_sql_federation | execution_plan_cache                      |
+------------------------+------------------------------+-------------------------------------------+
| true                   | false                        | initialCapacity: 2000, maximumSize: 65535 |
+------------------------+------------------------------+-------------------------------------------+
1 row in set (0.31 sec)
```

### 保留字

`SHOW`、`SQL_FEDERATION`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
