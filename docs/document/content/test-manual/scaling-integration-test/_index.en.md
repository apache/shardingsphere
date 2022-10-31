+++
pre = "<b>6.4. </b>"
title = "Scaling Integration Test"
weight = 4
+++

## Objectives

Verify the functional correctness of data migration and dependency modules. 

## Test environment

Currently, Native and Docker environments are supported.
1. The Native environment runs directly in the test environment provided by the developer, and users need to start ShardingSphere-Proxy and the corresponding database instance by themselves, which is suitable for debugging scenarios.
2. The Docker environment is run by Maven, which is suitable for cloud compilation environment and ShardingSphere-Proxy testing scenarios, such as GitHub Action.

Currently, you can use MySQL, PostgreSQL and openGuass databases.

## User guide

Module path: `shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling`.

### Environment setup

`${DOCKER-IMAGE}` refers to the name of a Docker mirror, such as `mysql:8`. `${DATABASE-TYPE}` refers to database types.
Directory: `src/test/resources/env`
- `it-env.properties`: the startup parameters of integration testing.
- `${DATABASE-TYPE}/server.yaml`: ShardingSphere-Proxy configuration file corresponding to the database.
- `${DATABASE-TYPE}/initdb.sql`: The database initializes SQL.
- `${DATABASE-TYPE}/*.cnf,*.conf`: Files ending with cnf or conf are database configuration files for Docker mount.
- `common/command.xml`: The DistSQL used in the test.
- `scenario/`: Store SQL in the test scenarios.

### Test case

Currently, all the test cases are directly inherited from `BaseExtraSQLITCase` and indirectly inherited from `BaseITCase`.
- `BaseITCase`: Provide generic methods for sub-class.
- `BaseExtraSQLITCase`: Provide table creation and CRUD statement execution methods.

Test case example: MySQLGeneralScalingIT.
Functions included:
- Database-level migration (all tables).
- Table-level migration (any number).
- Verify migration data consistency.
- Stop writing is supported during data migration.
- Support restart during data migration.
- Support integer primary keys during data migration.
- Support string primary keys during data migration.
- A non-administrator account can be used to migrate data.

### Running the test case

All property values of `it-env.properties` can be introduced by the Maven command line `-D`, and its priority is higher than that of the configuration file.

#### Native environment setup

The user starts ShardingSphere-Proxy locally in advance, along with dependent configuration centers (such as ZooKeeper) and databases.
The port required for ShardingSphere-Proxy is 3307.
Take MySQL as an example, `it-env.properties` can be configured as follows: 
```
scaling.it.env.type=NATIVE
scaling.it.native.database=mysql
scaling.it.native.mysql.username=root
scaling.it.native.mysql.password=root
scaling.it.native.mysql.port=3306
```

Find the appropriate test case and start it with Junit under the IDE.

#### Docker environment setup

Step 1: Package mirror.

```
./mvnw -B clean install -am -pl shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling -Pit.env.docker -DskipTests
```

Running the above command will build a Docker mirror apache/shardingsphere-proxy-test:latest used for integration testing. 
The mirror sets the port for remote debugging and the default port is 3308. If only the test code is modified, you can reuse the existing test mirror without rebuilding it. 

If you need to adjust Docker mirror startup parameters, you can modify the configuration of the ShardingSphereProxyDockerContainer file.

The output log of ShardingSphere-Proxy has the prefix Scaling-Proxy.

Use Maven to run the test cases. Take MySQL as an example:

```
./mvnw -nsu -B install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling/pom.xml -Dscaling.it.env.type=DOCKER -Dscaling.it.docker.mysql.version=${image-name}
```

You can also use IDE to run test cases. `it-env.properties` can be configured as follows: 

```
scaling.it.env.type=DOCKER
scaling.it.docker.mysql.version=mysql:5.7
```
