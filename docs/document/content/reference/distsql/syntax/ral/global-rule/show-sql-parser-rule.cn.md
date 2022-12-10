+++
title = "SHOW SQL_PARSER RULE"
weight = 4
+++

### 描述

`SHOW SQL_PARSER RULE` 语法用于查询解析引擎规则配置

### 语法

```sql
ShowSqlParserRule ::=
  'SHOW' 'SQL_PARSER' 'RULE'
```

### 返回值说明

| 列                        | 说明          |
|---------------------------|-------------|
| sql_comment_parse_enable  | sql注释解析启用状态 |
| parse_tree_cache          | 语法树缓存       |
| sql_statement_cache       | sql语句缓存     |

### 示例

- 查询权限规则配置

```sql
SHOW SQL_PARSER RULE;
```

```sql
mysql> SHOW SQL_PARSER RULE;
+--------------------------+-----------------------------------------+-------------------------------------------+
| sql_comment_parse_enable | parse_tree_cache                        | sql_statement_cache                       |
+--------------------------+-----------------------------------------+-------------------------------------------+
| false                    | initialCapacity: 128, maximumSize: 1024 | initialCapacity: 2000, maximumSize: 65535 |
+--------------------------+-----------------------------------------+-------------------------------------------+
1 row in set (0.05 sec)
```

### 保留字

`SHOW`、`SQL_PARSER`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)