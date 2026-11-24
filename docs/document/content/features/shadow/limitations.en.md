+++
title = "Limitations"
weight = 2
+++

## Hint based shadow algorithm

No

## Column based shadow algorithm
SQL does not support lists：
- Does not support DDL.

- Does not support scope, group, subqueries such as BETWEEN, GROUP BY ... HAVING, etc.

SQL support list

  - INSERT
  
  | *SQL*                                                                            | *support or not* |
  |----------------------------------------------------------------------------------|------------------|
  | INSERT INTO table (column,...) VALUES (value,...)                                | support          |
  | INSERT INTO table (column,...) VALUES (value,...),(value,...),...                | support          |
  | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 | do not support   |
 
  - SELECT/UPDATE/DELETE
  
  | *condition categories* | *SQL*                                                                                   | *support or not* |
  |------------------------|-----------------------------------------------------------------------------------------|------------------|
  | =                      | SELECT/UPDATE/DELETE ... WHERE column = value                                           | support          |
  | LIKE/NOT LIKE          | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value                               | support          |                        
  | IN/NOT IN              | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)                     | support          |
  | BETWEEN                | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2                         | do not support   |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value                | do not support   |
  | Sub Query              | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | do not support   |

## Batch INSERT

Multi-row INSERT values are routed per value when the column shadow algorithms match only part of them, so matching values go to the shadow data source and the remaining values go to the production data source.
The split statement runs on both data sources, so atomicity relies on the transaction rule configured for the database.
