+++
pre = "<b>3.9.1. </b>"
title = "Integration Test"
weight = 1
+++

The SQL parsing unit test covers both SQL placeholder and literal dimension. 
Integration test can be further divided into two dimensions of strategy and JDBC; the former one includes strategies as Sharding, table Sharding, database Sharding, and replica query while the latter one includes `Statement` and `PreparedStatement`.

Therefore, one SQL can drive 5 kinds of database parsing * 2 kinds of parameter transmission modes + 5 kinds of databases * 5 kinds of Sharding strategies * 2 kinds of JDBC operation modes = 60 test cases, to enable ShardingSphere to achieve the pursuit of high quality.

## Process

The `Parameterized` in JUnit will collect all test data, and pass to test method to assert one by one. The process of handling test data is just like a leaking hourglass:

### Configuration
 
  - environment type
    - /shardingsphere-integration-test-suite/src/test/resources/env-native.properties
    - /shardingsphere-integration-test-suite/src/test/resources/env/`SQL-TYPE`/dataset.xml
    - /shardingsphere-integration-test-suite/src/test/resources/env/`SQL-TYPE`/schema.xml
  - test case type
    - /shardingsphere-integration-test-suite/src/test/resources/cases/`SQL-TYPE`/`SQL-TYPE`-integration-test-cases.xml
    - /shardingsphere-integration-test-suite/src/test/resources/cases/`SQL-TYPE`/dataset/`FEATURE-TYPE`/*.xml
  - sql-case 
    - /sharding-sql-test/src/main/resources/sql/sharding/`SQL-TYPE`/*.xml

### Environment Configuration

Integration test depends on existed database environment, developer need to setup the configuration file for corresponding database to test: 

Firstly, setup configuration file `/shardingsphere-integration-test-suite/src/test/resources/env-native.properties`, for example: 

```properties
# the switch for PK, concurrent, column index testing and so on
it.run.additional.cases=false

# test scenarios, could define multiple rules
it.scenarios=db,tbl,dbtbl_with_replica_query,replica_query

# database type, could define multiple databases(H2,MySQL,Oracle,SQLServer,PostgreSQL)
it.databases=MySQL,PostgreSQL

# MySQL configuration
it.mysql.host=127.0.0.1
it.mysql.port=13306
it.mysql.username=root
it.mysql.password=root

## PostgreSQL configuration
it.postgresql.host=db.psql
it.postgresql.port=5432
it.postgresql.username=postgres
it.postgresql.password=postgres

## SQLServer configuration
it.sqlserver.host=db.mssql
it.sqlserver.port=1433
it.sqlserver.username=sa
it.sqlserver.password=Jdbc1234

## Oracle configuration
it.oracle.host=db.oracle
it.oracle.port=1521
it.oracle.username=jdbc
it.oracle.password=jdbc
```

Secondly, setup configuration file `/shardingsphere-integration-test-suite/src/test/resources/env/SQL-TYPE/dataset.xml`. 
Developer can set up metadata and expected data to start the data initialization in `dataset.xml`. For example: 

```xml
<dataset>
    <metadata data-nodes="tbl.t_order_${0..9}">
        <column name="order_id" type="numeric" />
        <column name="user_id" type="numeric" />
        <column name="status" type="varchar" />
    </metadata>
    <row data-node="tbl.t_order_0" values="1000, 10, init" />
    <row data-node="tbl.t_order_1" values="1001, 10, init" />
    <row data-node="tbl.t_order_2" values="1002, 10, init" />
    <row data-node="tbl.t_order_3" values="1003, 10, init" />
    <row data-node="tbl.t_order_4" values="1004, 10, init" />
    <row data-node="tbl.t_order_5" values="1005, 10, init" />
    <row data-node="tbl.t_order_6" values="1006, 10, init" />
    <row data-node="tbl.t_order_7" values="1007, 10, init" />
    <row data-node="tbl.t_order_8" values="1008, 10, init" />
    <row data-node="tbl.t_order_9" values="1009, 10, init" />
</dataset>
```

Developer can customize DDL to create databases and tables in `schema.xml`.

### Assertion Configuration

So far have confirmed what kind of sql execute in which environment in upon configuration, here define the data for assert.
There are two kinds of config for assert, one is at `/shardingsphere-integration-test-suite/src/test/resources/cases/SQL-TYPE/SQL-TYPE-integration-test-cases.xml`.
This file just like an index, defined the sql, parameters and expected index position for execution. the SQL is the value for `sql-case-id`. For example: 

```xml
<integration-test-cases>
    <dml-test-case sql-case-id="insert_with_all_placeholders">
       <assertion parameters="1:int, 1:int, insert:String" expected-data-file="insert_for_order_1.xml" />
       <assertion parameters="2:int, 2:int, insert:String" expected-data-file="insert_for_order_2.xml" />
    </dml-test-case>
</integration-test-cases>
```

Another kind of config for assert is the data, as known as the corresponding expected-data-file in SQL-TYPE-integration-test-cases.xml, which is at `/shardingsphere-integration-test-suite/src/test/resources/cases/SQL-TYPE/dataset/FEATURE-TYPE/*.xml`.  
This file is very like the dataset.xml mentioned before, and the difference is that expected-data-file contains some other assert data, such as the return value after a sql execution. For examples:  

```xml
<dataset update-count="1">
    <metadata data-nodes="db_${0..9}.t_order">
        <column name="order_id" type="numeric" />
        <column name="user_id" type="numeric" />
        <column name="status" type="varchar" />
    </metadata>
    <row data-node="db_0.t_order" values="1000, 10, update" />
    <row data-node="db_0.t_order" values="1001, 10, init" />
    <row data-node="db_0.t_order" values="2000, 20, init" />
    <row data-node="db_0.t_order" values="2001, 20, init" />
</dataset>
```
Util now, all config files are ready, just launch the corresponding test case is fine.With no need to modify any Java code, only set up some config files.
This will reduce the difficulty for ShardingSphere testing.

## Notice

1. If Oracle needs to be tested, please add Oracle driver dependencies to the pom.xml.
1. 10 splitting-databases and 10 splitting-tables are used in the integrated test to ensure the test data is full, so it will take a relatively long time to run the test cases.
