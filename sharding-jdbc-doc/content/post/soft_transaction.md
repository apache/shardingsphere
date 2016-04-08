+++
date = "2016-01-08T16:14:21+08:00"
title = "柔性事务"
weight = 6
+++
# 最大努力送达型

## 概念
在分布式数据库的场景下，相信对于该数据库的操作最终一定可以成功，所以通过最大努力反复尝试送达操作。

## 架构图
![最大努力送达型事务](../../img/architecture-soft-transaction-bed.png)

## 使用场景

* 根据主键删除数据。
* 更新记录永久状态，如更新通知送达状态。

## 使用限制
使用最大努力送达型柔性事务的`SQL`需要满足幂等性。

* INSERT语句要求必须包含主键，且不能是自增主键。
* UPDATE语句要求幂等，不能是`UPDATE xxx SET x=x+1`
* DELETE语句无要求。

## 开发指南
* `Sharding-JDBC-trnasaction`完全基于`java`开发，直接提供`jar`包，可直接使用maven导入坐标即可使用。
* 由于柔性事务采用异步尝试，需要部署独立的作业和`Zookeeper`。`Sharding-JDBC-trnasaction`采用`elastic-job`实现的`Sharding-JDBC-trnasaction-async-job`，通过简单配置即可启动高可用作业异步送达柔性事务。作业可自行打包，并通过`main`方法启动。(目前独立作业的配置部分还在开发中)
* 为了便于开发，`Sharding-JDBC-trnasaction`提供了内嵌异步作业，配置方法可参见事务管理器工厂配置项。
* 为了保证事务不丢失，`Sharding-JDBC-trnasaction`需要提供数据库存储事务日志，配置方法可参见事务管理器工厂配置项。
* 为了便于开发，`Sharding-JDBC-trnasaction`提供了基于内存的事务日志存储器，配置方法可参见事务管理器工厂配置项。

## 开发步骤
1. 配置`SoftTransactionConfiguration`
```java
    SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(dataSource);
    transactionConfig.setXXX();
```
2. 初始化`SoftTransactionManagerFactory`
```java
    SoftTransactionManagerFactory transactionManagerFactory = new SoftTransactionManagerFactory(transactionConfig);
    transactionManagerFactory.init();
```

3. 获取`BEDSoftTransactionManager`
```java
    BEDSoftTransactionManager transactionManager = (BEDSoftTransactionManager) transactionManagerFactory.getTransactionManager(SoftTransactionType.BestEffortsDelivery);
```

4. 开启事务
```java
    transactionManager.begin(connection);
```

5. 执行`JDBC`

6. 关闭事务
```java
    transactionManager.end();
```

## 部署指南
* 部署用于存储事务日志的数据库。
* 部署用于异步作业使用的`Zookeeper`。
* 按照正常`java`项目发布引用`Sharding-JDBC`的`jar`。
* 打包并通过`main`方法启动`Sharding-JDBC-trnasaction-async-job`。

## 事务管理器工厂配置项

### `SoftTransactionConfiguration`配置
| 名称                                | 类型                                       | 默认值     | 功能                                                             |
| ---------------------------------- | ------------------------------------------ | --------- | ---------------------------------------------------------------- |
| syncMaxDeliveryTryTimes            | int                                        | 3         | 同步的事务送达的最大尝试次数                                         |
| asyncMaxDeliveryTryTimes           | int                                        | 3         | 异步的事务送达的最大尝试次数                                         |
| asyncMaxDeliveryTryDelayMillis     | long                                       | 60000     | 执行异步送达事务的延迟毫秒数。早于此间隔时间的入库事务才会被异步作业执行   |
| storageType                        | enum                                       | DATABASE  | 事务日志存储类型。可选值: DATABASE, MEMORY。使用DATABASE类型将自动建表 |
| transactionLogDataSource           | DataSource                                 | null      | 存储事务日志的数据源                                                |
| nestedJob                          | boolean                                    | false     | 是否使用内嵌的作业处理异步事务送达                                    |
| bestEffortsDeliveryJobConfiguration| AbstractBestEffortsDeliveryJobConfiguration| null      | 最大努力送达型异步作业配置对象                                       |

### `BestEffortsDeliveryJobConfiguration`配置
用于配置异步送达作业。使用`elastic-job`实现的高可用弹性作业，可直接用于生产环境。如果`SoftTransactionConfiguration`的`nestedJob`为`false`，请使用此对象配置异步作业。

| 名称                                | 类型                        | 默认值     | 功能                                    |
| ---------------------------------- | --------------------------- | --------- | -------------------------------------- |
| zookeeperConnectionString          | String                      | null      | 注册中心的连接地址                        |


### `NestedBestEffortsDeliveryJobConfiguration`配置
用于配置内嵌的异步作业，仅用于开发环境。生产环境应使用独立部署的作业版本。如果`SoftTransactionConfiguration`的`nestedJob`为`true`，请使用此对象配置异步作业。

| 名称                                | 类型                        | 默认值                    | 功能                                    |
| ---------------------------------- | --------------------------- | ------------------------ | -------------------------------------- |
| zookeeperPort                      | int                         | 4181                     | 内嵌的注册中心端口号                      |
| zookeeperDataDir                   | String                      | target/test_zk_data/nano/| 异步的事务送达的最大尝试次数               |


# TCC型
开发中...