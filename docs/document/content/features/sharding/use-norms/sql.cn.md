+++
title = "SQL"
weight = 1
+++

由于 SQL 语法灵活复杂，面向分布式的数据库和单机数据库的查询场景又不完全相同，难免有和单机数据库不兼容的 SQL 出现。

本文详细罗列出已明确可支持的 SQL 种类以及已明确不支持的 SQL 种类，供使用者参考。

其中有未涉及到的 SQL 欢迎补充，未支持的 SQL 也尽量会在未来的版本中支持。

## 解析引擎

解析引擎负责将 SQL 字符串解析为抽象语法树。
目前支持 MySQL, PostgreSQL, SQLServer, Oracle, openGauss 以及符合 SQL92 规范的 SQL。
由于 SQL 语法的复杂性，目前仍然存在少量解析引擎不支持的 SQL。

清单如下：

| MySQL                                                        |
| ------------------------------------------------------------ |
| FLUSH PRIVILEGES                                             |
| CLONE LOCAL DATA DIRECTORY = 'clone_dir'                     |
| INSTALL COMPONENT 'file://component1', 'file://component2'   |
| UNINSTALL COMPONENT 'file://component1', 'file://component2' |
| SHOW CREATE USER user                                        |
| REPAIR TABLE t_order                                         |
| OPTIMIZE TABLE t_order                                       |
| CHECKSUM TABLE t_order                                       |
| CHECK TABLE t_order                                          |
| SET RESOURCE GROUP group_name                                |
| DROP RESOURCE GROUP group_name                               |
| CREATE RESOURCE GROUP group_name TYPE = SYSTEM               |
| ALTER RESOURCE GROUP rg1 VCPU = 0-63                         |

## 数据分片

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

TODO

### 不支持

以下 CASE WHEN 语句不支持：

* `CASE WHEN` 中包含子查询
* `CASE WHEN` 中使用逻辑表名（请使用表别名）

以下 UNION 和 UNION ALL 语句不支持：

* 包含分片表和广播表

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
| ------------------------------------------------------------------------ | -------------------------------- |
| SELECT * FROM (SELECT * FROM tbl_name) o                                 |                                  |
| SELECT * FROM (SELECT * FROM tbl_name) o WHERE o.col1 = ?                |                                  |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o                  |                                  |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o WHERE o.col1 = ? | 子查询和外层查询不在同一分片后的数据节点 |
| SELECT (SELECT MAX(col1) FROM tbl_name) a, col2 from tbl_name            |                                  |
| SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name                       |                                  |
| SELECT col1, SUM(col2) FROM tbl_name GROUP BY col1 HAVING SUM(col2) > ?  |                                  |

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
