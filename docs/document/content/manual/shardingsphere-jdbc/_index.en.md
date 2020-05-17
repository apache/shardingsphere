+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

## Introduction

As the first product and the predecessor of ShardingSphere, ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. With the client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png)

## Comparison

|                        | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| ---------------------- | --------------------- | ---------------------- | ------------------------ |
| Database               | `Any`                 | MySQL/PostgreSQL       | MySQL/PostgreSQL         |
| Connections Count Cost | `More`                | Less                   | More                     |
| Supported Languages    | `Java Only`           | Any                    | Any                      |
| Performance            | `Low loss`            | Relatively High loss   | Low loss                 |
| Decentralization       | `Yes`                 | No                     | No                       |
| Static Entry           | `No`                  | Yes                    | No                       |

ShardingSphere-JDBC is suitable for java application.

## Internal Structure

![The class diagrams for Domain Model](https://shardingsphere.apache.org/document/current/img/config_domain.png)

#### Yellow Part

The yellow part in the diagram indicates the API entrance of ShardingSphere-JDBC, provided in factory method. There are two kinds of factories, `ShardingDataSourceFactory ` and `MasterSlaveDataSourceFactory` for now. `ShardingDataSourceFactory` is used to create JDBC drivers of  sharding databases with sharding tables or sharding databases with sharding tables+read-write split; `MasterSlaveDataSourceFactory` is used to create JDBC drivers of read-write split.

#### Blue Part

The blue part in the diagram indicates configuration objects of ShardingSphere-JDBC, which provides flexible configuration methods. `ShardingRuleConfiguration` is the core and entrance of sharding database and sharding table configuration, which can include many `TableRuleConfiguration` and `MasterSlaveRuleConfiguration`. Each group of sharding tables with same rules is set with a `TableRuleConfiguration`. If sharding databases with sharding tables and read-write split are used together, each logic database of read-write split is set with a  `MasterSlaveRuleConfiguration`. Each `TableRuleConfiguration` is corresponding to a `ShardingStrategyConfiguration` and it has 5 types to choose.

Read-write split uses `MasterSlaveRuleConfiguration`.

#### Red Part

The red part in the diagram indicates internal objects, which are used in ShardingSphere-JDBC and needless to be focused by application developers. ShardingSphere-JDBC uses `ShardingRuleConfiguration` and `MasterSlaveRuleConfiguration` to generate rule objects used by  `ShardingDataSource` and `MasterSlaveDataSource`. Implemented with `DataSource` interface, `ShardingDataSource` and `MasterSlaveDataSource` are complete implementation schemes of JDBC.

#### Initialization Process

1. Configure `Configuration` objects.
2. Transfer `Configuration` objects to `Rule` objects through `Factory` objects.
3. Equip `Rule` objects and `DataSource` objects through `Factory` objects.
4. ShardingSphere-JDBC uses `DataSource` objects to split databases.

#### Use Convention

Classes in `org.apache.shardingsphere.api` and `org.apache.shardingsphere.driver.api` packages are API open to users, the modifications of which will be declared in release notes. As internal implementations, classes in other packages can adjust any time, `please not use them directly`.

