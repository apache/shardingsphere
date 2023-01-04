+++
title = "DROP DB_DISCOVERY TYPE"
weight = 6
+++

## 描述

`DROP DB_DISCOVERY TYPE` 语法用于为指定逻辑库删除数据库发现类型

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDatabaseDiscoveryType ::=
  'DROP' 'DB_DISCOVERY' 'TYPE'  dbDiscoveryTypeName (',' dbDiscoveryTypeName)* ('FROM' databaseName)?

dbDiscoveryTypeName ::=
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

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`

- `dbDiscoveryTypeName` 需要通过 [SHOW DB_DISCOVERY TYPE](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/db-discovery/show-db-discovery-type/) 语法查询获得

### 示例

- 为指定数据库删除多个数据库发现类型
 
```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr, group_1_mysql_mgr FROM discovery_db;
```

- 为当前数据库删除单个数据库发现类型

```sql
DROP DB_DISCOVERY TYPE group_0_mysql_mgr, group_1_mysql_mgr;
```

### 保留字

`DROP`、`DB_DISCOVERY`、`TYPE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)