+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "目录结构"
weight = 2
prev = "/01-overview/intro/"
next = "/03-design/roadmap/"
+++

您可以在Sharding-JDBC的github[官方仓库](https://github.com/shardingjdbc)找到源码。

```
sharding-jdbc
    ├──sharding-jdbc-core                                                     分库分表、读写分离核心模块，可直接使用
    ├──sharding-jdbc-core-spring                                              Spring配置父模块，不应直接使用
    ├      ├──sharding-jdbc-core-spring-namespace                             Spring命名空间支持模块，可直接使用
    ├      ├──sharding-jdbc-core-spring-boot                                  SpringBoot支持模块，可直接使用
    ├──sharding-jdbc-orchestration                                            数据库服务编排治理模块，可接使用
    ├──sharding-jdbc-orchestration-spring                                     数据库服务编排治理的Spring父模块，不应接使用
    ├      ├──sharding-jdbc-orchestration-core-spring-namespace               数据库服务编排治理的Spring命名空间支持模块，可直接使用
    ├      ├──sharding-jdbc-orchestration-core-spring-boot                    数据库服务编排治理的SpringBoot支持模块，可直接使用
    ├──sharding-jdbc-server                                                   提供代理服务器连接数据库的模块，可直接使用
    ├──sharding-jdbc-transaction-parent                                       柔性事务父模块，不应直接使用
    ├      ├──sharding-jdbc-transaction                                       柔性事务核心模块，可直接使用
    ├      ├──sharding-jdbc-transaction-storage                               柔性事务存储模块，不应直接使用
    ├      ├──sharding-jdbc-transaction-async-job                             柔性事务异步作业，不应直接使用，直接下载tar包配置启动即可
    ├──sharding-jdbc-plugin                                                   插件模块，目前包含自定义分布式自增主键，可直接使用

sharding-jdbc-example                                                         使用示例
    ├──sharding-jdbc-raw-jdbc-example                                         原生JDBC的使用示例
    ├      ├──sharding-jdbc-raw-jdbc-java-example                             基于Java配置的原生JDBC的使用示例
    ├      ├──sharding-jdbc-raw-jdbc-yaml-example                             基于Yaml配置的原生JDBC的使用示例
    ├──sharding-jdbc-spring-example                                           Spring的使用示例
    ├      ├──sharding-jdbc-spring-namespace-jpa-example                      基于Spring的JPA使用示例
    ├      ├──sharding-jdbc-spring-namespace-mybatis-example                  基于Spring的Mybatis使用示例
    ├──sharding-jdbc-spring-boot-example                                      SpringBoot的使用示例
    ├      ├──sharding-jdbc-spring-boot-data-jpa-example                      基于Spring Boot Data JPA的使用示例
    ├      ├──sharding-jdbc-spring-boot-data-mybatis-example                  基于Spring Boot Data Mybatis的使用示例
    ├──sharding-jdbc-orchestration-example                                    数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-zookeeper-java-example              Zookeeper基于Java配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-etcd-java-example                   Etcd基于Java配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-zookeeper-yaml-example              Zookeeper基于Yaml配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-etcd-yaml-example                   Etcd基于Yaml配置的数据库服务编排治理的使用示例
    ├──sharding-jdbc-orchestration-spring-example                             数据库服务编排治理的Spring使用示例
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-boot-example       Zookeeper基于Spring Boot配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-etcd-spring-boot-example            Etcd基于Spring Boot配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-namespace-example  Zookeeper基于Spring Namespace配置的数据库服务编排治理的使用示例
    ├      ├──sharding-jdbc-orchestration-etcd-spring-namespace-example       Etcd基于Spring Namespace配置的数据库服务编排治理的使用示例
    ├──sharding-jdbc-transaction-example                                      柔性事务的使用示例

sharding-jdbc-opentracing                                                     提供与Opentracing适配插件的源码

sharding-jdbc-doc                                                             官方网站和文档的源码，不应直接使用，直接阅读官网即可
```
