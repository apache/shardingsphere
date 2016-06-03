+++
date = "2016-01-28T16:14:21+08:00"
title = "目录结构说明"
weight = 7
+++

# 目录结构说明

```
sharding-jdbc
    ├──sharding-jdbc-core                               分库分表核心模块，可直接使用
    ├──sharding-jdbc-config-parent                      配置父模块，不应直接使用
    ├      ├──sharding-jdbc-config-common               配置公共模块，不应直接使用
    ├      ├──sharding-jdbc-config-spring               Spring命名空间支持模块，可直接使用
    ├      ├──sharding-jdbc-config-yaml                 Yaml配置，可直接使用
    ├──sharding-jdbc-transaction-parent                 柔性事务父模块，不应直接使用
    ├      ├──sharding-jdbc-transaction                 柔性事务核心模块，可直接使用
    ├      ├──sharding-jdbc-transaction-storage         柔性事务存储模块，不应直接使用
    ├      ├──sharding-jdbc-transaction-async-job       柔性事务异步作业，不应直接使用，直接下载tar包配置启动即可
    ├──sharding-jdbc-example                            使用示例
    ├      ├──sharding-jdbc-example-jdbc                基于JDBC的使用示例
    ├      ├──sharding-jdbc-example-jpa                 基于JPA的使用示例
    ├      ├──sharding-jdbc-example-mybatis             基于MyBatis的使用示例
    ├      ├──sharding-jdbc-example-config-spring       基于Spring命名空间配置的使用示例
    ├      ├──sharding-jdbc-example-config-yaml         基于Yaml配置的使用示例
    ├      ├──sharding-jdbc-example-jdbc-transaction    柔性事务的使用示例
    ├──sharding-jdbc-doc                                markdown生成文档的项目，使用方无需关注
```
