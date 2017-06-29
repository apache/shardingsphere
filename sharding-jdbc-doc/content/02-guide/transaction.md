+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "事务支持"
weight = 8
prev = "/02-guide/key-generator/"
next = "/02-guide/subquery/"

+++

Sharding-JDBC由于性能方面的考量，决定不支持强一致性分布式事务。我们已明确规划线路图，未来会支持最终一致性的柔性事务。

目前最大努力送达型柔性事务已开发完成。

如果不使用柔性事务，也会自动包含弱XA事务支持，有以下几点说明：

* 完全支持非跨库事务，例如：仅分表，或分库但是路由的结果在单库中。

* 完全支持因逻辑异常导致的跨库事务。例如：同一事务中，跨两个库更新。更新完毕后，抛出空指针，则两个库的内容都能回滚。

* 不支持因网络、硬件异常导致的跨库事务。例如：同一事务中，跨两个库更新，更新完毕后、未提交之前，第一个库死机，则只有第二个库数据提交。

# 柔性事务

## 最大努力送达型

### 概念
在分布式数据库的场景下，相信对于该数据库的操作最终一定可以成功，所以通过最大努力反复尝试送达操作。

### 架构图
![最大努力送达型事务](../../img/architecture-soft-transaction-bed.png)

### 适用场景

* 根据主键删除数据。
* 更新记录永久状态，如更新通知送达状态。

### 使用限制
使用最大努力送达型柔性事务的SQL需要满足幂等性。

* INSERT语句要求必须包含主键，且不能是自增主键。
* UPDATE语句要求幂等，不能是UPDATE xxx SET x=x+1
* DELETE语句无要求。

### 开发指南
* sharding-jdbc-transaction完全基于java开发，直接提供jar包，可直接使用maven导入坐标即可使用。
* 为了保证事务不丢失，sharding-jdbc-transaction需要提供数据库存储事务日志，配置方法可参见事务管理器配置项。
* 由于柔性事务采用异步尝试，需要部署独立的作业和Zookeeper。sharding-jdbc-transaction采用elastic-job实现的sharding-jdbc-transaction-async-job，通过简单配置即可启动高可用作业异步送达柔性事务，启动脚本为start.sh。
* 为了便于开发，sharding-jdbc-transaction提供了基于内存的事务日志存储器和内嵌异步作业。

### 开发示例

```java
    // 1. 配置SoftTransactionConfiguration
    SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(dataSource);
    transactionConfig.setXXX();
    
    // 2. 初始化SoftTransactionManager
    SoftTransactionManager transactionManager = new SoftTransactionManager(transactionConfig);
    transactionManager.init();
    
    // 3. 获取BEDSoftTransaction
    BEDSoftTransaction transaction = (BEDSoftTransaction) transactionManager.getTransaction(SoftTransactionType.BestEffortsDelivery);
    
    // 4. 开启事务
    transaction.begin(connection);
    
    // 5. 执行JDBC
    /* 
        codes here
    */
    * 
    // 6.关闭事务
    transaction.end();
```

### 事务管理器配置项

### SoftTransactionConfiguration配置
用于配置事务管理器。


| *名称*                              | *类型*                                     | *必填* | *默认值*   | *说明*                                                                                       |
| ---------------------------------- | ------------------------------------------ | ------ | --------- | ------------------------------------------------------------------------------------------- |
| shardingDataSource                 | ShardingDataSource                         | 是   |           | 事务管理器管理的数据源                                                                         |
| syncMaxDeliveryTryTimes            | int                                        | 否     | 3         | 同步的事务送达的最大尝试次数                                                                    |
| storageType                        | enum                                       | 否     | RDB       | 事务日志存储类型。可选值: RDB,MEMORY。使用RDB类型将自动建表                                       |
| transactionLogDataSource           | DataSource                                 | 否     | null      | 存储事务日志的数据源，如果storageType为RDB则必填                                                 |
| bestEffortsDeliveryJobConfiguration| NestedBestEffortsDeliveryJobConfiguration  | 否     | null      | 最大努力送达型内嵌异步作业配置对象。如需使用，请参考NestedBestEffortsDeliveryJobConfiguration配置 |

### NestedBestEffortsDeliveryJobConfiguration配置 (仅开发环境)
用于配置内嵌的异步作业，仅用于开发环境。生产环境应使用独立部署的作业版本。

| *名称*                              | *类型*                       | *必填* | *默认值*                    | *说明*                                                         |
| ---------------------------------- | --------------------------- | ------ | ------------------------ | --------------------------------------------------------------- |
| zookeeperPort                      | int                         | 否     | 4181                     | 内嵌的注册中心端口号                                               |
| zookeeperDataDir                   | String                      | 否     | target/test_zk_data/nano/| 内嵌的注册中心的数据存放目录                                        |
| asyncMaxDeliveryTryTimes           | int                         | 否     | 3                        | 异步的事务送达的最大尝试次数                                        |
| asyncMaxDeliveryTryDelayMillis     | long                        | 否     | 60000                    | 执行异步送达事务的延迟毫秒数，早于此间隔时间的入库事务才会被异步作业执行  |

### 独立部署作业指南
* 部署用于存储事务日志的数据库。
* 部署用于异步作业使用的Zookeeper。
* 配置YAML文件,参照示例。
* 下载并解压文件sharding-jdbc-transaction-async-job-$VERSION.tar，通过start.sh脚本启动异步作业。

### 异步作业YAML文件配置
```yaml
#目标数据库的数据源.
targetDataSource:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password:
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:

#事务日志的数据源.
transactionLogDataSource:
  ds_trans: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trans_log
    username: root
    password:

#注册中心配置
zkConfig:
  #注册中心的连接地址
  connectionString: localhost:2181
  
  #作业的命名空间
  namespace: Best-Efforts-Delivery-Job
  
  #注册中心的等待重试的间隔时间的初始值
  baseSleepTimeMilliseconds: 1000
  
  #注册中心的等待重试的间隔时间的最大值
  maxSleepTimeMilliseconds: 3000
  
  #注册中心的最大重试次数
  maxRetries: 3

#作业配置
jobConfig:
  #作业名称
  name: bestEffortsDeliveryJob
  
  #触发作业的cron表达式
  cron: 0/5 * * * * ?
  
  #每次作业获取的事务日志最大数量
  transactionLogFetchDataCount: 100
  
  #事务送达的最大尝试次数.
  maxDeliveryTryTimes: 3
  
  #执行送达事务的延迟毫秒数,早于此间隔时间的入库事务才会被作业执行
  maxDeliveryTryDelayMillis: 60000
```

## TCC型
开发中...

