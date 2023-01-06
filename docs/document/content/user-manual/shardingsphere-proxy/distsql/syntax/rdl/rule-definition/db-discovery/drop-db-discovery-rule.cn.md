+++
title = "DROP DB_DISCOVERY RULE"
weight = 4
+++

## 描述

`DROP DB_DISCOVERY RULE` 语法用于为指定逻辑库删除数据库发现规则

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDatabaseDiscoveryRule ::=
  'DROP' 'DB_DISCOVERY' 'RULE' ifExists? dbDiscoveryRuleName (',' dbDiscoveryRuleName)* ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

dbDiscoveryRuleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}


### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`；
- `ifExists` 子句用于避免 `Database discovery rule not exists` 错误。

### 示例

- 为指定数据库删除多个数据库发现规则
 
```sql
DROP DB_DISCOVERY RULE group_0, group_1 FROM discovery_db;
```

- 为当前数据库删除单个数据库发现规则

```sql
DROP DB_DISCOVERY RULE group_0;
```

- 使用 `ifExists` 子句删除数据库发现规则

```sql
DROP DB_DISCOVERY RULE IF EXISTS group_0;
```

### 保留字

`DROP`、`DB_DISCOVERY`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)