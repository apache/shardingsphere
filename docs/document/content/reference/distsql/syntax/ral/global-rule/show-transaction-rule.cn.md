+++
title = "SHOW TRANSACTION RULE"
weight = 3
+++

### 描述

`SHOW TRANSACTION RULE` 语法用于查询事务规则配置
### 语法

```sql
ShowTransactionRule ::=
  'SHOW' 'TRANSACTION' 'RULE'
```

### 返回值说明

| 列             | 说明          |
|----------------|--------------|
| default_type   | 默认事务类型   |
| provider_type  | 事务提供者类型  |
| props          | 事务参数       |

### 示例

- 查询权限规则配置

```sql
SHOW TRANSACTION RULE;
```

```sql
mysql> SHOW TRANSACTION RULE;
+--------------+---------------+-------+
| default_type | provider_type | props |
+--------------+---------------+-------+
| LOCAL        |               |       |
+--------------+---------------+-------+
1 row in set (0.05 sec)
```

### 保留字

`SHOW`、`TRANSACTION`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)