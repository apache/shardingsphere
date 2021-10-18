# Apache ShardingSphere 示例

1.x 版本示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x，3.x 或 4.x 版本示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**注意事项**

- *请不要使用 `dev` 分支来运行您的示例，`dev` 分支的示例尚未发布。*

- *如果采用手动模式，请在首次运行示例之前执行[初始化脚本](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql)。*

- *请确保 MySQL 上的主从数据同步正确运行。否则，读写分离示例查询从库数据为空。*

## 使用 `master` 分支

请在开始该示例之前，请确保已安装了来自 [Apache ShardingSphere](https://github.com/apache/shardingsphere) 的全部依赖项。
如果您是 ShardingSphere 的新手，您可以准备如下依赖：

1. 下载并安装 [Apache ShardingSphere](https://github.com/apache/shardingsphere): 

```bash
## 下载源码
git clone https://github.com/apache/shardingsphere.git

## 编译源码
cd shardingsphere
mvn clean install -Prelease
```

## 模块设计

### 项目结构

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

## 用例列表

| 例子 | 描述 |
|--------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| [分片](shardingsphere-jdbc-example/sharding-example)                                                    | 演示通过 ShardingSphere-JDBC 进行分库、分表、主从等      |
| [springboot jpa](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-jpa-example)         | 演示通过 SpringBoot JPA 对接 ShardingSphere-JDBC      |
| [springboot mybatis](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example) | 演示通过 SpringBoot Mybatis 对接 ShardingSphere-JDBC  |
| [治理](shardingsphere-jdbc-example/governance-example)                                                  | 演示在 ShardingSphere-JDBC 中使用治理                  |
| [事务](shardingsphere-jdbc-example/transaction-example)                                                 | 演示在 ShardingSphere-JDBC 中使用事务                  |
| [hint](shardingsphere-jdbc-example/other-feature-example/hint-example)                                  | 演示在 ShardingSphere-JDBC 中使用 hint                |
| [加密](shardingsphere-jdbc-example/other-feature-example/encrypt-example)                               | 演示在 ShardingSphere-JDBC 中使用加密                  |
| APM 监控(Pending)                                                                                        | 演示在 ShardingSphere 中使用 APM 监控                  |
| proxy(Pending)                                                                                          | 演示使用 ShardingSphere-Proxy                         |
| [docker](./docker/docker-compose.md)                                                                    | 演示通过 docker 创建 ShardingSphere 所依赖的环境        |
