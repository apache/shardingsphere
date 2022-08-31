+++
title = "CREATE DB_DISCOVERY TYPE"
weight = 3
+++

## 描述

`CREATE DB_DISCOVERY TYPE` 语法用于创建数据库发现类型规则

### 语法定义

```sql
CreateDatabaseDiscoveryType ::=
  'CREATE' 'DB_DISCOVERY' 'TYPE' databaseDiscoveryTypeDefinition ( ',' databaseDiscoveryTypeDefinition )*

databaseDiscoveryTypeDefinition ::=
    discoveryTypeName '(' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')' ')'
    
discoveryTypeName ::=
  string

typeName ::=
  string
```

### 补充说明

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MySQL.MGR`。

### 示例

#### 创建 `discoveryType`

```sql
CREATE DB_DISCOVERY TYPE db_discovery_group_1_mgr(
  TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec'))
);
```

### 保留字

`CREATE`、`DB_DISCOVERY`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)