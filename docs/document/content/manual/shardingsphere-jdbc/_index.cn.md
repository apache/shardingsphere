+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

## 简介

ShardingSphere-JDBC是ShardingSphere的第一个产品，也是ShardingSphere的前身。
它定位为轻量级Java框架，在Java的JDBC层提供的额外服务。它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

* 适用于任何基于JDBC的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
* 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
* 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer，PostgreSQL以及任何遵循SQL92标准的数据库。

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png)

## 对比

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| --------- | --------------------- | ---------------------- | ------------------------ |
| 数据库     | `任意`                | MySQL/PostgreSQL       | MySQL/PostgreSQL          |
| 连接消耗数 | `高`                  | 低                      | 高                        |
| 异构语言   | `仅Java`              | 任意                    | 任意                      |
| 性能       | `损耗低`              | 损耗略高                | 损耗低                     |
| 无中心化   | `是`                  | 否                     | 是                         |
| 静态入口   | `无`                  | 有                     | 无                         |

ShardingSphere-JDBC的优势在于对Java应用的友好度。

## 内部结构

![配置领域模型类图](https://shardingsphere.apache.org/document/current/img/config_domain.png)

#### 黄色部分

图中黄色部分表示的是ShardingSphere-JDBC的入口API，采用工厂方法的形式提供。
目前有ShardingDataSourceFactory和MasterSlaveDataSourceFactory两个工厂类。ShardingDataSourceFactory用于创建分库分表或分库分表+读写分离的JDBC驱动，MasterSlaveDataSourceFactory用于创建独立使用读写分离的JDBC驱动。

#### 蓝色部分

图中蓝色部分表示的是ShardingSphere-JDBC的配置对象，提供灵活多变的配置方式。
ShardingRuleConfiguration是分库分表配置的核心和入口，它可以包含多个TableRuleConfiguration和MasterSlaveRuleConfiguration。每一组相同规则分片的表配置一个TableRuleConfiguration。如果需要分库分表和读写分离共同使用，每一个读写分离的逻辑库配置一个MasterSlaveRuleConfiguration。
每个TableRuleConfiguration对应一个ShardingStrategyConfiguration，它有5中实现类可供选择。

仅读写分离使用MasterSlaveRuleConfiguration即可。

#### 红色部分

图中红色部分表示的是内部对象，由ShardingSphere-JDBC内部使用，应用开发者无需关注。ShardingSphere-JDBC通过ShardingRuleConfiguration和MasterSlaveRuleConfiguration生成真正供ShardingDataSource和MasterSlaveDataSource使用的规则对象。ShardingDataSource和MasterSlaveDataSource实现了DataSource接口，是JDBC的完整实现方案。

#### 初始化流程

1. 配置Configuration对象。
2. 通过Factory对象将Configuration对象转化为Rule对象。
3. 通过Factory对象将Rule对象与DataSource对象装配。
4. ShardingSphere-JDBC使用DataSource对象进行分库。

#### 使用约定

在`org.apache.shardingsphere.api`和`org.apache.shardingsphere.driver.api` 包中的类是面向用户的API，每次修改都会在release notes中明确声明。
其他包中的类属于内部实现，可能随时进行调整，`请勿直接使用`。
