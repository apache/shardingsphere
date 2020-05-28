# ShardingSphere示例

1.x的示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x或3.x或4.x的示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**需要注意**

- *请不要使用`dev`分支来运行您的示例，`dev`分支的示例尚未发布。*

- *手动模式初始脚本位于 `https://github.com/apache/shardingsphere-example/blob/dev/src/resources/manual_schema.sql`，请在首次运行示例之前执行它。*

- *请确保MySQL上的主从数据同步正确运行。否则，此示例查询从库数据将是空。*

## 如果要使用`dev`分支，请在开始该example之前

请确保已安装了来自[shardingsphere](https://github.com/apache/shardingsphere) 和 [shardingsphere-spi-impl](https://github.com/OpenSharding/shardingsphere-spi-impl)的某些依赖项，因为某些示例依赖于此。如果您是shardingsphere的新手，您可以准备如下依赖：

1.下载并安装[shardingsphere](https://github.com/apache/shardingsphere)：

```bash
## 下载shardingsphere代码
git clone https://github.com/apache/shardingsphere.git

## 检出一个指定版本，比如是 4.0.0-RC1
cd shardingsphere && git checkout 4.0.0-RC1

## 安装依赖
mvn clean install -Prelease
```

2.下载并安装[shardingsphere-spi-impl](https://github.com/OpenSharding/shardingsphere-spi-impl)：

```bash
## 下载shardingsphere-SPI-implement的代码
git clone https://github.com/OpenSharding/shardingsphere-spi-impl.git

## 检出一个指定版本，比如是 4.0.0-RC1
cd shardingsphere-spi-impl && git checkout 4.0.0-RC1

## 安装依赖
mvn clean install
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
  │   ├── orchestration-example
  │   │   ├── orchestration-raw-jdbc-example
  │   │   ├── orchestration-spring-boot-example
  │   │   └── orchestration-spring-namespace-example
  │   ├── transaction-example
  │   │   ├── transaction-2pc-xa-example
  │   │   └──transaction-base-seata-example
  │   ├── other-feature-example
  │   │   ├── hint-example
  │   │   └── encrypt-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
```

### 分片数据的最佳实践

* 分片数据库
* 分片表
* 分片数据库和表
* 主从
* 分片和主从

您可以从[shardingsphere-example](shardingsphere-jdbc-example/sharding-example)中获取更多详细信息

### 分片与编排的最佳实践

* 使用本地配置文件和zookeeper/etcd配置分片
* 使用注册中心（zookeeper/etcd）的配置进行分片

您可以从业务[orchestration-example](shardingsphere-jdbc-example/orchestration-example)中获取更多细节

### 分片+分布式事务的最佳实践

* 2pc-xa事务
* base-seata事务

您可以从[transaction-example](shardingsphere-jdbc-example/transaction-example)中获取更多详细信息

### 提示路由的最佳实践

您可以从[hint-example](shardingsphere-jdbc-example/other-feature-example/hint-example)中获取更多细节

### 数据加密的最佳实践

您可以从[encrypt-example](shardingsphere-jdbc-example/other-feature-example/encrypt-example)中获取更多详细信息

### APM集成的最佳实践

我们将在最近添加APM集成示例。

### 分片代理的最佳实践

我们希望最近添加一个docker基础示例。

### [如何使用docker配置ShardingSphere-JDBC和ShardingSphere-Proxy](./docker/docker-compose.md) (可选)
