# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Notices**

- *The `shardingsphere-sample` module is a brand new sample experience module. It is in the process of development and improvement. At present, the correctness and stability of the sample cannot be guaranteed. Please ignore it for now.*

- *Please execute [initial script](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql) before you first run the example if using manual mode.*

- *Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise, primary-replica example will query empty data from the replica.*

## Using `master` branch

Please make sure some dependencies from [Apache ShardingSphere](https://github.com/apache/shardingsphere) has been installed since examples depend on that.
if you are a newbie for Apache ShardingSphere, you could prepare the dependencies as following: 

1. download and install [Apache ShardingSphere](https://github.com/apache/shardingsphere): 

```bash
## download source code
git clone https://github.com/apache/shardingsphere.git

## compile source code
cd shardingsphere
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
  ├── other-example
  │   └── shardingsphere-parser-example
  ├── shardingsphere-jdbc-example
  │   ├── mixed-feature-example
  │   │   └── sharding-readwrite-splitting-example
  │   │   │   ├── sharding-readwrite-splitting-raw-jdbc-example
  │   │   │   ├── sharding-readwrite-splitting-spring-boot-jpa-example
  │   │   │   ├── sharding-readwrite-splitting-spring-boot-mybatis-example
  │   │   │   ├── sharding-readwrite-splitting-spring-namespace-jpa-example
  │   │   │   └── sharding-readwrite-splitting-spring-namespace-mybatis-example
  │   └── single-feature-example
  │   │   ├── cluster-mode-example
  │   │   │   ├── cluster-mode-raw-jdbc-example
  │   │   │   ├── cluster-mode-spring-boot-mybatis-example
  │   │   │   └── cluster-mode-spring-namespace-mybatis-example
  │   │   ├── encrypt-example
  │   │   │   ├── encrypt-raw-jdbc-example
  │   │   │   ├── encrypt-spring-boot-mybatis-example
  │   │   │   └── encrypt-spring-namespace-mybatis-example
  │   │   ├── extension-example
  │   │   │   └── custom-sharding-algortihm-example
  │   │   │   │   ├── class-based-sharding-algorithm-example
  │   │   │   │   └── spi-based-sharding-algorithm-example
  │   │   ├── readwrite-splitting-example
  │   │   │   ├── readwrite-splitting-raw-jdbc-example
  │   │   │   ├── readwrite-splitting-spring-boot-jpa-example
  │   │   │   ├── readwrite-splitting-spring-boot-mybatis-example
  │   │   │   ├── readwrite-splitting-spring-namespace-jpa-example
  │   │   │   └── readwrite-splitting-spring-namespace-mybatis-example
  │   │   ├── shadow-example
  │   │   │   ├── shadow-raw-jdbc-example
  │   │   │   ├── shadow-spring-boot-mybatis-example
  │   │   │   └── shadow-spring-namespace-mybatis-example
  │   │   ├── sharding-example
  │   │   │   ├── sharding-raw-jdbc-example
  │   │   │   ├── sharding-spring-boot-jpa-example
  │   │   │   ├── sharding-spring-boot-mybatis-example
  │   │   │   ├── sharding-spring-namespace-jpa-example
  │   │   │   └── sharding-spring-namespace-mybatis-example
  │   │   └── transaction-example
  │   │   │   ├── transaction-2pc-xa-atomikos-raw-jdbc-example
  │   │   │   ├── transaction-2pc-xa-bitronix-raw-jdbc-example
  │   │   │   ├── transaction-2pc-xa-narayana-raw-jdbc-example
  │   │   │   ├── transaction-2pc-xa-spring-boot-example
  │   │   │   ├── transaction-2pc-xa-spring-namespace-example
  │   │   │   ├── transaction-base-seata-raw-jdbc-example
  │   │   │   └── transaction-base-seata-spring-boot-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   ├── shardingsphere-proxy-distsql-example
  │   └── shardingsphere-proxy-hint-example
  ├── shardingsphere-sample
  │   ├── shardingsphere-example-generator
  └── src/resources
        └── manual_schema.sql
```

## Available Examples

| Example                                                                                                 | Description                                                                  　　　　　　　　　　　　　　           |
|---------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| [sharding](shardingsphere-jdbc-example/single-feature-example/sharding-example)                                                | show how to use table sharding\database sharding with ShardingSphere-JDBC               |
| [readwrite-splitting](shardingsphere-jdbc-example/mixed-feature-example/sharding-readwrite-splitting-example)                  | show how to use ShardingSphere-JDBC readwrite-splitting                                 |
| [springboot jpa](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-spring-boot-jpa-example)         | show how to use SpringBoot JPA with ShardingSphere-JDBC                                 |
| [springboot mybatis](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-spring-boot-mybatis-example) | show how to use SpringBoot Mybatis with ShardingSphere-JDBC                             |
| [governance](shardingsphere-jdbc-example/single-feature-example/cluster-mode-example)                                          | show how to use ShardingSphere-JDBC governance                                          |
| [transaction](shardingsphere-jdbc-example/single-feature-example/transaction-example)                                          | show how to use ShardingSphere-JDBC transaction                                         |
| [hint](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-raw-jdbc-example)                          | show how to use ShardingSphere-JDBC hint                                                |
| [encryption](shardingsphere-jdbc-example/single-feature-example/encrypt-example)                        | show how to use ShardingSphere-JDBC encryption                                          |
| [DistSQL](shardingsphere-proxy-example/shardingsphere-proxy-distsql-example)                            | show how to use DistSQL in ShardingSphere-Proxy                   |
| APM(Pending)                                                                                            | show how to use APM in ShardingSphere                                                   |
| proxy(Pending)                                                                                          | show how to use ShardingSphere-Proxy                                                    |
| [docker](./docker/docker-compose.md)                                                                    | show how to use docker to setup the environment for ShardingSphere                      |
