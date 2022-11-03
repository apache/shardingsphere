+++
title = "SHOW AUTHORITY RULE"
weight = 2
+++

### 描述

`SHOW AUTHORITY RULE` 语法用于查询权限规则配置
### 语法

```sql
ShowAuthorityRule ::=
  'SHOW' 'AUTHORITY' 'RULE'
```

### 返回值说明

| 列          | 说明          |
|-------------|--------------|
| users       | 用户          |
| provider    | 权限提供者类型 |
| props       | 权限参数      |

### 示例

- 查询权限规则配置

```sql
SHOW AUTHORITY RULE;
```

```sql
mysql> SHOW AUTHORITY RULE;
+--------------------+---------------+-------+
| users              | provider      | props |
+--------------------+---------------+-------+
| root@%; sharding@% | ALL_PERMITTED |       |
+--------------------+---------------+-------+
1 row in set (0.07 sec)
```

### 保留字

`SHOW`、`AUTHORITY`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)