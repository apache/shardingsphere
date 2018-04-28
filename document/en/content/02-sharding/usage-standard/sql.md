+++
toc = true
title = "SQL"
weight = 1
+++

Because of the flexibility and complexity of SQL syntax and the different handling for SQL queries for distributed databases and single database, not all of the SQLs can be used in Sharding-Sphere.

This section lists the supported SQL syntax and the unsupported SQL syntax for user to look up. In the future, more and more SQL syntax will be supported in Sharding-Sphere.

## Supported SQL

Fully support DQL, DML and DDL. Support pagination, ORDER BY, GROUP BY, aggregation and JOIN(cannot support cross database). Example for DQL: 

- Main SELECT

```sql
SELECT select_expr [, select_expr ...] FROM table_reference [, table_reference ...]
[WHERE where_condition] 
[GROUP BY {col_name | position} [ASC | DESC]] 
[ORDER BY {col_name | position} [ASC | DESC], ...] 
[LIMIT {[offset,] row_count | row_count OFFSET offset}]
```

- select_expr

```sql
* | 
COLUMN_NAME [AS] [alias] | 
(MAX | MIN | SUM | AVG)(COLUMN_NAME | alias) [AS] [alias] | 
COUNT(* | COLUMN_NAME | alias) [AS] [alias]
```

- table_reference

```sql
tbl_name [AS] alias] [index_hint_list] | 
table_reference ([INNER] | {LEFT|RIGHT} [OUTER]) JOIN table_factor [JOIN ON conditional_expr | USING (column_list)] | 
```

## Unsupported SQL

Cannot support brackets redundancy, CASE WHEN、DISTINCT、HAVING、UNION (ALL), can support subquery limited.

Sharding-Sphere supports subquery similar with paging subquery ([Pagination](/02-sharding/usage-standard/pagination)). No matter how many layers in a subquery, Sharding-Sphere can always find the first subquery that contains the table data, once the sub-subquery containing table data is found in the lower nest, Sharding-Sphere will throw an exception.

For example, the following subquery is ok: 

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o)
```

But this subquery will cause exceptions: 

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o WHERE o.id IN (SELECT id FROM t_order WHERE status = ?))
```

Simply speaking, none-functional subquery can be supported in most cases, such as pagination or statistics, etc, but subquery for business is not supported currently.

In addition, subquery containing aggregate functions are not supported currently due to merge constraints.

## Example

### Supported SQL

| SQL                                                                                         | Required condition                  |
| ------------------------------------------------------------------------------------------- | ----------------------------------- |
| SELECT * FROM tbl_name                                                                      |                                     |
| SELECT * FROM tbl_name WHERE (col1 = ? or col2 = ?) and col3 = ?                            |                                     |
| SELECT * FROM tbl_name WHERE col1 = ? ORDER BY col2 DESC LIMIT ?                            |                                     |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = ?    |                                     |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = ? GROUP BY col1 ORDER BY col3 DESC LIMIT ?, ? |                                     |
| INSERT INTO tbl_name (col1, col2,...) VALUES (?, ?, ....)                                   |                                     |
| INSERT INTO tbl_name VALUES (?, ?,....)                                                     |                                     |
| UPDATE tbl_name SET col1 = ? WHERE col2 = ?                                                 |                                     |
| DELETE FROM tbl_name WHERE col1 = ?                                                         |                                     |
| CREATE TABLE tbl_name (col1 int, ...)                                                       |                                     |
| ALTER TABLE tbl_name ADD col1 varchar(10)                                                   |                                     |
| DROP TABLE tbl_name                                                                         |                                     |
| TRUNCATE TABLE tbl_name                                                                     |                                     |
| CREATE INDEX idx_name ON tbl_name                                                           |                                     |
| DROP INDEX idx_name ON tbl_name                                                             |                                     |
| DROP INDEX idx_name                                                                         |  Configure logic-index in TableRule |

### Unsupported SQL

| SQL                                                                                         | Unsupported reason  |
| ------------------------------------------------------------------------------------------- |-------------------- |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (?, ?,....), (?, ?,....)                      | Multiple INSERT     |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ?  | INSERT .. SELECT    |
| INSERT INTO tbl_name SET col1 = ?                                                           | INSERT .. SET       |
| SELECT DISTINCT * FROM tbl_name WHERE column1 = ?                                           | DISTINCT            |
| SELECT COUNT(col1) as count_alias FROM tbl_name GROUP BY col1 HAVING count_alias > ?        | HAVING              |
| SELECT * FROM tbl_name1 UNION SELECT * FROM tbl_name2                                       | UNION               |
| SELECT * FROM tbl_name1 UNION ALL SELECT * FROM tbl_name2                                   | UNION ALL           |
| SELECT * FROM tbl_name1 WHERE (val1=?) AND (val1=?)                                         | brackets redundancy |
