+++
title = "DROP DEFAULT SINGLE TABLE RULE"
weight = 2
+++

## 描述

`DROP DEFAULT SINGLE TABLE RULE` 语法用于删除默认的单表规则

### 语法定义

```sql
DropDefaultSingleTableRule ::=
  'DROP' 'DEFAULT' 'SINGLE' 'TABLE' 'RULE' ifExists?

ifExists ::=
  'IF' 'EXISTS'
```

### 示例

#### 删除默认单表规则

```sql
DROP DEFAULT SINGLE TABLE RULE;
```

### 保留字

`DROP`、`SHARDING`、`SINGLE`、`TABLE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)