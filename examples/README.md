# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Notices**

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
  │   ├── other-feature-example
  │   │   ├── hint-example
  │   │   │   └── hint-raw-jdbc-example
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
  │   └── shardingsphere-proxy-hint-example
  ├── shardingsphere-sample
  │   ├── shardingsphere-example-engine
  │   ├── shardingsphere-jdbc-sample
  │   │   └── shardingsphere-jdbc-memory-example
  │   │   │   └── shardingsphere-jdbc-memory-local-example
  │   │   │   │   └── shardingsphere-jdbc-memory-local-sharding-example
  │   │   │   │   │   ├── shardingsphere-jdbc-memory-local-sharding-jdbc-example
  │   │   │   │   │   └── shardingsphere-jdbc-memory-local-sharding-springboot-starter-jdbc-example
  │   └── shardingsphere-proxy-sample
  │   │   ├── shardingsphere-proxy-cluster-etcd-example
  │   │   │   ├── shardingsphere-proxy-cluster-etcd-base-seata-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-base-seata-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-base-seata-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-base-seata-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-base-seata-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-etcd-base-seata-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-etcd-local-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-local-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-local-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-local-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-local-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-etcd-local-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-atomikos-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-atomikos-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-atomikos-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-atomikos-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-atomikos-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-etcd-xa-atomikos-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-bitronix-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-bitronix-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-bitronix-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-bitronix-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-bitronix-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-etcd-xa-bitronix-sharding-example
  │   │   │   └── shardingsphere-proxy-cluster-etcd-xa-narayana-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-narayana-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-narayana-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-narayana-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-etcd-xa-narayana-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-etcd-xa-narayana-sharding-example
  │   │   ├── shardingsphere-proxy-cluster-zookeeper-example
  │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-base-seata-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-base-seata-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-base-seata-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-base-seata-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-base-seata-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-zookeeper-base-seata-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-local-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-local-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-local-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-local-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-local-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-zookeeper-local-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-zookeeper-xa-atomikos-sharding-example
  │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-zookeeper-xa-bitronix-sharding-example
  │   │   │   └── shardingsphere-proxy-cluster-zookeeper-xa-narayana-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-narayana-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-narayana-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-narayana-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-cluster-zookeeper-xa-narayana-shadow-example
  │   │   │   │   └── shardingsphere-proxy-cluster-zookeeper-xa-narayana-sharding-example
  │   │   ├── shardingsphere-proxy-memory-example
  │   │   │   ├── shardingsphere-proxy-memory-base-seata-example
  │   │   │   │   ├── shardingsphere-proxy-memory-base-seata-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-memory-base-seata-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-memory-base-seata-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-memory-base-seata-shadow-example
  │   │   │   │   └── shardingsphere-proxy-memory-base-seata-sharding-example
  │   │   │   ├── shardingsphere-proxy-memory-local-example
  │   │   │   │   ├── shardingsphere-proxy-memory-local-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-memory-local-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-memory-local-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-memory-local-shadow-example
  │   │   │   │   └── shardingsphere-proxy-memory-local-sharding-example
  │   │   │   ├── shardingsphere-proxy-memory-xa-atomikos-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-atomikos-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-atomikos-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-atomikos-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-atomikos-shadow-example
  │   │   │   │   └── shardingsphere-proxy-memory-xa-atomikos-sharding-example
  │   │   │   ├── shardingsphere-proxy-memory-xa-bitronix-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-bitronix-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-bitronix-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-bitronix-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-bitronix-shadow-example
  │   │   │   │   └── shardingsphere-proxy-memory-xa-bitronix-sharding-example
  │   │   │   └── shardingsphere-proxy-memory-xa-narayana-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-narayana-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-narayana-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-narayana-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-memory-xa-narayana-shadow-example
  │   │   │   │   └── shardingsphere-proxy-memory-xa-narayana-sharding-example
  │   │   └── shardingsphere-proxy-standalone-file-example
  │   │   │   ├── shardingsphere-proxy-standalone-file-base-seata-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-base-seata-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-base-seata-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-base-seata-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-base-seata-shadow-example
  │   │   │   │   └── shardingsphere-proxy-standalone-file-base-seata-sharding-example
  │   │   │   ├── shardingsphere-proxy-standalone-file-local-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-local-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-local-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-local-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-local-shadow-example
  │   │   │   │   └── shardingsphere-proxy-standalone-file-local-sharding-example
  │   │   │   ├── shardingsphere-proxy-standalone-file-xa-atomikos-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-atomikos-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-atomikos-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-atomikos-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-atomikos-shadow-example
  │   │   │   │   └── shardingsphere-proxy-standalone-file-xa-atomikos-sharding-example
  │   │   │   ├── shardingsphere-proxy-standalone-file-xa-bitronix-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-bitronix-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-bitronix-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-bitronix-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-bitronix-shadow-example
  │   │   │   │   └── shardingsphere-proxy-standalone-file-xa-bitronix-sharding-example
  │   │   │   └── shardingsphere-proxy-standalone-file-xa-narayana-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-narayana-db-discovery-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-narayana-encrypt-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-narayana-readwrite-splitting-example
  │   │   │   │   ├── shardingsphere-proxy-standalone-file-xa-narayana-shadow-example
  │   │   │   │   └── shardingsphere-proxy-standalone-file-xa-narayana-sharding-example
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
| APM(Pending)                                                                                            | show how to use APM in ShardingSphere                                                   |
| proxy(Pending)                                                                                          | show how to use ShardingSphere-Proxy                                                    |
| [docker](./docker/docker-compose.md)                                                                    | show how to use docker to setup the environment for ShardingSphere                      |
