+++
pre = "<b>3.7.3. </b>"
title = "Use Norms"
weight = 3
+++

## Shadow database

### Supported Items

* The database is MySQL, Oracle, PostgreSQL, SQLServer;

### Unsupported Items

* NoSQL database;

## Shadow algorithm

### Supported Items

* The note shadow algorithm supports MDL and DDL statements;
* The column shadow algorithm basically supports commonly used MDL statements;

### Unsupported Items

* Column shadow algorithm does not support DDL statements；
* The column shadow algorithm does not support range value matching operations, for example: subQuery, BETWEEN, GROUP BY ... HAVING...;
* Use shadow library function + sub-library sub-table function, some special SQL is not supported, 
  please refer to[SQL Usage Specification](https://shardingsphere.apache.org/document/current/en/features/sharding/use-norms/sql/)

## Column shadow algorithm DML statement support list

* INSERT statement

Judge the inserted column and inserted value of `INSERT` operation

| *Operation* | *SQL* | *Support*  |
| -------- | --------- | --------- |
| INSERT   | INSERT INTO table (column,...) VALUES (value,...) |  true |
| INSERT   | INSERT INTO table (column,...) VALUES (value,...),(value,...),... |  true |
| INSERT   | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  false |

* SELECT/UPDATE/DELETE statement

Judge the fields and values included in the `WHERE` condition

| *Condition* | *SQL* | *Support* |
| -------- | --------- | --------- |
| = | SELECT/UPDATE/DELETE ... WHERE column = value | true |
| LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value | true |
| IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...) | true |
| BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2 | false |
| GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value; | false |
| subQuery | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | false |
