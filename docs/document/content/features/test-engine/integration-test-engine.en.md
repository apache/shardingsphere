+++
pre = "<b>3.10.2. </b>"
title = "Integration Test Engine"
weight = 2
+++

## Process
The `Parameterized` in JUnit will collect all test data, and pass to test method to assert one by one. The process of handling test data is just like a leaking hourglass:

![](https://shardingsphere.apache.org/document/current/img/test-engine/integration-test.jpg)

### Configuration
 
  - environment type
    - /shardingsphere-test-suite/src/test/resources/integrate/env-jdbc-local.properties
    - /shardingsphere-test-suite/src/test/resources/integrate/env/`SQL-TYPE`/dataset.xml
    - /shardingsphere-test-suite/src/test/resources/integrate/env/`SQL-TYPE`/schema.xml
  - test case type
    - /shardingsphere-test-suite/src/test/resources/integrate/cases/`SQL-TYPE`/`SQL-TYPE`-integrate-test-cases.xml
    - /shardingsphere-test-suite/src/test/resources/integrate/cases/`SQL-TYPE`/dataset/`FEATURE-TYPE`/*.xml
  - sql-case 
    - /sharding-sql-test/src/main/resources/sql/sharding/`SQL-TYPE`/*.xml

### Environment Configuration

Integration test depends on existed database environment, developer need to setup the configuration file for corresponding database to test: 

Firstly, setup configuration file `/shardingsphere-test-suite/src/test/resources/integrate/env-jdbc-local.properties`, for example: 

```properties
# the switch for PK, concurrent, column index testing and so on
run.additional.cases=false

# sharding rule, could define multiple rules
sharding.rule.type=db,tbl,dbtbl_with_replica_query_,replica_query_

# database type, could define multiple databases(H2,MySQL,Oracle,SQLServer,PostgreSQL)
databases=MySQL,PostgreSQL

# MySQL configuration
mysql.host=127.0.0.1
mysql.port=13306
mysql.username=root
mysql.password=root

## PostgreSQL configuration
postgresql.host=db.psql
postgresql.port=5432
postgresql.username=postgres
postgresql.password=postgres

## SQLServer configuration
sqlserver.host=db.mssql
sqlserver.port=1433
sqlserver.username=sa
sqlserver.password=Jdbc1234

## Oracle configuration
oracle.host=db.oracle
oracle.port=1521
oracle.username=jdbc
oracle.password=jdbc
```

Secondly, setup configuration file `/shardingsphere-test-suite/src/test/resources/integrate/env/SQL-TYPE/dataset.xml`. 
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

So far have confirmed what kind of sql execute in which environment in upon config, here define the data for assert.
There are two kinds of config for assert, one is at `/shardingsphere-test-suite/src/test/resources/integrate/cases/SQL-TYPE/SQL-TYPE-integrate-test-cases.xml`.
This file just like an index, defined the sql, parameters and expected index position for execution. the SQL is the value for `sql-case-id`. For example: 

```xml
<integrate-test-cases>
    <dml-test-case sql-case-id="insert_with_all_placeholders">
       <assertion parameters="1:int, 1:int, insert:String" expected-data-file="insert_for_order_1.xml" />
       <assertion parameters="2:int, 2:int, insert:String" expected-data-file="insert_for_order_2.xml" />
    </dml-test-case>
</integrate-test-cases>
```

Another kind of config for assert is the data, as known as the corresponding expected-data-file in SQL-TYPE-integrate-test-cases.xml, which is at `/shardingsphere-test-suite/src/test/resources/integrate/cases/SQL-TYPE/dataset/FEATURE-TYPE/*.xml`.  
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
