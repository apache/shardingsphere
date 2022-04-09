# Apache ShardingSphere 示例

1.x 版本示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x，3.x 或 4.x 版本示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**注意事项**

- *`shardingsphere-sample`模块是一个全新的示例体验模块，正在开发完善过程中，目前示例正确性以及稳定性不能够保证，请先忽略*

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

## 用例列表

| 例子 | 描述 |
|--------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| [分片](shardingsphere-jdbc-example/single-feature-example/sharding-example)                              | 演示通过 ShardingSphere-JDBC 进行分库、分表等                                  |
| [读写分离](shardingsphere-jdbc-example/mixed-feature-example/sharding-readwrite-splitting-example)        | 演示在 ShardingSphere-JDBC 中使用读写分离                                     |
| [springboot jpa](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-spring-boot-jpa-example)         | 演示通过 SpringBoot JPA 对接 ShardingSphere-JDBC      |
| [springboot mybatis](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-spring-boot-mybatis-example) | 演示通过 SpringBoot Mybatis 对接 ShardingSphere-JDBC  |
| [治理](shardingsphere-jdbc-example/single-feature-example/cluster-mode-example)                          | 演示在 ShardingSphere-JDBC 中使用治理                  |
| [事务](shardingsphere-jdbc-example/single-feature-example/transaction-example)                           | 演示在 ShardingSphere-JDBC 中使用事务                  |
| [hint](shardingsphere-jdbc-example/single-feature-example/sharding-example/sharding-raw-jdbc-example)   | 演示在 ShardingSphere-JDBC 中使用 hint                |
| [加密](shardingsphere-jdbc-example/single-feature-example/encrypt-example)                               | 演示在 ShardingSphere-JDBC 中使用加密                  |
| [DistSQL](shardingsphere-proxy-example/shardingsphere-proxy-distsql-example)                            | 演示在 ShardingSphere-Proxy 中使用 DistSQL                  |
| APM 监控(Pending)                                                                                        | 演示在 ShardingSphere 中使用 APM 监控                  |
| proxy(Pending)                                                                                          | 演示使用 ShardingSphere-Proxy                         |
| [docker](./docker/docker-compose.md)                                                                    | 演示通过 docker 创建 ShardingSphere 所依赖的环境         |
