+++
title = "Limitations"
weight = 2
+++

Compatible with all commonly used SQL that routes to single data nodes; SQL routing to multiple data nodes is divided, because of complexity issues, into three conditions: stable support, experimental support, and no support.

## Stable Support

Full support for DML, DDL, DCL, TCL, and common DALs. Support for complex queries such as paging, de-duplication, sorting, grouping, aggregation, table association, etc.
Support SCHEMA DDL and DML statements of PostgreSQL and openGauss database. When no schema is specified in SQL, default access to 'public' schema. Other schemas need to declare before the table name, and do not support 'SEARCH_PATH' to modify the schema search path.

### Normal Queries

- main statement SELECT

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

### Sub-query

Stable support is provided by the kernel when both the subquery and the outer query specify a shard key and the values of the slice key remain consistent.
e.g:

```sql
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;
```
Sub-query for [pagination](https://shardingsphere.apache.org/document/current/en/features/sharding/limitation/#pagination-query) can be stably supported by the kernel.
e.g.:

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;
```

### Pagination Query

MySQL, PostgreSQL, and openGauss are fully supported, Oracle and SQLServer are only partially supported due to more intricate paging queries.

Pagination for Oracle and SQLServer needs to be handled by subqueries, and ShardingSphere supports paging-related subqueries.

- Oracle
Support pagination by rownum

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT o.order_id as order_id FROM t_order o JOIN t_order_item i ON o.order_id = i.order_id) row_ WHERE rownum <= ?) WHERE rownum > ?
```

- SQL Server
Support pagination that coordinates TOP + ROW_NUMBER() OVER

```sql
SELECT * FROM (SELECT TOP (?) ROW_NUMBER() OVER (ORDER BY o.order_id DESC) AS rownum, * FROM t_order o) AS temp WHERE temp.rownum > ? ORDER BY temp.order_id
```

Support pagination by OFFSET FETCH after SQLServer 2012

```sql
SELECT * FROM t_order o ORDER BY id OFFSET ? ROW FETCH NEXT ? ROWS ONLY
```

- MySQL, PostgreSQL and openGauss all support  LIMIT pagination without the need for sub-query：

```sql
SELECT * FROM t_order o ORDER BY id LIMIT ? OFFSET ?
```

### Aggregation

Support `MAX`, `MIN`, `SUM`, `COUNT`, `AVG`, `BIT_XOR`, `GROUP_CONCAT` and so on.

### Shard keys included in operation expressions

When the sharding key is contained in an expression, the value used for sharding cannot be extracted through the SQL letters and will result in full routing.

For example, assume `create_time` is a sharding key.

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

### LOAD DATA / LOAD XML

Support MySQL `LOAD DATA` and `LOAD XML` statements to load data to single table and broadcast table.

### View

1. Support create, alter and drop view based on a single table or multiple single tables on the same storage node;
2. Support create, alter and drop view based on any broadcast table;
3. Support create, alter and drop view based on any sharding table. The view must be configured with same sharding rules as sharding table, the view and sharding table must be in same binding table rule;
4. Support create, alter and drop view based on broadcast tables and sharding tables. The sharding table rules are same as create view using sharding tables alone;
5. Support create, alter and drop view based on broadcast tables and single tables;
6. Support MySQL `SHOW CREATE TABLE viewName` to show create statement of the view.

## Experimental Support 

Experimental support refers specifically to support provided by implementing Federation execution engine, an experimental product that is still under development. Although largely available to users, it still requires significant optimization.

### Sub-query

The Federation execution engine provides support for subqueries and outer queries that do not both specify a sharding key or have inconsistent values for the sharding key.

e.g:

```sql
SELECT * FROM (SELECT * FROM t_order) o;

SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;
```

### Cross-database Associated query

When multiple tables in an associated query are distributed across different database instances, the Federation execution engine can provide support. Assuming that t_order and t_order_item are sharded tables with multiple data nodes while no binding table rules are configured, and t_user and t_user_role are single tables distributed across different database instances, then the Federation execution engine can support the following common associated queries.

```sql
SELECT * FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id WHERE o.order_id = 1;

SELECT * FROM t_order o INNER JOIN t_user u ON o.user_id = u.user_id WHERE o.user_id = 1;

SELECT * FROM t_order o LEFT JOIN t_user_role r ON o.user_id = r.user_id WHERE o.user_id = 1;

SELECT * FROM t_order_item i LEFT JOIN t_user u ON i.user_id = u.user_id WHERE i.user_id = 1;

SELECT * FROM t_order_item i RIGHT JOIN t_user_role r ON i.user_id = r.user_id WHERE i.user_id = 1;

SELECT * FROM t_user u RIGHT JOIN t_user_role r ON u.user_id = r.user_id WHERE u.user_id = 1;
```

## Do not Support

### CASE WHEN

The following CASE WHEN statements are not supported:
- `CASE WHEN` contains sub-query
- Logic names are used in `CASE WHEN`( Please use an alias)

### Pagination Query

Due to the complexity of paging queries, there are currently some paging queries that are not supported for Oracle and SQLServer, such as:
- Oracle
The paging method of rownum + BETWEEN is not supported at present

- SQLServer
Currently, pagination with `WITH xxx AS (SELECT ...)` is not supported. Since the SQLServer paging statement automatically generated by Hibernate uses the `WITH` statement, Hibernate-based SQLServer paging is not supported at this moment. Pagination using two TOP + subquery also cannot be supported at this time.

### Aggregation

When a query contains multiple aggregate functions at the same time, it does not support mixing aggregate functions with DISTINCT and aggregate functions without DISTINCT.

### LOAD DATA / LOAD XML

Not support MySQL `LOAD DATA` and `LOAD XML` statements to load data to sharding table.

### Semicolons separate multiple statements

Not support simultaneous execution of multiple SQL statements separated by `;`.
