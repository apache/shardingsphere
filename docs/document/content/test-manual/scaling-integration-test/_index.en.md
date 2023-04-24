+++
pre = "<b>6.4. </b>"
title = "Pipeline E2E Test"
weight = 4
+++

## Objectives

Verify the functional correctness of pipeline scenarios.

## Test environment type

Currently, Native and Docker are available.
1. Native : Run on developer local machine. Need to start ShardingSphere-Proxy instance (run proxy installation package or run `org.apache.shardingsphere.proxy.Bootstrap` in IDE) and database instance by developer. It could be used for local debugging.
2. Docker : Run on Docker started by Maven plugin. It could be used for GitHub Actions, and it could be used for local debugging too.

Supported databases: MySQL, PostgreSQL and openGuass.

## User guide

Module path: `test/e2e/operation/pipeline`.

### Environment setup

`${DOCKER-IMAGE}` refers to the name of a Docker mirror, such as `mysql:5.7`. `${DATABASE-TYPE}` refers to database types.

Directory: `src/test/resources/env/`
- `it-env.properties`: Environment setup configuration file.
- `${DATABASE-TYPE}/server.yaml`: ShardingSphere-Proxy configuration fi;e.
- `${DATABASE-TYPE}/initdb.sql`: Database initialization SQL file.
- `${DATABASE-TYPE}/*.cnf,*.conf`: Database configuration files.
- `common/*.xml`: DistSQL files.
- `scenario/`: SQL files for different scenarios.

### Test case

Test case example: MySQLMigrationGeneralE2EIT.
Functions included:
- Database-level migration (all tables).
- Table-level migration (any number).
- Verify migration data consistency.
- Support restart during data migration.
- Support integer primary keys during data migration.
- Support string primary keys during data migration.
- A non-administrator account can be used to migrate data.

### Running the test case

Any property of `it-env.properties` could be defined by Maven command line parameter `-D`, and its priority is higher than configuration file.

#### Native environment setup

Start ShardingSphere-Proxy, registry center (e.g. ZooKeeper) and database.
Suppose ShardingSphere-Proxy port is 3307.
Take MySQL as an example, `it-env.properties` could be configured as follows: 
```
pipeline.it.env.type=NATIVE
pipeline.it.native.database=mysql
pipeline.it.native.mysql.username=root
pipeline.it.native.mysql.password=root
pipeline.it.native.mysql.port=3306
```

Find test class and start it on IDE.

#### Docker environment setup

Refer to `.github/workflows/e2e-pipeline.yml` for more details.

Step 1: Build docker image.

```
./mvnw -B clean install -am -pl test/e2e/operation/pipeline -Pit.env.docker -DskipTests
```

Running the above command will build a Docker image `apache/shardingsphere-proxy-test:latest`.

The Docker image has port `3308` for remote debugging.

If only test code is modified, you could reuse existing docker image.

Step 2: Configure `it-env.properties`.

```
pipeline.it.env.type=DOCKER
pipeline.it.docker.mysql.version=mysql:5.7
```

Step 3: Run test cases.

Take MySQL as an example:
```
./mvnw -nsu -B install -f test/e2e/operation/pipeline/pom.xml -Dpipeline.it.env.type=docker -Dpipeline.it.docker.mysql.version=mysql:5.7
```
