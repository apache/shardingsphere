# ShardingSphere 使用示例代码生成器

项目基于 freemarker 模板引擎生成 ShardingSphere 使用示例代码。

## 使用步骤

1. 配置参数

文件位置：`src/main/resources/config.yaml` 

2. 生成代码

运行 `org.apache.shardingsphere.example.generator.ExampleGeneratorMain` 即可生成对应的示例代码。

生成后的代码位于：`target/generated-sources/shardingsphere-${product}-sample`

## 配置项说明

| *属性名称*    | *说明*      | *可选项*                                                                                                                           |
|:----------- | ---------- |:--------------------------------------------------------------------------------------------------------------------------------|
| product     | 产品        | jdbc、proxy                                                                                                                      |
| mode        | 运行模式     | cluster-zookeeper、cluster-etcd、standalone                                                                                       |
| transaction | 事务类型     | local, xa-atomikos, xa-narayana                                                                                                                         |
| features    | 功能        | sharding、readwrite-splitting、db-discovery、encrypt                                                                               |
| frameworks  | 框架        | jdbc、spring-boot-starter-jdbc、spring-boot-starter-jpa、spring-boot-starter-mybatis、spring-namespace-jpa、spring-namespace-mybatis |
| host        | 数据库主机名 |                                                                                                                                 |
| port        | 数据库端口   |                                                                                                                                 |
| username    | 数据库用户名  |                                                                                                                                 |
| password    | 数据库密码    |                                                                                                                                 |
