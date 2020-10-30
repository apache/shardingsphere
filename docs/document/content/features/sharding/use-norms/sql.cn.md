+++
title = "SQL"
weight = 1
+++

由于SQL语法灵活复杂，分布式数据库和单机数据库的查询场景又不完全相同，难免有和单机数据库不兼容的SQL出现。

本文详细罗列出已明确可支持的SQL种类以及已明确不支持的SQL种类，尽量让使用者避免踩坑。

其中必然有未涉及到的SQL欢迎补充，未支持的SQL也尽量会在未来的版本中支持。

## 支持项

### 路由至单数据节点

- 100%全兼容（目前仅MySQL，其他数据库完善中）。

### 路由至多数据节点

全面支持DML、DDL、DCL、TCL和部分DAL。支持分页、去重、排序、分组、聚合、关联查询（不支持跨库关联）。以下用最为复杂的DML举例：

- SELECT主语句

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

## 不支持项

### 路由至多数据节点

部分支持CASE WHEN
* CASE WHEN 中包含子查询不支持
* CASE WHEN 中使用逻辑表名不支持（请使用表别名）
不支持 HAVING、UNION (ALL)，部分支持子查询。

除了分页子查询的支持之外(详情请参考[分页](/cn/features/sharding/use-norms/pagination))，也支持同等模式的子查询。无论嵌套多少层，ShardingSphere都可以解析至第一个包含数据表的子查询，一旦在下层嵌套中再次找到包含数据表的子查询将直接抛出解析异常。

例如，以下子查询可以支持：

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o)
```

以下子查询不支持：

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o WHERE o.id IN (SELECT id FROM t_order WHERE status = ?))
```

简单来说，通过子查询进行非功能需求，在大部分情况下是可以支持的。比如分页、统计总数等；而通过子查询实现业务查询当前并不能支持。

由于归并的限制，子查询中包含聚合函数目前无法支持。

不支持包含schema的SQL。因为ShardingSphere的理念是像使用一个数据源一样使用多数据源，因此对SQL的访问都是在同一个逻辑schema之上。

### 对分片键进行操作

运算表达式和函数中的分片键会导致全路由。

假设`create_time`为分片键，则无法精确路由形如SQL：

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

由于ShardingSphere只能通过SQL`字面`提取用于分片的值，因此当分片键处于运算表达式或函数中时，ShardingSphere无法提前获取分片键位于数据库中的值，从而无法计算出真正的分片值。

当出现此类分片键处于运算表达式或函数中的SQL时，ShardingSphere将采用全路由的形式获取结果。

## 示例

### 支持的SQL

| SQL                                                                                         | 必要条件                  |
| ------------------------------------------------------------------------------------------- | -------------------------|
| SELECT * FROM tbl_name                                                                      |                          |
| SELECT * FROM tbl_name WHERE (col1 = ? or col2 = ?) and col3 = ?                            |                          |
| SELECT * FROM tbl_name WHERE col1 = ? ORDER BY col2 DESC LIMIT ?                            |                          |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = ?    |                          |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = ? GROUP BY col1 ORDER BY col3 DESC LIMIT ?, ? |                          |
| INSERT INTO tbl_name (col1, col2,...) VALUES (?, ?, ....)                                   |                          |
| INSERT INTO tbl_name VALUES (?, ?,....)                                                     |                          |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (?, ?, ....), (?, ?, ....)                    |                          |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ?  | INSERT表和SELECT表必须为相同表或绑定表 |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ? | REPLACE表和SELECT表必须为相同表或绑定表 |
| UPDATE tbl_name SET col1 = ? WHERE col2 = ?                                                 |                          |
| DELETE FROM tbl_name WHERE col1 = ?                                                         |                          |
| CREATE TABLE tbl_name (col1 int, ...)                                                       |                          |
| ALTER TABLE tbl_name ADD col1 varchar(10)                                                   |                          |
| DROP TABLE tbl_name                                                                         |                          |
| TRUNCATE TABLE tbl_name                                                                     |                          |
| CREATE INDEX idx_name ON tbl_name                                                           |                          |
| DROP INDEX idx_name ON tbl_name                                                             |                          |
| DROP INDEX idx_name                                                                         |                          |
| SELECT DISTINCT * FROM tbl_name WHERE col1 = ?                                              |                          |
| SELECT COUNT(DISTINCT col1) FROM tbl_name                                                   |                          |
| SELECT subquery_alias.col1 FROM (select tbl_name.col1 from tbl_name where tbl_name.col2=?) subquery_alias                                                   |                                         |

### 不支持的SQL

| SQL                                                                                        | 不支持原因                  |
| ------------------------------------------------------------------------------------------ | -------------------------- |
| INSERT INTO tbl_name (col1, col2, ...) VALUES(1+2, ?, ...)                                 | VALUES语句不支持运算表达式   |
| INSERT INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?               | SELECT子句暂不支持使用*号简写及内置的分布式主键生成器 |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?              | SELECT子句暂不支持使用*号简写及内置的分布式主键生成器 |
| SELECT * FROM tbl_name1 UNION SELECT * FROM tbl_name2                                      | UNION                      |
| SELECT * FROM tbl_name1 UNION ALL SELECT * FROM tbl_name2                                  | UNION ALL                  |
| SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name                                         | 详见DISTINCT支持情况详细说明 |
| SELECT * FROM tbl_name WHERE to_date(create_time, 'yyyy-mm-dd') = ?                        | 会导致全路由                |
| (SELECT * FROM tbl_name)                                                                   | 暂不支持加括号的查询                              |
| SELECT MAX(tbl_name.col1) FROM tbl_name                                                    | 查询列是函数表达式时,查询列前不能使用表名;若查询表存在别名,则可使用表的别名|

## DISTINCT支持情况详细说明

### 支持的SQL

| SQL                                                           |
| ------------------------------------------------------------- |
| SELECT DISTINCT * FROM tbl_name WHERE col1 = ?                |
| SELECT DISTINCT col1 FROM tbl_name                            |
| SELECT DISTINCT col1, col2, col3 FROM tbl_name                |
| SELECT DISTINCT col1 FROM tbl_name ORDER BY col1              |
| SELECT DISTINCT col1 FROM tbl_name ORDER BY col2              |
| SELECT DISTINCT(col1) FROM tbl_name                           |
| SELECT AVG(DISTINCT col1) FROM tbl_name                       |
| SELECT SUM(DISTINCT col1) FROM tbl_name                       |
| SELECT COUNT(DISTINCT col1) FROM tbl_name                     |
| SELECT COUNT(DISTINCT col1) FROM tbl_name GROUP BY col1       |
| SELECT COUNT(DISTINCT col1 + col2) FROM tbl_name              |
| SELECT COUNT(DISTINCT col1), SUM(DISTINCT col1) FROM tbl_name |
| SELECT COUNT(DISTINCT col1), col1 FROM tbl_name GROUP BY col1 |
| SELECT col1, COUNT(DISTINCT col1) FROM tbl_name GROUP BY col1 |

### 不支持的SQL

| SQL                                                                                         | 不支持原因                          |
| ------------------------------------------------------------------------------------------- |----------------------------------- |
| SELECT SUM(DISTINCT tbl_name.col1), SUM(tbl_name.col1) FROM tbl_name                        | 查询列是函数表达式时,查询列前不能使用表名;若查询表存在别名,则可使用表的别名 |
