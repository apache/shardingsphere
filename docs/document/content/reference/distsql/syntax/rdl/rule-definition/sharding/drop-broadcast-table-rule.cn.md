+++
title = "DROP BROADCAST TABLE RULE"
weight = 17
+++

## 描述

`DROP BROADCAST TABLE RULE` 语法用于为指定的广播表删除广播规则

### 语法定义

```sql
DropBroadcastTableRule ::=
  'DROP' 'BROADCAST' 'TABLE' 'RULE'  tableName (',' tableName)* 

tableName ::=
  identifier
```

### 补充说明

- `tableName` 可使用已经存在的广播规则的表。

### 示例

- 为指定广播表删除广播规则
 
```sql
DROP BROADCAST TABLE RULE t_province, t_city;
```

### 保留字

`DROP`、`BROADCAST`、`TABLE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)