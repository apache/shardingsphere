+++
title = "SHOW DB_DISCOVERY TYPES"
weight = 3
+++

### 描述

`SHOW DB_DISCOVERY TYPES` 语法用于查询指定逻辑库中的数据库发现类型。

### 语法

```
ShowDatabaseDiscoveryType::=
  'SHOW' 'DB_DISCOVERY' 'TYPES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                       | 说明                    |
| ------------------------ | ---------------------- |
| name                     | 数据库发现类型名称        |
| type                     | 数据库发现类型种类        |
| props                    | 数据库发现类型参数        |


### 示例

- 查询指定逻辑库中的数据库发现类型

```sql
SHOW DB_DISCOVERY TYPES FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY TYPES FROM test1;
+-------------------+-----------+---------------------------------------------------+
| name              | type      | props                                             |
+-------------------+-----------+---------------------------------------------------+
| group_0_MySQL.MGR | MySQL.MGR | {group-name=667edd3c-02ec-11ea-9bb3-080027e39bd2} |
+-------------------+-----------+---------------------------------------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的数据库发现类型

```sql
SHOW DB_DISCOVERY TYPES;
```

```sql
mysql> SHOW DB_DISCOVERY TYPES;
+-------------------+-----------+---------------------------------------------------+
| name              | type      | props                                             |
+-------------------+-----------+---------------------------------------------------+
| group_0_MySQL.MGR | MySQL.MGR | {group-name=667edd3c-02ec-11ea-9bb3-080027e39bd2} |
+-------------------+-----------+---------------------------------------------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`DB_DISCOVERY`、`TYPES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

