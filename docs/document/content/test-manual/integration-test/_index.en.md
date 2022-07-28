+++
pre = "<b>6.1. </b>"
title = "Integration Test"
weight = 1
+++

## Design

The integration testing consists of three modules: test case, test environment and test engine.

### Test case

It is used to define the SQL to be tested and the assertion data of the test results.

Each case defines one SQL, which can define multiple database execution types.

### Test environment

It is used to set up the database and ShardingSphere-Proxy environment for running test cases.
The environment is classified into environment preparation mode, database type, and scenario.

Environment preparation mode is divided into Native and Docker, and Embed type will be supported in the future.
  - Native environment is used for test cases to run directly in the test environment provided by the developer, suitable for debugging scenarios;
  - Docker environment is directly built when Maven runs the Docker-Compose plug-in. It is suitable for cloud compilation environment and testing ShardingSphere-Proxy, such as GitHub Action;
  - Embed environment is built when the test framework automatically builds embedded MySQL. It is suitable for the local environment test of ShardingSphere-JDBC.

Currently, the Native environment is adopted by default, and ShardingSphere-JDBC + H2 database is used to run test cases.
Maven's `-pit. Env.docker` parameter specifies how the Docker environment is run.
In the future, ShardingSphere-JDBC + MySQL of the Embed environment will be adopted to replace the default environment type used when Native executes test cases.

Database types currently support MySQL, PostgreSQL, SQLServer, and Oracle, and test cases can be executed using ShardingSphere-JDBC or ShardingSphere-Proxy.

Scenarios are used to test the supporting rules of ShardingSphere. Currently, data sharding and read/write splitting and other related scenarios are supported, and the combination of scenarios will be improved continuously in the future. 

### Test engine

It is used to read test cases in batches and execute and assert test results line by line.

The test engine arranges test cases and environments to test as many scenarios as possible with the fewest test cases.

Each SQL generates a test report in the combination of `database type * access port type * SQL execution mode * JDBC execution mode * Scenario`. Currently, each dimension is supported as follows:

  - Database types: H2, MySQL, PostgreSQL, SQLServer, and Oracle;
  - Access port types: ShardingSphere-JDBC and ShardingSphere-Proxy;
  - SQL execution modes: Statement and PreparedStatement;
  - JDBC execution modes: execute and executeQuery/executeUpdate;
  - Scenarios: database shards, table shards, read/write splitting and sharding + read/write splitting

Therefore, one SQL will drive `Database type (5) * Access port type (2) * SQL execution mode (2) * JDBC execution mode (2) * Scenario (4) = 160` test cases to be run to achieve the pursuit of high quality.

## User Guide

Module path：`shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-suite`

### Test case configuration

SQL test case is in `resources/cases/${SQL-TYPE}/${SQL-TYPE}-integration-test-cases.xml`.

The case file format is as follows:

```xml
<integration-test-cases>
    <test-case sql="${SQL}">
        <assertion parameters="${value_1}:${type_1}, ${value_2}:${type_2}" expected-data-file="${dataset_file_1}.xml" />
        <!-- ... more assertions -->
        <assertion parameters="${value_3}:${type_3}, ${value_4}:${type_4}" expected-data-file="${dataset_file_2}.xml" />
     </test-case>
    
    <!-- ... more test cases -->
</integration-test-cases>
```

The lookup rule of `expected-data-file`is as follows: 
  1. Find the file `dataset\${SCENARIO_NAME}\${DATABASE_TYPE}\${dataset_file}.xml` in the same level directory;
  2. Find the file `dataset\${SCENARIO_NAME}\${dataset_file}.xml` in the same level directory;
  3. Find the file `dataset\${dataset_file}.xml` in the same level directory;
  4. Report an error if none of them are found.

The assertion file format is as follows:

```xml
<dataset>
    <metadata>
        <column name="column_1" />
        <!-- ... more columns -->
        <column name="column_n" />
    </metadata>
    <row values="value_01, value_02" />
    <!-- ... more rows -->
    <row values="value_n1, value_n2" />
</dataset>
```

### Environment configuration

`${SCENARIO-TYPE}` Refers to the scenario name used to identify a unique scenario during the test engine run.
`${DATABASE-TYPE}` refers to the database types.

#### Native environment configuration

Directory: `src/test/resources/env/${SCENARIO-TYPE}`

  - `scenario-env.properties`: data source configuration；
  - `rules.yaml`: rule configuration；
  - `databases.xml`: name of the real database；
  - `dataset.xml`: initialize the data；
  - `init-sql\${DATABASE-TYPE}\init.sql`: initialize the database and table structure；
  - `authority.xml`: to be supplemented.

#### Docker environment configuration

Directory: `src/test/resources/docker/${SCENARIO-TYPE}`

  - `docker-compose.yml`: Docker-Compose config files, used for Docker environment startup；
  - `proxy/conf/config-${SCENARIO-TYPE}.yaml`: rule configuration。

**The Docker environment configuration provides a remote debugging port for ShardingSphere-Proxy. You can find the second exposed port for remote debugging in `shardingsphere-proxy` of the `docker-comemage. yml` file. **

### Run the test engine

#### Configure the running environment of the test engine

Control the test engine by configuring `src/test/resources/env/engine-env.properties`. 

All attribute values can be dynamically injected via Maven command line `-D`.

```properties

# Scenario type. Multiple values can be separated by commas. Optional values: db, tbl, dbtbl_with_replica_query, replica_query
it.scenarios=db,tbl,dbtbl_with_replica_query,replica_query

# Whether to run additional test cases
it.run.additional.cases=false

# Configure the environment type. Only one value is supported. Optional value: docker or null. The default value: null. 
it.cluster.env.type=${it.env}
# Access port types to be tested. Multiple values can be separated by commas. Optional value: jdbc, proxy. The default value: jdbc
it.cluster.adapters=jdbc

# Scenario type. Multiple values can be separated by commas. Optional value: H2, MySQL, Oracle, SQLServer, PostgreSQL
it.cluster.databases=H2,MySQL,Oracle,SQLServer,PostgreSQL
```

#### Run debugging mode

  - Standard test engine
    Run `org.apache.shardingsphere.test.integration.engine.${SQL-TYPE}.General${SQL-TYPE}IT` to start the test engines of different SQL types.

  - Batch test engine
    Run `org.apache.shardingsphere.test.integration.engine.dml.BatchDMLIT` to start the batch test engine for the test `addBatch()` provided for DML statements.

  - Additional test engine
    Run `org.apache.shardingsphere.test.integration.engine.${SQL-TYPE}.Additional${SQL-TYPE}IT` to start the test engine with more JDBC method calls.
    Additional test engines need to be enabled by setting `it.run.additional.cases=true`.

#### Run Docker mode

```bash
./mvnw -B clean install -f shardingsphere-test/shardingsphere-integration-test/pom.xml -Pit.env.docker -Dit.cluster.adapters=proxy,jdbc -Dit.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -Dit.cluster.databases=MySQL
```
Run the above command to build a Docker mirror `apache/shardingsphere-proxy-test:latest` used for integration testing.
If you only modify the test code, you can reuse the existing test mirror without rebuilding it. Skip the mirror building and run the integration testing directly with the following command:

```bash
./mvnw -B clean install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-suite/pom.xml -Pit.env.docker -Dit.cluster.adapters=proxy,jdbc -Dit.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -Dit.cluster.databases=MySQL
```

#### Notice

1. To test Oracle, add an Oracle driver dependency to pom.xml.
1. In order to ensure the integrity and legibility of the test data, 10 database shards and 10 table shards are used in the sharding of the integration testing, which takes a long time to run the test cases completely.