+++
toc = true
title = "分布式事务"
weight = 5
+++

ShardingDataSource已经整合了分布式事务的功能，因此不需要用户进行额外的配置，每次获取ShardingConnection前，通过修改`TransactionTypeHolder`，可以对事务类型进行切换。

## XA事务

### 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### JAVA编码方式设置事务类型

 ```java
 TransactionTypeHolder.set(TransactionType.XA);
 ```

### XA事务管理器参数配置（可选）

ShardingSphere默认的XA事务管理器为Atomikos，在项目的logs目录中会生成`xa_tx.log`, 这是XA崩溃恢复时所需的日志，请勿删除。

也可以通过在项目的classpath中添加`jta.properties`来定制化Atomikos配置项。具体的配置规则请参考Atomikos的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

## BASE（柔性）事务

ShardingSphere中已经整合了Saga和Seata两种BASE类型的事务

### 引入Maven依赖

```xml
<!-- saga柔性事务 -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>
```

${shardingsphere-spi-impl.version} 的jar暂未发布到maven中央仓，因此需要您根据源码自行部署。项目地址: [shardingsphere-spi-impl](https://github.com/sharding-sphere/shardingsphere-spi-impl)

```xml
<!-- seata柔性事务 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-seata-at</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### JAVA编码方式设置事务类型

 ```java
 TransactionTypeHolder.set(TransactionType.BASE);
 ```

#### Saga配置

可以通过在项目的classpath中添加`saga.properties`来定制化Saga事务的配置项。当saga.persistence.enabled=true时，事务日志默认按JDBC的方式持久化到数据库中，也可以通过实现`io.shardingsphere.transaction.saga.persistence.SagaPersistence` 
SPI，支持定制化存储，具体可参考项目sharding-transaction-base-saga-persistence-jpa。

配置项的属性及说明如下：

| **属性名称**                                       | **默认值**       | **说明**                              |
| ---------------------------------------------------|-----------------|---------------------------------------|
| saga.actuator.executor.size                        |        5        | 使用的线程池大小                       |
| saga.actuator.transaction.max.retries              |        5        | 失败SQL的最大重试次数                  |
| saga.actuator.compensation.max.retries             |        5        | 失败SQL的最大尝试补偿次数              |
| saga.actuator.transaction.retry.delay.milliseconds |       5000      | 失败SQL的重试间隔，单位毫秒            |
| saga.actuator.compensation.retry.delay.milliseconds|       3000      | 失败SQL的补偿间隔，单位毫秒            |
| saga.persistence.enabled                           |       false     | 是否对日志进行持久化                   |
| saga.persistence.ds.url                            |    无           | 事务日志数据库JDBC连接                 |
| saga.persistence.ds.username                       |    无           | 事务日志数据库用户名                   |
| saga.persistence.ds.password                       |    无           | 事务日志数据库密码                     |
| saga.persistence.ds.max.pool.size                  |    50           | 事务日志连接池最大连接数               |
| saga.persistence.ds.min.pool.size                  |    1            | 事务日志连接池最小连接数               |
| saga.persistence.ds.max.life.time.milliseconds     | 0(无限制)       | 事务日志连接池最大存活时间，单位毫秒    |
| saga.persistence.ds.idle.timeout.milliseconds      | 60 * 1000       | 事务日志连接池空闲回收时间，单位毫秒    |
| saga.persistence.ds.connection.timeout.milliseconds| 30 * 1000       | 事务日志连接池超时时间，单位毫秒        |

Saga事务日志表：

```sql
-- MySQL init table SQL

CREATE TABLE IF NOT EXISTS saga_event(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  saga_id VARCHAR(255) null,
  type VARCHAR(255) null,
  content_json TEXT null,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX saga_id_index(saga_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8
```

在classpath中添加`schema-init.sql`可以定日志表，Saga引擎会完成初始化建表操作。

#### Seata配置

1.按照[seata-work-shop](https://github.com/seata/seata-workshop)中的步骤，下载并启动seata server，参考 Step6 和 Step7即可。

2.在每一个分片数据库实例中执创建undo_log表（目前只支持Mysql）
```sql
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```
3.在classpath中修改seata.conf

```conf
client {
    application.id = raw-jdbc   ## 应用唯一id
    transaction.service.group = raw-jdbc-group   ## 所属事务组
}
```

## 分布式事务example

* [官方example](https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example)

* [第三方example（含spring配置）](https://github.com/OpenSharding/shardingsphere-spi-impl-example/tree/master/transaction-example)