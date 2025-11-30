+++
title = "使用限制"
weight = 2
+++

兼容全部常用的路由至单数据节点的 SQL； 路由至多数据节点的 SQL 由于场景复杂，分为稳定支持、实验性支持和不支持这三种情况。

## 稳定支持

全面支持 DML、DDL、DCL、TCL 和常用 DAL。 支持分页、去重、排序、分组、聚合、表关联等复杂查询。
支持 PostgreSQL 和 openGauss 数据库的 schema DDL 和 DML 语句，当 SQL 中不指定 schema 时，默认访问 `public` schema，其他 schema 则需要在表名前显示声明，暂不支持 `SEARCH_PATH` 修改 schema 搜索路径。

### 常规查询

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

### 子查询

子查询和外层查询同时指定分片键，且分片键的值保持一致时，由内核提供稳定支持。

例如：

```sql
SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 1;
```

用于分页的子查询，由内核提供稳定支持。

例如：

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT * FROM t_order) row_ WHERE rownum <= ?) WHERE rownum > ?;
```

### 分页查询

完全支持 MySQL、PostgreSQL、openGauss，Oracle 和 SQLServer 由于分页查询较为复杂，仅部分支持。

Oracle 和 SQLServer 的分页都需要通过子查询来处理，ShardingSphere 支持分页相关的子查询。

- Oracle

支持使用 rownum 进行分页：

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT o.order_id as order_id FROM t_order o JOIN t_order_item i ON o.order_id = i.order_id) row_ WHERE rownum <= ?) WHERE rownum > ?
```

- SQLServer

支持使用 TOP + ROW_NUMBER() OVER 配合进行分页：

```sql
SELECT * FROM (SELECT TOP (?) ROW_NUMBER() OVER (ORDER BY o.order_id DESC) AS rownum, * FROM t_order o) AS temp WHERE temp.rownum > ? ORDER BY temp.order_id
```
支持 SQLServer 2012 之后的 OFFSET FETCH 的分页方式：

```sql
SELECT * FROM t_order o ORDER BY id OFFSET ? ROW FETCH NEXT ? ROWS ONLY
```

- MySQL, PostgreSQL 和 openGauss

MySQL、PostgreSQL 和 openGauss 都支持 LIMIT 分页，无需子查询：

```sql
SELECT * FROM t_order o ORDER BY id LIMIT ? OFFSET ?
```

### 聚合查询

支持 `MAX`, `MIN`, `SUM`, `COUNT`, `AVG`, `BIT_XOR`, `GROUP_CONCAT` 聚合语法。

### 运算表达式中包含分片键

当分片键处于运算表达式中时，无法通过 SQL `字面` 提取用于分片的值，将导致全路由。
例如，假设 `create_time` 为分片键：

```sql
SELECT * FROM t_order WHERE to_date(create_time, 'yyyy-mm-dd') = '2019-01-01';
```

### LOAD DATA / LOAD XML

支持 MySQL `LOAD DATA` 和 `LOAD XML` 语句加载数据到单表和广播表。

### 视图

1. 支持基于单个单表，或多个相同存储节点的单表创建、修改和删除视图；
2. 支持基于任意个广播表创建、修改和删除视图；
3. 支持基于任意个分片表创建、修改和删除视图，视图必须和分片表一样配置分片规则，并且视图和分片表必须为绑定表关系；
4. 支持基于广播表和分片表创建、修改和删除视图，分片表规则同单独使用分片表创建视图；
5. 支持基于广播表和单表创建、修改和删除视图；
6. 支持 MySQL `SHOW CREATE TABLE viewName` 查看视图的创建语句。

## 实验性支持

实验性支持特指使用 Federation 执行引擎提供支持。 该引擎处于快速开发中，用户虽基本可用，但仍需大量优化，是实验性产品。

### 子查询

子查询和外层查询未同时指定分片键，或分片键的值不一致时，由 Federation 执行引擎提供支持。

例如：

```sql
SELECT * FROM (SELECT * FROM t_order) o;

SELECT * FROM (SELECT * FROM t_order) o WHERE o.order_id = 1;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o;

SELECT * FROM (SELECT * FROM t_order WHERE order_id = 1) o WHERE o.order_id = 2;
```

### 跨库关联查询

当关联查询中的多个表分布在不同的数据库实例上时，由 Federation 执行引擎提供支持。 假设 `t_order` 和 `t_order_item` 是多数据节点的分片表，并且未配置绑定表规则，`t_user` 和 `t_user_role` 是分布在不同的数据库实例上的单表，那么 Federation 执行引擎能够支持如下常用的关联查询：

```sql
SELECT * FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id WHERE o.order_id = 1;

SELECT * FROM t_order o INNER JOIN t_user u ON o.user_id = u.user_id WHERE o.user_id = 1;

SELECT * FROM t_order o LEFT JOIN t_user_role r ON o.user_id = r.user_id WHERE o.user_id = 1;

SELECT * FROM t_order_item i LEFT JOIN t_user u ON i.user_id = u.user_id WHERE i.user_id = 1;

SELECT * FROM t_order_item i RIGHT JOIN t_user_role r ON i.user_id = r.user_id WHERE i.user_id = 1;

SELECT * FROM t_user u RIGHT JOIN t_user_role r ON u.user_id = r.user_id WHERE u.user_id = 1;
```

## 不支持

### CASE WHEN

以下 `CASE WHEN` 语句不支持：

- `CASE WHEN` 中包含子查询
- `CASE WHEN` 中使用逻辑表名（请使用表别名）

### 分页查询

Oracle 和 SQLServer 由于分页查询较为复杂，目前有部分分页查询不支持，具体如下：

- Oracle

目前不支持 `rownum + BETWEEN` 的分页方式。

- SQLServer

目前不支持使用 `WITH xxx AS (SELECT …)` 的方式进行分页。由于 Hibernate 自动生成的 SQLServer 分页语句使用了 `WITH` 语句，因此目前并不支持基于 Hibernate 的 SQLServer 分页。 目前也不支持使用两个 TOP + 子查询的方式实现分页。

### 聚合查询

查询中同时包含多个聚合函数时，不支持带 DISTINCT 的聚合函数和不带 DISTINCT 的聚合函数混合使用。

### LOAD DATA / LOAD XML

不支持 MySQL `LOAD DATA` 和 `LOAD XML` 语句加载数据到分片表。

### 分号分隔多语句

不支持使用 `;` 分隔的多条 SQL 同时执行。
