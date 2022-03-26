# 自动生成代码使用示例

shardingsphere-example-generator 是基于 freemarker 模板引擎的示例代码生成模块，数据 + 模板即可生成所需要的代码示例。

## 模板

shardingsphere-example-generator 已经定义好了相关模板，用户在使用时，不需要关注，相关模板在 resources 目录下的 template 文件夹，若有错误，也请指正。

## 数据

数据是模板生成的核心，基于数据，为我们的模板注入灵魂，生成运行的代码，具体的数据模型如下：

| 属性名         | 含义        | 参考值                                                                                                                             |
|:------------|-----------|:--------------------------------------------------------------------------------------------------------------------------------|
| product     | 所属产品      | jdbc、proxy                                                                                                                      |
| mode        | 运行模式      | memory、cluster-zookeeper、cluster-etcd、standalone-file                                                                           |
| transaction | 事务类型      | local                                                                                                                           |
| features    | 功能特性      | sharding、readwrite-splitting、encrypt、db-discovery                                                                               |
| frameworks  | 框架        | jdbc、spring-boot-starter-jdbc、spring-boot-starter-jpa、spring-boot-starter-mybatis、spring-namespace-jpa、spring-namespace-mybatis |
| host        | 数据库主机名    | localhost                                                                                                                       |
| port        | 数据库端口     | 3306                                                                                                                            |
| username    | 数据库用户名    | root                                                                                                                            |
| password    | 数据库密码     | root                                                                                                                            |

## 生成步骤

1、配置数据模型

在`shardingsphere-example-generator`模块的资源目录中，配置`data-model.yaml` 文件

2、生成代码

运行`ExampleGeneratorMain`即可生成对应的模块代码在`target/generated-sources/shardingsphere-${product}-sample`目录

3、编译

将生成的代码标记位maven工程，并且在相关目录文件下，运行`mvn clean compile`

4、运行

运行生成模块的 java 目录`Example#main`方法，即可运行相关的代码示例。
