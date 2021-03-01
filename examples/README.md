# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Notices**

- *Please do not use `dev` branch to run your example, example of `dev` branch is not released yet.*

- *Please execute [initial script](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql) before you first run the example if using manual mode.*

- *Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise, primary-replica example will query empty data from the replica.*

## Using `dev` branch

Please make sure some dependencies from [Apache ShardingSphere](https://github.com/apache/shardingsphere) has been installed since examples depend on that.
if you are a newbie for Apache ShardingSphere, you could prepare the dependencies as following: 

1. download and install [Apache ShardingSphere](https://github.com/apache/shardingsphere): 

```bash
## download source code
git clone https://github.com/apache/shardingsphere.git

## compile source code
cd shardingsphere/examples
mvn clean install -Prelease
```

## Module design

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

| Example                                                                                                 | Description                                                                             |
|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| [sharding](shardingsphere-jdbc-example/sharding-example)                                                | show how to use table sharding\database sharding\replica-query with ShardingSphere-JDBC |
| [springboot jpa](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-jpa-example)         | show how to use SpringBoot JPA with ShardingSphere-JDBC                                 |
| [springboot mybatis](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example) | show how to use SpringBoot Mybatis with ShardingSphere-JDBC                             |
| [governance](shardingsphere-jdbc-example/governance-example)                                            | show how to use ShardingSphere-JDBC governance                                          |
| [transaction](shardingsphere-jdbc-example/transaction-example)                                          | show how to use ShardingSphere-JDBC transaction                                         |
| [hint](shardingsphere-jdbc-example/other-feature-example/hint-example)                                  | show how to use ShardingSphere-JDBC hint                                                |
| [encryption](shardingsphere-jdbc-example/other-feature-example/encrypt-example)                         | show how to use ShardingSphere-JDBC encryption                                          |
| APM(Pending)                                                                                            | show how to use APM in ShardingSphere                                                   |
| proxy(Pending)                                                                                          | show how to use ShardingSphere-Proxy                                                    |
| [docker](./docker/docker-compose.md)                                                                    | show how to use docker to setup the environment for ShardingSphere                      |
