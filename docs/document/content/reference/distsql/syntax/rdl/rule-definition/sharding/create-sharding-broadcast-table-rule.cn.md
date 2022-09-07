+++
title = "CREATE SHARDING BROADCAST TABLE RULE"
weight = 7
+++

## 描述

`CREATE SHARDING BROADCAST TABLE RULE` 语法用于为需要广播的表（广播表）创建广播规则

### 语法定义

```sql
CreateBroadcastTableRule ::=
  'CREATE' 'SHARDING' 'BROADCAST' 'TABLE' 'RULES' '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- `tableName` 可使用已经存在的表或者将要创建的表；
- 只能存在一个广播规则，但可包含多个广播表，因此无法重复执行 `CREATE SHARDING BROADCAST TABLE RULE`。
 当广播规则已经存在但还需要添加广播表时，需要使用 `ALTER BROADCAST TABLE RULE` 来修改广播规则。

### 示例

#### 创建广播规则

```sql
-- 将 t_province， t_city 添加到广播规则中 
CREATE SHARDING BROADCAST TABLE RULES (t_province, t_city);
```

### 保留字

`CREATE`、`SHARDING`、`BROADCAST`、`TABLE`、`RULES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)