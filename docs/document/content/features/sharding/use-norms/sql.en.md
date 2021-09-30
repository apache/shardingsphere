+++
title = "SQL"
weight = 1
+++

Since the SQL syntax is flexible and complex and distributed databases and stand-alone databases do not have identical query scenarios, SQLs incompatible with stand-alone databases are hard to avoid.

This document has listed identified supported SQL types and unsupported SQL types, trying to avoid traps for users.

It is inevitably to have some unlisted SQLs, welcome to supplement for that. We will also try to support those unavailable SQLs in future versions.

## Parse Engine

Parse engine consists of `SQLParser` and `SQLVisitor`. `SQLParser` parses SQL into a syntax tree. `SQLVisitor` converts the syntax tree into `SQLStatement`. Parse engine supports MySQL, PostgreSQL, SQLServer, Oracle, openGauss and SQL that conform to the SQL92 specification. However, due to the complexity of SQL syntax, there are still a little of SQL that the parse engine does not support. The list is as follows:

### Unsupported SQL
#### MySQL

| SQL                                                                                        | 
| ------------------------------------------------------------------------------------------ |
| FLUSH PRIVILEGES                                                                           | 
| CLONE LOCAL DATA DIRECTORY = 'clone_dir'                                                   | 
| INSTALL COMPONENT 'file://component1', 'file://component2'                                 | 
| UNINSTALL COMPONENT 'file://component1', 'file://component2'                               | 
| SHOW CREATE USER user                                                                      | 
| REPAIR TABLE t_order                                                                       | 
| OPTIMIZE TABLE t_order                                                                     | 
| CHECKSUM TABLE t_order                                                                     | 
| CHECK TABLE t_order                                                                        | 
| SET RESOURCE GROUP group_name                                                              | 
| DROP RESOURCE GROUP group_name                                                             | 
| CREATE RESOURCE GROUP group_name TYPE = SYSTEM                                             | 
| ALTER RESOURCE GROUP rg1 VCPU = 0-63                                                       | 

## Data Sharding

### Supported SQL

#### Route to single data node

- 100% compatible（MySQL only, we are completing other databases).

#### Route to multiple data nodes

Fully support DML, DDL, DCL, TCL and some DAL. Support pagination, DISTINCT, ORDER BY, GROUP BY, aggregation and JOIN. Here is an example of a most complex kind of DML:

- Main SELECT

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

### Unsupported SQL

#### Route to multiple data nodes

Partially support CASE WHEN
* `CASE WHEN` containing sub-query is not supported
* `CASE WHEN` containing logical-table is not supported(please use alias of table)

Partly available UNION (ALL)
* `Union (ALL)` containing sharding or broadcast table is not supported

Partly available sub-query
* Subquery is supported by kernel when sharding keys are specified in both subquery and outer query, and values of sharding keys are the same.
* Subquery is supported by federation executor engine (under development) when sharding keys are not specified for both subquery and outer query, or values of sharding keys are not the same.

Support not only pagination sub-query (see [pagination](https://shardingsphere.apache.org/document/current/cn/features/sharding/usage-standard/pagination) for more details), but also sub-query with the same mode.

For example, the following subquery is supported by kernel:

```sql
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;
```

The following subquery is supported by federation executor engine (under development):

```sql
SELECT * FROM (SELECT * FROM t_order) o;
SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;
SELECT * FROM (SELECT * FROM t_order WHERE product_id = 1) o;
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;
```

To be simple, through subquery, non-functional requirements are supported by kernel in most cases, such as pagination, sum count and so on. Functional requirements are supported by federation executor engine (under development).

Do not support SQL that contains actual schema, but support SQL that contains logic schema. For the concept of ShardingSphere is to use multiple data source as one data source, so all the SQL visits are based on one logic schema.

#### Operation for shardingColumn

ShardingColumn in expressions and functions will lead to full routing.

The following SQL is unavailable to single sharding, if `create_time` is shardingColumn:

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

ShardingSphere extract the value of ShardingColumn through `literal` of SQL, so ShardingSphere can not calculate the sharding value from the SQL because the data inside the expression is in database.

When shardingColumn in expressions and functions, ShardingSphere will use full routing to get results.

### Example

#### Supported SQL

| SQL                                                                                         | Necessary conditions                    |
| ------------------------------------------------------------------------------------------- | --------------------------------------- |
| SELECT * FROM tbl_name                                                                      |                                         |
| SELECT * FROM tbl_name WHERE (col1 = ? or col2 = ?) and col3 = ?                            |                                         |
| SELECT * FROM tbl_name WHERE col1 = ? ORDER BY col2 DESC LIMIT ?                            |                                         |
| SELECT COUNT(*), SUM(col1), MIN(col1), MAX(col1), AVG(col1) FROM tbl_name WHERE col1 = ?    |                                         |
| SELECT COUNT(col1) FROM tbl_name WHERE col2 = ? GROUP BY col1 ORDER BY col3 DESC LIMIT ?, ? |                                         |
| SELECT col1, SUM(col2) FROM tbl_name GROUP BY col1 HAVING SUM(col2) > 10                    |                                         |
| SELECT DISTINCT * FROM tbl_name WHERE col1 = ?                                              |                                         |
| SELECT COUNT(DISTINCT col1) FROM tbl_name                                                   |                                         |
| SELECT subquery_alias.col1 FROM (select tbl_name.col1 from tbl_name where tbl_name.col2=?) subquery_alias                                                   |                                         |
| SELECT (SELECT MAX(col1) FROM tbl_name) a, col2 from tbl_name                               |                                         |
| (SELECT * FROM tbl_name)                                                                    |                                         |
| INSERT INTO tbl_name (col1, col2,...) VALUES (?, ?, ....)                                   |                                         |
| INSERT INTO tbl_name VALUES (?, ?,....)                                                     |                                         |
| INSERT INTO tbl_name (col1, col2, ...) VALUES(1 + 2, ?, ...)                                |                                         |
| INSERT INTO tbl_name (col1, col2, ...) VALUES (?, ?, ....), (?, ?, ....)                    |                                         |
| INSERT INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ?  | The table inserted and the table selected must be the same or bind tables |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT col1, col2, ... FROM tbl_name WHERE col3 = ? | The table replaced and the table selected must be the same or bind tables |
| UPDATE tbl_name SET col1 = ? WHERE col2 = ?                                                 |                                         |
| DELETE FROM tbl_name WHERE col1 = ?                                                         |                                         |
| CREATE TABLE tbl_name (col1 int, ...)                                                       |                                         |
| ALTER TABLE tbl_name ADD col1 varchar(10)                                                   |                                         |
| DROP TABLE tbl_name                                                                         |                                         |
| TRUNCATE TABLE tbl_name                                                                     |                                         |
| CREATE INDEX idx_name ON tbl_name                                                           |                                         |
| DROP INDEX idx_name ON tbl_name                                                             |                                         |
| DROP INDEX idx_name                                                                         |                                         |

#### Unsupported SQL

| SQL                                                                                        | Reason                                              |
| ------------------------------------------------------------------------------------------ | --------------------------------------------------- |
| INSERT INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?               | SELECT clause does not support *-shorthand and built-in key generators |
| REPLACE INTO tbl_name (col1, col2, ...) SELECT * FROM tbl_name WHERE col3 = ?              | SELECT clause does not support *-shorthand and built-in key generators |
| SELECT * FROM tbl_name WHERE to_date(create_time, 'yyyy-mm-dd') = ?                        | Lead to full routing                                |
| SELECT MAX(tbl_name.col1) FROM tbl_name                                                    | The select function item contains TableName. Otherwise, If this query table had an alias, then TableAlias could work well in select function items. |

### DISTINCT Availability Explanation

#### Supported SQL

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
| SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name            |
| SELECT COUNT(DISTINCT col1) FROM tbl_name                     |
| SELECT COUNT(DISTINCT col1) FROM tbl_name GROUP BY col1       |
| SELECT COUNT(DISTINCT col1 + col2) FROM tbl_name              |
| SELECT COUNT(DISTINCT col1), SUM(DISTINCT col1) FROM tbl_name |
| SELECT COUNT(DISTINCT col1), col1 FROM tbl_name GROUP BY col1 |
| SELECT col1, COUNT(DISTINCT col1) FROM tbl_name GROUP BY col1 |

#### Unsupported SQL

| SQL                                                | Reason                                                                             |
| -------------------------------------------------- | ---------------------------------------------------------------------------------- |
| SELECT SUM(DISTINCT tbl_name.col1), tbl_name.col2 FROM tbl_name | The select function item contains TableName. Otherwise, If this query table had an alias, then TableAlias could work well in select function items. |