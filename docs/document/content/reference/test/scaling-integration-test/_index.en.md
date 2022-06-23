+++
title = "Scaling Integration Test"
weight = 4
+++

## Objective

Verify the correctness of Scaling's own functionality and dependent modules.

## Environment

There are two types of environment preparation: Native and Docker, either of which requires Docker to be pre-installed locally.

- Native Environment: For local debugging, you can use the IDE's debug mode for debugging
- Docker Environment: Environment run by Maven for cloud compiled environments and testing ShardingSphere-Proxy scenarios, e.g. GitHub Action

The current default Docker environment, involving ShardingSphere-Proxy, Zookeeper, database instances (MySQL, PostgreSQL), are automatically started via Docker.

Database type currently supports MySQL, PostgreSQL, openGauss

## Guide

Module path: `shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling`

The Class distribution of the test is as follows.

Core Case：
- MySQLGeneralScalingIT: Covered the most test scenarios, including part of the table migration, most variety of table fields, etc.
- PostgreSQLGeneralScalingIT: Similar, except that the database type is PostgreSQL/openGauss and includes custom schema migration scenarios.

Primary Key Case：

- TextPrimaryKeyScalingIT: Support migration of tables with primary key of text type(e.g. UUID).

### Configuration File

Catalog：`resources/env/`
- /common: The Dist SQL used in the Scaling process.
- /{SQL-TYPE}: database-level configuration files.
- /scenario: The configuration file for the test scenario, mainly SQL, may be written differently for different databases.

### Run Test Cases

All property values can be dynamically injected by means of the Maven command line `-D`.

`${image-name}` Indicates a legal docker image name, e.g., mysql:5.7, separated by commas if multiple.
`-Dit.env.postgresql.version=${image-name}` Indicates the version of PostgreSQL that needs to be tested.
`-Dit.env.mysql.version=${image-name}` Indicates the version of MySQL that needs to be tested.

#### Native Environment Startup

Native environments require that ShardingSphere-Proxy (and its own dependent Cluster, such as Zookeeper) be started locally, and that ShardingSphere-Proxy be started on port 3307, while the database will be started according to the user's configuration, but the corresponding ports are the default ports for the database (MySQL=3306, PostgreSQL=5432).

Therefore, Native mode does not support running multiple cases, and you need to clean up the information in Zookeeper and restart ShardingSphere-Proxy after each run.

The startup method is as follows: Find the Case you need to test, such as MySQLGeneralScalingIT, and configure the corresponding VM Option before startup, add the following configuration.

```
-Dit.cluster.env.type=native -Dit.env.mysql.version=${image-name}
```

Just start it under the IDE using the Junit.

#### Docker Environment Startup

Step 1: Packaging Image

```bash
./mvnw -B clean install -am -pl shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling -Pit.env.docker -DskipTests
```

Running the above command will build a Docker image `apache/shardingsphere-proxy-test:latest` for integration testing.
If you have only modified the test code, you can reuse the existing test image without rebuilding it.

**Docker environment configuration provides remote debugging port for ShardingSphere-Proxy, the default is 3308.**
You can change it yourself in ShardingSphereProxyDockerContainer.

#### Running Case

As with Native, only one parameter needs to be changed.

```
-Dit.cluster.env.type=docker
```

You can run the use case using the same IDE as Native, or you can run it using maven.

```shell
./mvnw -nsu -B install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling/pom.xml -Dit.cluster.env.type=DOCKER -Dit.env.mysql.version=${image-name}
```

#### Attentions

The commands in the Scaling integration test are basically executed in the ShardingSphere-Proxy, so if they fail, most of them require a debug of the ShardingSphere-Proxy, and the logs prefixed with `:Scaling-Proxy` are output from the ShardingSphere-Proxy container.
