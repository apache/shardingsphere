+++
title = "Use Norms"
weight = 2
+++

## Supported

* Hint based shadow algorithm support all SQL;
* Column based shadow algorithm support part of SQL.

## Unsupported

### Hint based shadow algorithm

* None

### Column based shadow algorithm

* Does not support DDL;
* Does not support range, group and subquery, for example: BETWEEN, GROUP BY ... HAVING...;

SQL support list:

- INSERT

| *SQL*                                                                            | *Supported*  |
| -------------------------------------------------------------------------------- | ------------ |
| INSERT INTO table (column,...) VALUES (value,...)                                |  Y           |
| INSERT INTO table (column,...) VALUES (value,...),(value,...),...                |  Y           |
| INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  N           |

- SELECT/UPDATE/DELETE

| *Condition*            | *SQL*                                                                                   | *Supported* |
| ---------------------- | --------------------------------------------------------------------------------------- | ----------- |
| =                      | SELECT/UPDATE/DELETE ... WHERE column = value                                           | Y           |
| LIKE/NOT LIKE          | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value                               | Y           |
| IN/NOT IN              | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)                     | Y           |
| BETWEEN                | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2                         | N           |
| GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value                | N           |
| Subquery               | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | N           |
