+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 15
+++

## 描述

`DROP SHARDING TABLE REFERENCE RULE` 语法用删除指定的关联规则

### 语法定义

```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  (tableName (',' tableName)* )?

tableName ::=
  identifier
```

### 补充说明

- 未指定 `SHARDING TABLE REFERENCE RULE` 时，默认删除所有关联规则。

### 示例

- 删除指定关联规则
 
```sql
DROP SHARDING TABLE REFERENCE RULE (t_order, t_order_item);
```

- 删除全部关联规则

```sql
DROP SHARDING TABLE REFERENCE RULE;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)