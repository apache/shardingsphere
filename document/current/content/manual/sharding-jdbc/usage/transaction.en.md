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

Currently, we have integrated saga and seata into shardingsphere.

### Introduce Maven Dependency

```xml
<!-- saga transaction -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>
```

`${shardingsphere-spi-impl.version}` mentioned has not been posted to the central maven repository, so you need to install it by yourself.Project Address: [shardingsphere-spi-impl](https://github.com/sharding-sphere/shardingsphere-spi-impl)

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

#### Saga Configuration

You can add `saga.properties` in the project classpath to customize Saga configurations. When `saga.persistence.enabled=true`, Saga engine will persist event log through JDBC.  
Configuration properties and explanations are as follow:

| **Property**                                        | **Default Value**| **Explanation**                                              |
| --------------------------------------------------- | ---------------- | ------------------------------------------------------------ |
| saga.actuator.executor.size                         | 5                | Saga actuator thread pool size                               |
| saga.actuator.transaction.max.retries               | 5                | Maximum retry times                                          |
| saga.actuator.compensation.max.retries              | 5                | Maximum compensation times                                   |
| saga.actuator.transaction.retry.delay.milliseconds  | 5000             | Retry interval                                               |
| saga.actuator.compensation.retry.delay.milliseconds | 3000             | Compensation interval                                        |
| saga.persistence.enabled                            | false            | Persistence for event log                                    |
| saga.persistence.ds.url                             | No               | JDBC url                         |
| saga.persistence.ds.username                        | No               | User name                        |
| saga.persistence.ds.password                        | No               | Password                         |
| saga.persistence.ds.max.pool.size                   | 50               | Maximum connection               |
| saga.persistence.ds.min.pool.size                   | 1                | Minimum connection               |
| saga.persistence.ds.max.life.time.milliseconds      | 0 (unrestricted) | Maximum life time (millisecond)  |
| saga.persistence.ds.idle.timeout.milliseconds       | 60 * 1000        | Idle timeout (millisecond)       |
| saga.persistence.ds.connection.timeout.milliseconds | 30 * 1000        | Connection timeout (millisecond) |

Saga event log table structure

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
`saga_event` table will be created automatically when your put this DDL into `schema-init.sql` located in classpath.

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