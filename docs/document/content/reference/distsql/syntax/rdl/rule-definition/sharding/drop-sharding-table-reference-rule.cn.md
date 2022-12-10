+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 15
+++

## 描述

`DROP SHARDING TABLE REFERENCE RULE` 语法用删除指定的关联规则

### 语法定义

```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  ruleName (',' ruleName)*

ruleName ::=
  identifier
```

### 示例

- 删除单个关联规则
 
```sql
DROP SHARDING TABLE REFERENCE RULE ref_0;
```

- 删除多个关联规则

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0, ref_1;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)