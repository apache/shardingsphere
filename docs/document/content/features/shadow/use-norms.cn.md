+++
title = "使用规范"
weight = 2
+++

## 影子数据库

### 支持项

* 后端数据库为 MySQL、Oracle、PostgreSQL、SQLServer；

### 不支持项

* NoSQL 数据库；

## 影子算法

### 支持项

* 注解影子算法支持 MDL，DDL 语句；
* 列影子算法基本支持常用的 MDL 语句；

### 不支持项

* 列影子算法不支持 DDL 语句。
* 列影子算法不支持范围值匹配操作，比如：子查询、BETWEEN、GROUP BY ... HAVING等；
* 使用影子库功能+分库分表功能，部分特殊SQL不支持，请参考[SQL使用规范](https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)

## 列影子算法 DML 语句支持度列表

* INSERT 语句

对 `INSERT` 插入的字段和插入的值判定

| *操作类型* | *SQL语句* | *是否支持*  |
| -------- | --------- | --------- |
| INSERT   | INSERT INTO table (column,...) VALUES (value,...) |  支持 |
| INSERT   | INSERT INTO table (column,...) VALUES (value,...),(value,...),... |  支持 |
| INSERT   | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  不支持 |

* SELECT/UPDATE/DELETE语句

对 `WHERE` 条件中包含的字段和值进行判定

| *条件类型* | *SQL语句* | *是否支持* |
| -------- | --------- | --------- |
| = | SELECT/UPDATE/DELETE ... WHERE column = value | 支持 |
| LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value | 支持 |
| IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...) | 支持 |
| BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2 | 不支持 |
| GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value; | 不支持 |
| 子查询 | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | 不支持 |
