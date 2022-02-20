+++
title = "SQL"
weight = 1
+++

## SQL 支持程度

兼容全部常用的**路由至单数据节点**的 SQL；
**路由至多数据节点**的 SQL 由于场景复杂，分为稳定支持、实验性支持和不支持这三种情况。

### 稳定支持

全面支持 DML、DDL、DCL、TCL 和常用 DAL。
支持分页、去重、排序、分组、聚合、表关联等复杂查询。

#### 常规查询

- SELECT 主语句

```sql
SELECT select_expr [, select_expr ...] FROM table_reference [, table_reference ...]
[WHERE predicates]
[GROUP BY {col_name | position} [ASC | DESC], ...]
[ORDER BY {col_name | position} [ASC | DESC], ...]
[LIMIT {[offset,] row_count | row_count OFFSET offset}]
```

- select_expr

```sql
* | 
[DISTINCT] COLUMN_NAME [AS] [alias] | 
(MAX | MIN | SUM | AVG)(COLUMN_NAME | alias) [AS] [alias] | 
COUNT(* | COLUMN_NAME | alias) [AS] [alias]
```

- table_reference

```sql
tbl_name [AS] alias] [index_hint_list]
| table_reference ([INNER] | {LEFT|RIGHT} [OUTER]) JOIN table_factor [JOIN ON conditional_expr | USING (column_list)]
```

#### 子查询

子查询和外层查询同时指定分片键，且分片键的值保持一致时，由内核提供稳定支持。

例如：

```sql
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;
```

用于[分页](/cn/features/sharding/use-norms/pagination)的子查询，由内核提供稳定支持。

例如：

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;
```

#### 运算表达式中包含分片键

当分片键处于运算表达式中时，无法通过 SQL `字面`提取用于分片的值，将导致全路由。

例如，假设 `create_time` 为分片键：

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

### 实验性支持

实验性支持特指使用 Federation 执行引擎提供支持。
该引擎处于快速开发中，用户虽基本可用，但仍需大量优化，是实验性产品。

#### 子查询

子查询和外层查询未同时指定分片键，或分片键的值不一致时，由 Federation 执行引擎提供支持。

例如：

```sql
SELECT * FROM (SELECT * FROM t_order) o;

SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;
```

#### 跨库关联查询

当关联查询中的多个表分布在不同的数据库实例上时，由 Federation 执行引擎提供支持。
假设 `t_order` 和 `t_order_item` 是多数据节点的分片表，并且未配置绑定表规则，`t_user` 和 `t_user_role` 是分布在不同的数据库实例上的单表，那么 Federation 执行引擎能够支持如下常用的关联查询：

```sql
SELECT * FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id WHERE o.order_id = 1;

SELECT * FROM t_order o INNER JOIN t_user u ON o.user_id = u.user_id WHERE o.user_id = 1;

SELECT * FROM t_order o LEFT JOIN t_user_role r ON o.user_id = r.user_id WHERE o.user_id = 1;

SELECT * FROM t_order_item i LEFT JOIN t_user u ON i.user_id = u.user_id WHERE i.user_id = 1;

SELECT * FROM t_order_item i RIGHT JOIN t_user_role r ON i.user_id = r.user_id WHERE i.user_id = 1;

SELECT * FROM t_user u RIGHT JOIN t_user_role r ON u.user_id = r.user_id WHERE u.user_id = 1;
```

### 不支持

以下 CASE WHEN 语句不支持：

* `CASE WHEN` 中包含子查询
* `CASE WHEN` 中使用逻辑表名（请使用表别名）

## SQL 示例

| 稳定支持的 SQL                                                                                | 必要条件                          |
| ------------------------------------------------------------------------------------------- | -------------------------------- |
| SELECT * FROM tbl_name                                                                      |                                  |
| SELECT * FROM tbl_name WHERE (col1 = ? or col2 = ?) and col3 = ?                            |                                  |
| SELECT * FROM tbl_name WHERE col1 = ? ORDER BY col2 DESC LIMIT ?                            |                                  |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = ?    |                                  |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = ? GROUP BY col1 ORDER BY col3 DESC LIMIT ?, ? |                                  |
| SELECT DISTINCT * FROM tbl_name WHERE col1 = ?                                              |                                  |
| SELECT COUNT(DISTINCT col1), SUM(DISTINCT col1) FROM tbl_name                               |                                  |
| (SELECT * FROM tbl_name)                                                                    |                                  |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o WHERE o.col1 = ?                    | 子查询和外层查询在同一分片后的数据节点  |
| INSERT INTO tbl_name (col1, col2,...) VALUES (?, ?, ....)                                   |                                  |
| INSERT INTO tbl_name VALUES (?, ?,....)                                                     |                                  |
| INSERT INTO tbl_name (col1, col2, ...) VALUES(1 + 2, ?, ...)                                |                                  |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (?, ?, ....), (?, ?, ....)                    |                                  |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ?  | INSERT 表和 SELECT 表相同表或绑定表  |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ? | REPLACE 表和 SELECT 表相同表或绑定表 |
| UPDATE tbl_name SET col1 = ? WHERE col2 = ?                                                 |                                  |
| DELETE FROM tbl_name WHERE col1 = ?                                                         |                                  |
| CREATE TABLE tbl_name (col1 int, ...)                                                       |                                  |
| ALTER TABLE tbl_name ADD col1 varchar(10)                                                   |                                  |
| DROP TABLE tbl_name                                                                         |                                  |
| TRUNCATE TABLE tbl_name                                                                     |                                  |
| CREATE INDEX idx_name ON tbl_name                                                           |                                  |
| DROP INDEX idx_name ON tbl_name                                                             |                                  |
| DROP INDEX idx_name                                                                         |                                  |

***

| 实验性支持的 SQL                                                           | 必要条件                          |
|--------------------------------------------------------------------------| -------------------------------- |
| SELECT * FROM (SELECT * FROM tbl_name) o                                 |                                  |
| SELECT * FROM (SELECT * FROM tbl_name) o WHERE o.col1 = ?                |                                  |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o                  |                                  |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o WHERE o.col1 = ? | 子查询和外层查询不在同一分片后的数据节点|
| SELECT (SELECT MAX(col1) FROM tbl_name) a, col2 from tbl_name            |                                  |
| SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name                       |                                  |
| SELECT col1, SUM(col2) FROM tbl_name GROUP BY col1 HAVING SUM(col2) > ?  |                                  |
| SELECT col1, col2 FROM tbl_name UNION SELECT col1, col2 FROM tbl_name    |                                  |
| SELECT col1, col2 FROM tbl_name UNION ALL SELECT col1, col2 FROM tbl_name|                                  |

***

| 慢 SQL                                                              | 原因                        |
| ------------------------------------------------------------------- | -------------------------- |
| SELECT * FROM tbl_name WHERE to_date(create_time, 'yyyy-mm-dd') = ? | 分片键在运算表达式中，导致全路由 |

***

| 不支持的 SQL                                                                    | 原因                                 | 解决方案   |
| ----------------------------------------------------------------------------- | ------------------------------------ | --------- |
| INSERT INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?  | SELECT 子句不支持 * 和内置分布式主键生成器 |   无      |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ? | SELECT 子句不支持 * 和内置分布式主键生成器 |   无      |
| SELECT MAX(tbl_name.col1) FROM tbl_name                                       | 查询列是函数表达式时，查询列前不能使用表名   | 使用表别名 |
