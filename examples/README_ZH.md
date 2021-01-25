# ShardingSphere示例

1.x的示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x或3.x或4.x的示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**需要注意**

- *请不要使用`dev`分支来运行您的示例，`dev`分支的示例尚未发布。*

- *手动模式初始脚本位于 `https://github.com/apache/shardingsphere-example/blob/dev/src/resources/manual_schema.sql`，请在首次运行示例之前执行它。*

- *请确保MySQL上的主从数据同步正确运行。否则，此示例查询从库数据将是空。*

## 如果要使用`dev`分支，请在开始该example之前

请确保已安装了来自[shardingsphere](https://github.com/apache/shardingsphere) 的某些依赖项，因为某些示例依赖于此。如果您是shardingsphere的新手，您可以准备如下依赖：

1.下载并安装[shardingsphere](https://github.com/apache/shardingsphere)：

```bash
## 下载shardingsphere代码
git clone https://github.com/apache/shardingsphere.git

## 安装依赖
cd shardingsphere/examples
mvn clean install -Prelease
```

## shardingsphere-example模块设计

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
|---------|-------------|
| [分片](shardingsphere-jdbc-example/sharding-example) | 演示了如何通过 ShardingSphere-JDBC 进行分库、分表、主从等 |
| [springboot jpa](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-jpa-example) | 演示了如何通过 SpringBoot JPA 对接 ShardingSphere |
| [springboot mybatis](shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example) | 演示了如何通过 SpringBoot Mybatis 对接 ShardingSphere |
| [governance](shardingsphere-jdbc-example/governance-example) | 演示了如何在 ShardingSphere 中使用 governance |
| [事务](shardingsphere-jdbc-example/transaction-example) | 演示了如何在 ShardingSphere 中使用事务 |
| [hint](shardingsphere-jdbc-example/other-feature-example/hint-example) | 演示了如何在 ShardingSphere 中使用 hint |
| [加密](shardingsphere-jdbc-example/other-feature-example/encrypt-example) | 演示了如何在 ShardingSphere 中使用加密 |
| APM监控(Pending) | 演示了如何在 ShardingSphere 中使用 APM 监控 |
| proxy(Pending) | 演示了如何使用 sharding proxy |
| [docker](./docker/docker-compose.md) | 演示了如何通过 docker 创建 ShardingSphere 所依赖的环境 |
