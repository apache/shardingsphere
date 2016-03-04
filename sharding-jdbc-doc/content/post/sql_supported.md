+++
date = "2016-01-08T16:14:21+08:00"
title = "SQL支持详细列表"
weight = 9
+++

# SQL支持详细列表

由于`SQL`语法灵活复杂，分布式数据库和单机数据库的查询场景又不完全相同，难免有和单机数据库不兼容的`SQL`出现。
本文详细罗列出已明确可支持的`SQL`种类以及已明确不支持的`SQL`种类，尽量让使用者避免踩坑。
其中必然有未涉及到的`SQL`欢迎补充，未支持的`SQL`也尽量会在未来的版本中支持。

## 全局不支持项

### 动态表
未配置逻辑表和真实表对应关系的真实表，称为动态表。凡是动态表且未在`SQL`或`Hint`中包含分片键的`SQL`均不支持。
原因是未找到分片键则需全路由，但由于未配置逻辑表和真实表的对应关系，无法全路由。

### 除`DQL`和`DML`以外的语句
`Sharding-JDBC`定位于`CRUD`操作，目前仅针对`DQL`和`DML`语句进行支持。

### 子查询未详细测试

## 支持的SQL

### DQL

#### SELECT主语句

```sql
SELECT select_expr [, select_expr ...] FROM table_reference [, table_reference ...]
[WHERE where_condition] 
[GROUP BY {col_name | position} [ASC | DESC]] 
[ORDER BY {col_name | position} [ASC | DESC], ...] 
[LIMIT {[offset,] row_count | row_count OFFSET offset}]
```

#### select_expr
```sql
* | 
COLUMN_NAME [AS] [alias] | 
(MAX | MIN | SUM | AVG)(COLUMN_NAME | alias) [AS] [alias] | 
COUNT(* | COLUMN_NAME | alias) [AS] [alias]
```

#### table_reference
```sql
tbl_name [AS] alias] [index_hint_list] | 
table_reference ([INNER] | {LEFT|RIGHT} [OUTER]) JOIN table_factor [JOIN ON conditional_expr | USING (column_list)] | 
```

#### 示例

| SQL                                                                                                     | 无条件支持 | 必要条件 |
| ------------------------------------------------------------------------------------------------------- | --------- | ------- |
| SELECT * FROM tbl_name                                                                                  | 是        |         |
| SELECT * FROM tbl_name WHERE col1 = val1 ORDER BY col2 DESC LIMIT limit                                 | 是        |         |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = val1             | 是        |         |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = val2 GROUP BY col1 ORDER BY col3 DESC LIMIT offset, limit | 是        |         |
| SELECT * FROM tbl_name WHERE col1 = val1 OR col2 = val2                                                 | 是        |         |

### DML

#### INSERT

| SQL                                                           | 无条件支持 | 必要条件            |
| ------------------------------------------------------------- | --------- | ------------------ |
| INSERT INTO tbl_name (col1, col2,...) VALUES (val1, val2,....)| `否`      | 插入列需要包含分片键  |
| INSERT INTO tbl_name VALUES (val1, val2,....)                 | `否`      | 通过`Hint`注入分片键 |

#### UPDATE

| SQL                                                           | 无条件支持 | 必要条件             |
| ------------------------------------------------------------- | --------- | ------------------ |
| UPDATE tbl_name SET col1 = val1 WHERE col2 = val2             | 是        |                    |

#### DELETE

| SQL                                                           | 无条件支持 | 必要条件             |
| ------------------------------------------------------------- | --------- | ------------------ |
| DELETE FROM tbl_name WHERE col1 = val1                        | 是        |                    |

## 不支持的SQL

| SQL                                                                                             |
| ----------------------------------------------------------------------------------------------- |
| INSERT INTO tbl_name (col1, col2, ...) VALUES `(val1, val2,....), (val3, val4,....)`            |
| INSERT INTO tbl_name (col1, col2, ...) `SELECT col1, col2, ... FROM tbl_name WHERE col3 = val3` |
| INSERT INTO tbl_name `SET col1 = val1`                                                          |
| SELECT `DISTINCT` * FROM tbl_name WHERE column1 = value1                                        |
| SELECT COUNT(col1) as count_alias FROM tbl_name GROUP BY col1 `HAVING count_alias > val1`       |
| SELECT * FROM tbl_name1 `UNION` SELECT * FROM tbl_name2                                         |
| SELECT * FROM tbl_name1 `UNION ALL` SELECT * FROM tbl_name2                                     |
