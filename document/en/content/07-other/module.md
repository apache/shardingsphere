+++
toc = true
title = "Module structure"
weight = 2
prev = "/07-other/faq/"
next = "/07-other/stress-test/"
+++

You can find source code from Sharding-JDBC [official repo](https://github.com/shardingjdbc) on github.

```
sharding-jdbc
    ├──sharding-jdbc-core                                                     core module of Data sharding
    ├──sharding-jdbc-core-spring                                              spring parent module
    ├      ├──sharding-jdbc-core-spring-namespace                             spring namespace module
    ├      ├──sharding-jdbc-core-spring-boot                                  spring boot starter module
    ├──sharding-jdbc-orchestration                                            orchestration module
    ├──sharding-jdbc-orchestration-spring                                     orchestration for spring parent module
    ├      ├──sharding-jdbc-orchestration-core-spring-namespace               orchestration for spring namespace module
    ├      ├──sharding-jdbc-orchestration-core-spring-boot                    orchestration for spring boot starter module
    ├──sharding-proxy                                                   database proxy server module
    ├──sharding-jdbc-transaction-parent                                       BASE transaction parent module
    ├      ├──sharding-jdbc-transaction                                       BASE transaction core module
    ├      ├──sharding-jdbc-transaction-storage                               BASE transaction storage module
    ├      ├──sharding-jdbc-transaction-async-job                             BASE transaction async job module
    ├──sharding-jdbc-plugin                                                   plugin module, enable developers to provide customized plugins

sharding-jdbc-example                                                         example for Sharding-JDBC
    ├──sharding-jdbc-raw-jdbc-example                                         example for using raw JDBC
    ├      ├──sharding-jdbc-raw-jdbc-java-example                             example for using raw JDBC with java configuration
    ├      ├──sharding-jdbc-raw-jdbc-yaml-example                             example for using raw JDBC with yaml configuration
    ├──sharding-jdbc-spring-example                                           example for using spring framework
    ├      ├──sharding-jdbc-spring-namespace-jpa-example                      example for using spring framework with JPA
    ├      ├──sharding-jdbc-spring-namespace-mybatis-example                  example for using spring framework with MyBatis
    ├──sharding-jdbc-spring-boot-example                                      example for using spring boot
    ├      ├──sharding-jdbc-spring-boot-data-jpa-example                      example for using spring boot with JPA
    ├      ├──sharding-jdbc-spring-boot-data-mybatis-example                  example for using spring boot with MyBatis
    ├──sharding-jdbc-orchestration-example                                    example for orchestration
    ├      ├──sharding-jdbc-orchestration-zookeeper-java-example              example for orchestration using java configuration with zookeeper
    ├      ├──sharding-jdbc-orchestration-etcd-java-example                   example for orchestration using java configuration with etcd
    ├      ├──sharding-jdbc-orchestration-zookeeper-yaml-example              example for orchestration using yaml configuration with zookeeper
    ├      ├──sharding-jdbc-orchestration-etcd-yaml-example                   example for orchestration using yaml configuration with etcd
    ├──sharding-jdbc-orchestration-spring-example                             example for orchestration using spring framework
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-boot-example       example for orchestration using spring boot with zookeeper
    ├      ├──sharding-jdbc-orchestration-etcd-spring-boot-example            example for orchestration using spring boot with etcd
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-namespace-example  example for orchestration using spring namespace with zookeeper
    ├      ├──sharding-jdbc-orchestration-etcd-spring-namespace-example       example for orchestration using spring namespace with etcd
    ├──sharding-jdbc-transaction-example                                      example for BASE transaction

sharding-jdbc-opentracing                                                     plugin for integrate with open tracing

sharding-jdbc-doc                                                             Source code of homepage and documents
```
