+++
title = "CREATE DB_DISCOVERY RULE"
weight = 2
+++

## 描述

`CREATE DB_DISCOVERY RULE` 语法用于创建数据库发现规则

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateDatabaseDiscoveryRule ::=
  'CREATE' 'DB_DISCOVERY' 'RULE' ifNotExists? databaseDiscoveryDefinition (',' databaseDiscoveryDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

databaseDiscoveryDefinition ::=
  ruleName '(' 'STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')' ',' 'TYPE' '(' 'NAME' '=' typeName (',' propertiesDefinition)? ')' ',' 'HEARTBEAT' '(' propertiesDefinition ')' ')' 

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MySQL.MGR`；
- 重复的 `ruleName` 将无法被创建；
- `ifNotExists` 子句用于避免出现 `Duplicate db_discovery rule` 错误。

### 示例

- 创建数据库发现规则

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_0 (
    STORAGE_UNITS(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

- 使用 `ifNotExists` 子句创建数据库发现规则

```sql
CREATE DB_DISCOVERY RULE IF NOT EXISTS db_discovery_group_0 (
    STORAGE_UNITS(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

### 保留字

`CREATE`、`DB_DISCOVERY`、`RULE`、`STORAGE_UNITS`、`TYPE`、`NAME`、`PROPERTIES`、`HEARTBEAT`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)