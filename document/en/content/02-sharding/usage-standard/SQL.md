+++
toc = true
title = "SQL"
weight = 2
prev = "/02-sharding/usage-standard/JDBC/"
next = "/02-sharding/core-functions/"
+++

## The global supported items

### DQL

#### Basic SELECT

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

## The global unsupported items

### Support some kinds of subqueries
Sharding-JDBC also supports other subqueries similar with paging subquery( [The Pagination](/02-guide/subquery/)). No matter how many layers in a subquery, Sharding-JDBC can always find the first subquery that contains the table data, once the sub-subquery containing table data is found in the lower nest, Sharding-JDBC will throw an exception.

For example, the following subquery is ok:

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o)
```

But this subquery will cause exceptions：

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o WHERE o.id IN (SELECT id FROM t_order WHERE status = ?))
```

Simply speaking, none-functional subqueries can be supported in most cases, such as pagination or statistics, etc, but subqueries for business is not supported currently.

In addition, subqueries containing aggregate functions are not supported currently due to merge constraints.

### Does not support SQL that contains redundant parentheses

### Does not support OR

### Does not support CASE WHEN

#### The examples

### DQL

| SQL                                      | Unconditional | Condition |
| ---------------------------------------- | ------------- | --------- |
| SELECT * FROM tbl_name                   | Y             |           |
| SELECT * FROM tbl_name WHERE col1 = val1 ORDER BY col2 DESC LIMIT limit | Y             |           |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = val1 | Y             |           |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = val2 GROUP BY col1 ORDER BY col3 DESC LIMIT offset, limit | Y             |           |

### DML

| SQL                                      | Unconditional | Condition                                |
| ---------------------------------------- | ------------- | ---------------------------------------- |
| INSERT INTO tbl_name (col1, col2,...) VALUES (val1, val2,....) | N             | Add Sharding columns into insert columns. |
| INSERT INTO tbl_name VALUES (val1, val2,....) | N             | Inject Sharding columns by Hint.         |
| UPDATE tbl_name SET col1 = val1 WHERE col2 = val2 | Y             |                                          |
| DELETE FROM tbl_name WHERE col1 = val1   | Y             |                                          |

### DDL

| SQL                                      | Unconditional | Condition                           |
| ---------------------------------------- | ------------- | ----------------------------------- |
| CREATE TABLE tbl_name (col1 int,...)     | Y             |                                     |
| ALTER TABLE tbl_name ADD col1 varchar(10) | Y             |                                     |
| DROP TABLE tbl_name                      | Y             |                                     |
| TRUNCATE TABLE tbl_name                  | Y             |                                     |
| CREATE INDEX idx_name ON tbl_name        | Y             |                                     |
| DROP INDEX idx_name ON tbl_name          | Y             |                                     |
| DROP INDEX idx_name                      | Y             | Configure logic-index in tableRule. |

## The unsupported SQL

| SQL                                      |
| ---------------------------------------- |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (val1, val2,....), (val3, val4,....) |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = val3 |
| INSERT INTO tbl_name SET col1 = val1     |
| SELECT DISTINCT * FROM tbl_name WHERE column1 = value1 |
| SELECT * FROM tbl_name WHERE column1 = value1 OR column1 = value2 |
| SELECT COUNT(col1) as count_alias FROM tbl_name GROUP BY col1 HAVING count_alias > val1 |
| SELECT * FROM tbl_name1 UNION SELECT * FROM tbl_name2 |
| SELECT * FROM tbl_name1 UNION ALL SELECT * FROM tbl_name2 |
| SELECT * FROM tbl_name1 WHERE (val1=?) AND (val1=?) |
