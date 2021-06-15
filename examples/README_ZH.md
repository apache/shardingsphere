# Apache ShardingSphere 示例

1.x 版本示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x，3.x 或 4.x 版本示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**注意事项**

- *请不要使用`dev`分支来运行您的示例，`dev`分支的示例尚未发布。*

- *如果采用手动模式，请在首次运行示例之前执行[初始化脚本](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql)。*

- *请确保MySQL上的主从数据同步正确运行。否则，读写分离示例查询从库数据为空。*

## 使用 `dev` 分支

请在开始该示例之前，请确保已安装了来自 [Apache ShardingSphere](https://github.com/apache/shardingsphere) 的全部依赖项。
如果您是 ShardingSphere 的新手，您可以准备如下依赖：

1. 下载并安装 [Apache ShardingSphere](https://github.com/apache/shardingsphere): 

```bash
## 下载源码
git clone https://github.com/apache/shardingsphere.git

## 编译源码
cd shardingsphere/examples
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
| APM监控(Pending)                                                                                        | 演示在 ShardingSphere 中使用 APM 监控                  |
| proxy(Pending)                                                                                          | 演示使用 ShardingSphere-Proxy                         |
| [docker](./docker/docker-compose.md)                                                                    | 演示通过 docker 创建 ShardingSphere 所依赖的环境        |
