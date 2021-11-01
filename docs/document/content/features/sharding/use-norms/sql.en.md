+++
title = "SQL"
weight = 1
+++

## SQL Supporting Status

Compatible with all regular SQL when **routing to single data node**;
**The SQL routing to multiple data nodes** is pretty complex, it divides the scenarios as totally supported, experimental supported and unsupported.

### Totally Supported

Fully support DML, DDL, DCL, TCL and most regular DAL.
Support complex query with pagination, DISTINCT, ORDER BY, GROUP BY, aggregation and table JOIN.

#### Regular Query

- SELECT Clause

```sql
SELECT select_expr [, select_expr ...] FROM table_reference [, table_reference ...]
[WHERE predicates]
[GROUP BY {col_name | position} [ASC | DESC], ...]
[ORDER BY {col_name | position} [ASC | DESC], ...]
[LIMIT {[offset,] row_count | row_count OFFSET offset}]
```

- select_expr

```sql
*
| [DISTINCT] COLUMN_NAME [AS] [alias]
| (MAX | MIN | SUM | AVG)(COLUMN_NAME | alias) [AS] [alias]
| COUNT(* | COLUMN_NAME | alias) [AS] [alias]
```

- table_reference

```sql
tbl_name [AS] alias] [index_hint_list]
| table_reference ([INNER] | {LEFT|RIGHT} [OUTER]) JOIN table_factor [JOIN ON conditional_expr | USING (column_list)]
```

#### Subquery

Stable supported when sharding keys are using in both subquery and outer query, and values of sharding keys are the same.

For example:

```sql
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;
```

Stable supported for subquery with [pagination](https://shardingsphere.apache.org/document/current/cn/features/sharding/usage-standard/pagination).

For example:

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;
```

#### Sharding value in expression

Sharding value in calculated expressions will lead to full routing.

For example, if `create_time` is sharding value:

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

### Experimental Supported

Experimental support specifically refers to use of `Federation execution engine`.
The engine still in rapid development, basically available to users, but it still needs lots of optimization. 
It is an experimental product.

#### Subquery

Experimental supported when sharding keys are not using for both subquery and outer query, or values of sharding keys are not the same.

For example:

```sql
SELECT * FROM (SELECT * FROM t_order) o;

SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;
```

#### Join with cross databases

When tables in a join query are distributed on different database instances, sql statement will be supported by `Federation execution engine`.
Assuming that `t_order` and `t_order_item` are sharding tables with multiple data nodes, and no binding table rules are configured, `t_user` and `t_user_role` are single tables that distributed on different database instances. `Federation execution engine` can support the following commonly used join query:

```sql
SELECT * FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id WHERE o.order_id = 1;

SELECT * FROM t_order o INNER JOIN t_user u ON o.user_id = u.user_id WHERE o.user_id = 1;

SELECT * FROM t_order o LEFT JOIN t_user_role r ON o.user_id = r.user_id WHERE o.user_id = 1;

SELECT * FROM t_order_item i LEFT JOIN t_user u ON i.user_id = u.user_id WHERE i.user_id = 1;

SELECT * FROM t_order_item i RIGHT JOIN t_user_role r ON i.user_id = r.user_id WHERE i.user_id = 1;

SELECT * FROM t_user u RIGHT JOIN t_user_role r ON u.user_id = r.user_id WHERE u.user_id = 1;
```

### Unsupported

CASE WHEN can not support as following:

* `CASE WHEN` containing sub-query
* `CASE WHEN` containing logical-table (instead of table alias)

UNION and UNION ALL can not support as following:

* containing sharding or broadcast table

### SQL Example

| Stable supported SQL                                                                        | Necessary conditions                                           |
| ------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| SELECT * FROM tbl_name                                                                      |                                                                |
| SELECT * FROM tbl_name WHERE (col1 = ? or col2 = ?) and col3 = ?                            |                                                                |
| SELECT * FROM tbl_name WHERE col1 = ? ORDER BY col2 DESC LIMIT ?                            |                                                                |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = ?    |                                                                |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = ? GROUP BY col1 ORDER BY col3 DESC LIMIT ?, ? |                                                                |
| SELECT DISTINCT * FROM tbl_name WHERE col1 = ?                                              |                                                                |
| SELECT COUNT(DISTINCT col1), SUM(DISTINCT col1) FROM tbl_name                               |                                                                |
| (SELECT * FROM tbl_name)                                                                    |                                                                |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o WHERE o.col1 = ?                    | Subquery and outer query in same sharded data node after route |
| INSERT INTO tbl_name (col1, col2,...) VALUES (?, ?, ....)                                   |                                                                |
| INSERT INTO tbl_name VALUES (?, ?,....)                                                     |                                                                |
| INSERT INTO tbl_name (col1, col2, ...) VALUES(1 + 2, ?, ...)                                |                                                                |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (?, ?, ....), (?, ?, ....)                    |                                                                |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ?  | Inserted and selected table must be the same or binding tables |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ? | Replaced and selected table must be the same or binding tables |
| UPDATE tbl_name SET col1 = ? WHERE col2 = ?                                                 |                                                                |
| DELETE FROM tbl_name WHERE col1 = ?                                                         |                                                                |
| CREATE TABLE tbl_name (col1 int, ...)                                                       |                                                                |
| ALTER TABLE tbl_name ADD col1 varchar(10)                                                   |                                                                |
| DROP TABLE tbl_name                                                                         |                                                                |
| TRUNCATE TABLE tbl_name                                                                     |                                                                |
| CREATE INDEX idx_name ON tbl_name                                                           |                                                                |
| DROP INDEX idx_name ON tbl_name                                                             |                                                                |
| DROP INDEX idx_name                                                                         |                                                                |

***

| Experimental supported SQL                                               | Necessary conditions                                                |
| ------------------------------------------------------------------------ | ------------------------------------------------------------------- |
| SELECT * FROM (SELECT * FROM tbl_name) o                                 |                                                                     |
| SELECT * FROM (SELECT * FROM tbl_name) o WHERE o.col1 = ?                |                                                                     |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o                  |                                                                     |
| SELECT * FROM (SELECT * FROM tbl_name WHERE col1 = ?) o WHERE o.col1 = ? | Subquery and outer query in different sharded data node after route |
| SELECT (SELECT MAX(col1) FROM tbl_name) a, col2 from tbl_name            |                                                                     |
| SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name                       |                                                                     |
| SELECT col1, SUM(col2) FROM tbl_name GROUP BY col1 HAVING SUM(col2) > ?  |                                                                     |

***

| Slow SQL                                                            | Reason                                                       |
| ------------------------------------------------------------------- | ------------------------------------------------------------ |
| SELECT * FROM tbl_name WHERE to_date(create_time, 'yyyy-mm-dd') = ? | Full route because of sharding value in calculate expression |

***

| Unsupported SQL                                                               | Reason                                                                | Solution               |
| ----------------------------------------------------------------------------- | --------------------------------------------------------------------- | ---------------------- |
| INSERT INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?  | SELECT clause does not support *-shorthand and built-in key generator | -                      |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ? | SELECT clause does not support *-shorthand and built-in key generator | -                      |
| SELECT MAX(tbl_name.col1) FROM tbl_name                                       | Use table name as column owner in function                            | Instead of table alias |
