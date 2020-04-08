+++
toc = true
title = "Distributed Transaction"
weight = 5
+++

Distributed transaction have been integrated into `ShardingDataSource`, you can use `TransactionTypeHolder` to modify transaction type before creating `ShardingConnection`.

## XA

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Switch Transaction Type Based on Java

 ```java
 TransactionTypeHolder.set(TransactionType.XA);
 ```

### Atomikos Configuration (Optional)

Default ShardingSphere XA transaction manager is Atomikos. `xa_tx.log` generated in the project log is necessary for the recovery when XA crashes. Please do not delete it.

Or you can add `jta.properties` in `classpath` of the program to customize Atomikos configurations. 
For detailed configuration rules, please refer to the [official documentation](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos.

## BASE Transaction

Currently, we have integrated Seata-AT into shardingsphere.

### Introduce Maven Dependency

```xml
<!-- seata at transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-seata-at</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Switch Transaction Type Based on Java

 ```java
 TransactionTypeHolder.set(TransactionType.BASE);
 ```

#### Seata Configuration

1.Download seata server according to [seata-work-shop](https://github.com/seata/seata-workshop), refer to step6 and step7 is OK.

2.Create `undo_log` table in each physical database.(only mysql supported before seata version 0.8.X)

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
3.Config `seata.conf` file in classpath.

```conf
client {
    application.id = raw-jdbc   ## application unique id.
    transaction.service.group = raw-jdbc-group   ## transaction group
}
```

## Distributed Transaction Example

* [official example](https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example)

* [third party example（include spring）](https://github.com/OpenSharding/shardingsphere-spi-impl-example/tree/master/transaction-example)