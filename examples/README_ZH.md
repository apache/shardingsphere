# Apache ShardingSphere 示例

1.x 版本示例，请参阅 `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

2.x，3.x 或 4.x 版本示例，请参见 `https://github.com/apache/shardingsphere-example/tree/${tag}`

**注意事项**

- *`shardingsphere-jdbc-example-generator`模块是一个全新的示例体验模块*

-
*如果采用手动模式，请在首次运行示例之前执行[初始化脚本](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql)。*

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
  ├── shardingsphere-jdbc-example-generator
  ├── shardingsphere-parser-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   ├── shardingsphere-proxy-distsql-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
```

## 用例列表

| 例子                                                                           | 描述                                   |
|------------------------------------------------------------------------------|--------------------------------------|
| [ShardingSphere-JDBC示例](shardingsphere-jdbc-example-generator/README.md)       | 通过配置生成ShardingSphere-JDBC的演示示例       |
| [DistSQL](shardingsphere-proxy-example/shardingsphere-proxy-distsql-example) | 演示在 ShardingSphere-Proxy 中使用 DistSQL |
| APM 监控(Pending)                                                              | 演示在 ShardingSphere 中使用 APM 监控        |
| proxy(Pending)                                                               | 演示使用 ShardingSphere-Proxy            |
| [docker](./docker/docker-compose.md)                                         | 演示通过 docker 创建 ShardingSphere 所依赖的环境 |
