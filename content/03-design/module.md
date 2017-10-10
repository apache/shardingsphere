+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "模块结构说明"
weight = 3
prev = "/03-design/architecture/"
next = "/03-design/roadmap/"

+++

```
sharding-jdbc
    ├──sharding-jdbc-core                                    分库分表、读写分离核心模块，可直接使用
    ├──sharding-jdbc-spring                                  Spring配置父模块，不应直接使用
    ├      ├──sharding-jdbc-spring-namespace                 Spring命名空间支持模块，可直接使用
    ├      ├──sharding-jdbc-spring-boot                      SpringBoot支持模块，可直接使用
    ├──sharding-jdbc-orchestration                           数据库服务编排治理模块，可接使用
    ├──sharding-jdbc-transaction-parent                      柔性事务父模块，不应直接使用
    ├      ├──sharding-jdbc-transaction                      柔性事务核心模块，可直接使用
    ├      ├──sharding-jdbc-transaction-storage              柔性事务存储模块，不应直接使用
    ├      ├──sharding-jdbc-transaction-async-job            柔性事务异步作业，不应直接使用，直接下载tar包配置启动即可
    ├──sharding-jdbc-plugin                                  插件模块，目前包含自定义分布式自增主键，可直接使用

sharding-jdbc-example                                        使用示例
    ├──sharding-jdbc-orchestration-example                   数据库服务编排治理的使用示例
    ├──sharding-jdbc-raw-jdbc-example                        基于原生JDBC的使用示例
    ├      ├──sharding-jdbc-raw-jdbc-java-example            基于Java配置的原生JDBC的使用示例
    ├      ├──sharding-jdbc-raw-jdbc-yaml-example            基于Yaml配置的原生JDBC的使用示例
    ├──sharding-jdbc-spring-example                          基于Spring的使用示例
    ├      ├──sharding-jdbc-spring-namespace-jpa-example     基于Spring的JPA使用示例
    ├      ├──sharding-jdbc-spring-namespace-mybatis-example 基于Spring的Mybatis使用示例
    ├──sharding-jdbc-spring-boot-example                     基于SpringBoot的使用示例
    ├      ├──sharding-jdbc-spring-namespace-jpa-example     基于Spring Boot Data JPA的使用示例
    ├      ├──sharding-jdbc-spring-namespace-mybatis-example 基于Spring Boot Data Mybatis的使用示例
    ├──sharding-jdbc-transaction-example                     柔性事务的使用示例

sharding-jdbc-doc                                            文档md源码模块，不应直接使用，直接阅读官网即可
```
