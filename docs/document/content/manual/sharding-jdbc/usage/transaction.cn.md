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

ShardingSphere中已经整合了Seata-AT柔性事务

### 引入Maven依赖

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