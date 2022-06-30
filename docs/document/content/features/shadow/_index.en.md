+++
pre = "<b>7.6. </b>"
title = "Shadow"
weight = 6
+++

## Definition
Solution for stress testing data governance at the database level, under the online full link stress testing scenario of Apache Shardingsphere.

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
  | LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value  | support  |                        | IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)  | support |
  | BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2  | do not support   |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value  | do not support      |
  | Sub Query | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | do not support   |

## How it works

Apache ShardingSphere determines the incoming SQL via shadow by parsing the SQL and routing it to the production or shadow database based on the shadow rules set by the user in the configuration file.
![Execute Process](https://shardingsphere.apache.org/document/current/img/shadow/execute.png)

In the example of an INSERT statement, when writing data, Apache ShardingSphere parses the SQL and then constructs a routing chain based on the rules in the configuration file.
In the current version, the shadow feature is at the last execution unit in the routing chain, i.e. if other rules exist that require routing, such as sharding, Apache ShardingSphere will first route to a particular database according to the sharding rules, and then run the shadow routing determination process to determine that the execution SQL meets the configuration set by shadow rules. Then data is routed to the corresponding shadow database, while the production data remains unchanged.

### DML sentence
Two algorithms are supported. Shadow determination first determines whether the execution SQL-related table intersects with the configured shadow table. If the result is positive, the shadow algorithm within the part of intersection associated with the shadow table will be determined sequentially. If any of the determination is successful, the SQL statement is routed to the shadow library.
If there is no intersection or the shadow algorithm determination is unsuccessful, the SQL statement is routed to the production database.

### DDL sentence
Only supports shadow algorithm with comments attached. In stress testing scenarios, DDL statements are generally not required for testing, and are used mainly when initializing or modifying shadow tables in the shadow database.
The shadow determination will first determine whether the execution SQL contains comments  or not. If the result is a yes, the HINT shadow algorithm configured in the shadow rules determines them in order. The SQL statement is routed to the shadow database if any of the determinations are successful.
If the execution SQL does not contain comments or the HINT shadow algorithm determination is unsuccessful, the SQL statements are routed to the production database.

## References
[JAVA API: shadow database configuration](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)

[YAML Configuration: shadow database](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)

[ Spring Boot Starter: shadow database configuration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)

[Spring namespace: shadow database configuration](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
