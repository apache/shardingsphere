# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Need attention**

- *Please do not use `dev` branch to run your example, example of `dev` branch is not released yet.*

- *The manual schema initial script is in `https://github.com/apache/shardingsphere-example/blob/dev/src/resources/manual_schema.sql`, please execute it before you first run the example.*

- *Please make sure primary-replica replication data sync on MySQL is running correctly. Otherwise this example will query empty data from the replica.*

## Before start the example if you want use `dev` branch

Please make sure some dependencies from [shardingsphere](https://github.com/apache/shardingsphere) has been installed since some examples depend on that.
if you are a newbie for shardingsphere, you could prepare the dependencies as following: 

1.download and install [shardingsphere](https://github.com/apache/shardingsphere): 

```bash
## download the code of shardingsphere
git clone https://github.com/apache/shardingsphere.git

## install the dependencies
cd shardingsphere/examples
mvn clean install -Prelease
```

## shardingsphere-example module design

### project structure

```
shardingsphere-example
  ├── example-core
  │   ├── config-utility
  │   ├── example-api
  │   ├── example-raw-jdbc
  │   ├── example-spring-jpa
  │   └── example-spring-mybatis
  ├── shardingsphere-jdbc-example
  │   ├── sharding-example
  │   │   ├── sharding-raw-jdbc-example
  │   │   ├── sharding-spring-boot-jpa-example
  │   │   ├── sharding-spring-boot-mybatis-example
  │   │   ├── sharding-spring-namespace-jpa-example
  │   │   └── sharding-spring-namespace-mybatis-example
  │   ├── governance-example
  │   │   ├── governance-raw-jdbc-example
  │   │   ├── governance-spring-boot-example
  │   │   └── governance-spring-namespace-example
  │   ├── transaction-example
  │   │   ├── transaction-2pc-xa-example
  │   │   └── transaction-base-seata-example
  │   ├── other-feature-example
  │   │   ├── hint-example
  │   │   └── encrypt-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
```

## Available Examples

| Example | Description |
|---------|-------------|
| [sharding](shardingsphere-jdbc-example/sharding-example) | show how to use sharding-table\sharding-database\primary-replica-replication with ShardingSphere-JDBC |
| [springboot jpa](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-jpa-example) | show how to use SpringBoot JPA with ShardingSphere |
| [springboot mybatis](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example) | show how to use SpringBoot Mybatis with ShardingSphere |
| [governance](shardingsphere-jdbc-example/governance-example) | show how to use ShardingSphere governance |
| [transaction](shardingsphere-jdbc-example/transaction-example) | show how to use ShardingSphere transaction |
| [hint](shardingsphere-jdbc-example/other-feature-example/hint-example) | show how to use ShardingSphere hint |
| [encryption](shardingsphere-jdbc-example/other-feature-example/encrypt-example) | show how to use ShardingSphere encryption |
| APM(Pending) | show how to use APM in ShardingSphere |
| proxy(Pending) | show how to use sharding proxy |
| [docker](./docker/docker-compose.md) | show how to use docker to setup the environment for ShardingSphere |
