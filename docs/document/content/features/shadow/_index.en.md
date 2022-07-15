+++
pre = "<b>4.10. </b>"
title = "Shadow"
weight = 10
+++

## Definition
Solution for stress testing data governance at the database level, under the online full link stress testing scenario of Apache ShardingSphere.

## Related Concepts

### Production Database
Database for production data

### Shadow Database
The Database for stress test data isolation. Configurations should be the same as the Production Database.

### Shadow Algorithm
Shadow Algorithm, which is closely related to business operations, currently has 2 types.

- Column based shadow algorithm
Routing to shadow database by recognizing data from SQL. Suitable for stress test scenario that has an emphasis on data list.
- Hint based shadow algorithm
Routing to shadow database by recognizing comments from SQL. Suitable for stress test driven by the identification of upstream system passage.

## Limitations

### Hint based shadow algorithm
No

### Column based shadow algorithm
Does not support DDL.
Does not support scope, group, subqueries such as BETWEEN, GROUP BY ... HAVING, etc.
SQL support list

  - INSERT
  
  |  *SQL*  |  *support or not*  |
  | ------- | ------------ |
  | INSERT INTO table (column,...) VALUES (value,...)   |  support  |
  | INSERT INTO table (column,...) VALUES (value,...),(value,...),...   |  support   |
  | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  do not support   |
  - SELECT/UPDATE/DELETE
  
  |  *condition categories*  |  *SQL*   |  *support or not*  |
  | ------------ | -------- | ----------- |
  | =  | SELECT/UPDATE/DELETE ... WHERE column = value   | support |
  | LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value  | support  |                        
  | IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)  | support |
  | BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2  | do not support   |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value  | do not support      |
  | Sub Query | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | do not support   |
