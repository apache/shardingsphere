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

Database types currently support MySQL, PostgreSQL, SQLServer, and Oracle, and test cases can be executed using ShardingSphere-JDBC or ShardingSphere-Proxy.

Scenarios are used to test the supporting rules of ShardingSphere. Currently, data sharding, data encrypt, data mask and read/write splitting and other related scenarios are supported, and the combination of scenarios will be improved continuously in the future.

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

Module path：`test/e2e/sql`

### Test case configuration

SQL test case is in `resources/cases/${SQL-TYPE}/e2e-${SQL-TYPE}-${cases-description}.xml`.

The case file format is as follows:

```xml
<e2e-test-cases>
    <test-case sql="${SQL}">
        <!-- select case -->
        <assertion parameters="${value_1}:${type_1}, ${value_2}:${type_2}" expected-data-source-name="{datasource-name}" />
        <!-- not select case -->
        <assertion parameters="${value_1}:${type_1}, ${value_2}:${type_2}" expected-data-file="${dataset_file_1}.xml" />
        <!-- ... more assertions -->
    </test-case>

    <test-case sql="${SQL}">
        <assertion parameters="${value_1}:${type_1}, ${value_2}:${type_2}" expected-data-file="${dataset_file_1}.xml" />
        <!-- ... more assertions -->
        <assertion parameters="${value_3}:${type_3}, ${value_4}:${type_4}" expected-data-file="${dataset_file_2}.xml" />
    </test-case>

    <!-- ... more test cases -->
</e2e-test-cases>
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

Modify `e2e.run.type` in `src/test/resources/env/e2e-env.properties` file of `e2e-sql` module to `NATIVE` mode, and then modify the following properties to the local database address and account.

```properties
e2e.native.database.host=127.0.0.1
e2e.native.database.port=3306
e2e.native.database.username=root
e2e.native.database.password=123456
```

After the modification is completed, you can adjust other properties in `e2e-env.properties` to test ShardingSphere's Proxy, JDBC access terminal, or test the stand-alone and cluster modes.

#### Docker environment configuration

Modify `e2e.run.type` in the `src/test/resources/env/e2e-env.properties` file of the `e2e-sql` module to `DOCKER` mode.
If you perform a Proxy access end test, you need to execute the following command to package the Proxy image.

```bash
./mvnw -B clean install -am -pl test/e2e/sql -Pit.env.docker -DskipTests -Dspotless.apply.skip=true -Drat.skip=true
```

If it is a Mac platform M series chip, before packaging the Proxy image, you need to execute the following command first, and then package the Proxy image.

```bash
# Install socat
brew install socat
socat TCP-LISTEN:2375,reuseaddr,fork UNIX-CLIENT:/var/run/docker.sock

# Execute in the terminal where the image is created
export DOCKER_HOST=tcp://127.0.0.1:2375
```

After the modification is completed, you can adjust other properties in `e2e-env.properties` to test ShardingSphere's Proxy, JDBC access terminal, or test the stand-alone and cluster modes.

### Run the test engine

#### Configure the running environment of the test engine

Control the test engine by configuring `src/test/resources/env/e2e-env.properties`.

All attribute values can be dynamically injected via Maven command line `-D`.

```properties

# Scenario type. Multiple values can be separated by commas. Optional values: db, tbl, dbtbl_with_replica_query, replica_query
e2e.scenarios=db,tbl,dbtbl_with_replica_query,replica_query

# Whether to run additional test cases
e2e.run.additional.cases=false

# Whether to run smoke test
e2e.run.smoke.cases=false

# Configure the environment type. Only one value is supported. Optional value: DOCKER, NATIVE
e2e.run.type=DOCKER

# Access port types to be tested. Multiple values can be separated by commas. Optional value: jdbc, proxy. The default value: jdbc
e2e.artifact.adapters=jdbc

# Database type. Multiple values can be separated by commas. Optional value: H2, MySQL, PostgreSQL, openGauss
e2e.artifact.databases=H2,MySQL,PostgreSQL,openGauss

# The docker image version of the database
e2e.docker.database.mysql.images=mysql:8.2.0

# Database connection information and account in NATIVE mode
e2e.native.database.host=127.0.0.1
e2e.native.database.port=3306
e2e.native.database.username=root
e2e.native.database.password=123456
```

#### Run debugging mode

  - Standard test engine
    Run `org.apache.shardingsphere.test.e2e.it.sql.${SQL-TYPE}.General${SQL-TYPE}E2EIT` to start the test engines of different SQL types.

  - Batch test engine
    Run `org.apache.shardingsphere.test.e2e.it.sql.dml.BatchDMLE2EIT` to start the batch test engine for the test `addBatch()` provided for DML statements.

  - Additional test engine
    Run `org.apache.shardingsphere.test.e2e.it.sql.${SQL-TYPE}.Additional${SQL-TYPE}E2EIT` to start the test engine with more JDBC method calls.
    Additional test engines need to be enabled by setting `e2e.run.additional.cases=true`.

#### Run Docker mode

```bash
./mvnw -B clean install -f test/e2e/pom.xml -Pit.env.docker -De2e.artifact.adapters=proxy,jdbc -De2e.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -De2e.artifact.databases=MySQL
```
Run the above command to build a Docker mirror `apache/shardingsphere-proxy-test:latest` used for integration testing.
If you only modify the test code, you can reuse the existing test mirror without rebuilding it. Skip the mirror building and run the integration testing directly with the following command:

```bash
./mvnw -B clean install -f test/e2e/sql/pom.xml -Pit.env.docker -De2e.artifact.adapters=proxy,jdbc -De2e.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -De2e.artifact.databases=MySQL
```

#### Remote debug Proxy code in Docker container
First of all, you need to modify the configuration file e2e-env.properties, set function.it.env.type to `docker`, and then set the corresponding database image version like `transaction.it.docker.mysql.version=mysql:5.7`.
Then generate the test image through the command, for example:

```bash
# for operation, replace ${operation} with transaction、pipeline or showprocesslist
./mvnw -B clean install -am -pl test/e2e/operation/${operation} -Pit.env.docker -DskipTests

# for e2e sql
./mvnw -B clean install -am -pl test/e2e/sql -Pit.env.docker -DskipTests -Dspotless.apply.skip=true
```

##### Remote debug Proxy started by docker image
E2E Test Proxy image opens the 3308 port by default for remote debugging of the instance in the container.
Use the following method to connect and debug the Proxy code in the container with IDE tools such as IDEA:

IDEA -> Run -> Edit Configurations -> Add New Configuration -> Remote JVM Debug

Edit the corresponding information:
  - Name: A descriptive name, such as e2e-debug.
  - Host: A IP that can access docker, such as 127.0.0.1
  - Port: debugging port(will set in next step).
  - use module classpath: The root directory of the project shardingsphere.

After editing the above information, run Run -> Run -> e2e-debug in IDEA to start the remote debug of IDEA.

##### Remote debug Proxy started by Testcontainer
> Note: If the Proxy container is started by Testcontainer, because the 3308 port is not exposed before Testcontainer starts, it cannot be debugged by the `Remote debug Proxy started by docker image` method.
Debug Testcontainer started Proxy container by the following method:
  - Set a breakpoint in the relevant startup class of Testcontainer, for example, after the line `containerComposer.start();` in E2EContainerComposer in the suite test, at this time, the relevant containers must have been started.
  - Access breakpoint debugging mode through shortcut key Alt + F8, and get mapped port by `docker ps` for the 3308 mapping of the Proxy object under the containerComposer (the external mapping port of Testcontainer is random).
  - See the `Remote debug Proxy started by docker image` method, set the Name, Host, Port, and use the port got in previous step, e.g. 51837.

After editing the above information, run Run -> Run -> e2e-debug -> debug in IDEA to start the remote debug of IDEA.

#### Notice

1. To test Oracle, add an Oracle driver dependency to pom.xml.
2. In order to ensure the integrity and legibility of the test data, 10 database shards and 10 table shards are used in the sharding of the integration testing, which takes a long time to run the test cases completely.
