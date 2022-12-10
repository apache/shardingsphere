+++
title = "CREATE BROADCAST TABLE RULE"
weight = 16
+++

## 描述

`CREATE BROADCAST TABLE RULE` 语法用于为需要广播的表（广播表）创建广播规则

### 语法定义

```sql
CreateBroadcastTableRule ::=
  'CREATE' 'BROADCAST' 'TABLE' 'RULE'  tableName (',' tableName)* 

tableName ::=
  identifier
```

### 补充说明

- `tableName` 可使用已经存在的表或者将要创建的表；
- 只能存在一个广播规则，但可包含多个广播表，因此无法重复执行 `CREATE BROADCAST TABLE RULE`。

### 示例

#### 创建广播规则

```sql
-- 将 t_province， t_city 添加到广播规则中 
CREATE BROADCAST TABLE RULE t_province, t_city;
```

### 保留字

`CREATE`、`BROADCAST`、`TABLE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)