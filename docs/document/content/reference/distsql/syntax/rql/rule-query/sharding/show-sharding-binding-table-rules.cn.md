+++
title = "SHOW SHARDING BINDING TABLE RULES"
weight = 14
+++

### 描述

`SHOW SHARDING BINDING TABLE RULES` 语法用于查询指定逻辑库中具有绑定关系的分片表

### 语法

```
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'BINDING' 'TABLE' 'RULES'('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                       | 说明           |
| -------------------------| --------------|
| sharding_binding_tables  | 绑定表名称      |

### 示例

- 查询指定逻辑库中具有绑定关系的分片表

```sql
SHOW SHARDING BINDING TABLE RULES FROM test1;
```

```sql
mysql> SHOW SHARDING BINDING TABLE RULES FROM test1;
+-------------------------+
| sharding_binding_tables |
+-------------------------+
| t_order,t_order_item    |
+-------------------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中具有绑定关系的分片表

```sql
SHOW SHARDING BINDING TABLE RULES;
```

```sql
mysql> SHOW SHARDING BINDING TABLE RULES;
+-------------------------+
| sharding_binding_tables |
+-------------------------+
| t_order,t_order_item    |
+-------------------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`BINDING`、`TABLE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

