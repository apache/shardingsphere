+++
toc = true
title = "Distributed Transaction"
weight = 5
+++

## 1. 2PC Transaction-XA

### 1.1 Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

XA transaction manager will be uploaded by Sharding-JDBC as SPI.

### 1.2 Atomikos Configuration (Optional)

Default ShardingSphere XA transaction manager is Atomikos. `xa_tx.log` generated in the project log is necessary for the recovery when XA crashes. Please do not delete it.

Or you can add `jta.properties` in `classpath` of the program to customize Atomikos configurations. 
For detailed configuration rules, please refer to the [official documentation](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos.

## 2. Third Party BASE Implementation-Saga

Currently, Apache/incubator-shardingsphere does not have BASE implementation, but it still can use a third party BASE implementation.

Project: [shardingsphere-spi-impl](https://github.com/sharding-sphere/shardingsphere-spi-impl)

`${shardingsphere-spi-impl.version}` mentioned has not been posted to the central maven repository, so you need to set it yourself.

### 2.1 Introduce Maven Dependency

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>
```

Saga transaction manager will be uploaded by Sharding-JDBC as SPI.

### 2.2 Saga Configuration

You can add `saga.properties` in the project classpath to customize Saga configurations. Configuration properties and explanations are as follow:

| *Property*                                          | *Default Value* | *Explanation*                                                |
| --------------------------------------------------- | --------------- | ------------------------------------------------------------ |
| saga.actuator.executor.size                         | 5               | Thread pool size                                             |
| saga.actuator.transaction.max.retries               | 5               | Maximum retry times                                          |
| saga.actuator.compensation.max.retries              | 5               | Maximum compensation times                                   |
| saga.actuator.transaction.retry.delay.milliseconds  | 5000            | Retry interval                                               |
| saga.actuator.compensation.retry.delay.milliseconds | 3000            | Compensation interval                                        |
| saga.persistence.enabled                            | false           | Persistence for snapshot and log                             |
| saga.actuator.recovery.policy                       | ForwardRecovery | Compensation strategy: ForwardRecovery is trying to deliver, BackwardRecovery is backward SQL compensation |

### 2.3 Saga Persistence for Snapshot and Log

When `saga.persistence.enabled` is `true`, Saga engine will persist snapshot and log. Saga Persistence is written to MySQL, H2 or PostgreSQL database through HikariCP link. Its configurations are also in `saga.properties`, configuration explanation is as follow:

| *Property*                                          | *Default Value*  | *Explanation*                    |
| --------------------------------------------------- | ---------------- | -------------------------------- |
| saga.persistence.ds.url                             | No               | JDBC url                         |
| saga.persistence.ds.username                        | No               | User name                        |
| saga.persistence.ds.password                        | No               | Password                         |
| saga.persistence.ds.max.pool.size                   | 50               | Maximum connection               |
| saga.persistence.ds.min.pool.size                   | 1                | Minimum connection               |
| saga.persistence.ds.max.life.time.milliseconds      | 0 (unrestricted) | Maximum life time (millisecond)  |
| saga.persistence.ds.idle.timeout.milliseconds       | 60 * 1000        | Idle timeout (millisecond)       |
| saga.persistence.ds.connection.timeout.milliseconds | 30 * 1000        | Connection timeout (millisecond) |

Users may use different database and transaction sizes, so they can add `schema-init.sql` in classpath to customize a persistent table structure, which will be automatically established by Saga engine according to SQL. Take MySQL for example, users can modify them to match different databases and fields.

```sql
-- MySQL init table SQL

CREATE TABLE IF NOT EXISTS saga_snapshot(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id VARCHAR(255) null,
  snapshot_id int null,
  revert_context VARCHAR(255) null,
  transaction_context VARCHAR(255) null,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX transaction_snapshot_index(transaction_id, snapshot_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS saga_event(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  saga_id VARCHAR(255) null,
  type VARCHAR(255) null,
  content_json TEXT null,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX saga_id_index(saga_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8
```

### 2.4 Specialized SPI for Saga Snapshot and Log Persistence

Snapshot and Log Persistence through database may not satisfy users' business and performance requirements. So Saga engine provides SPI for users to customize persistence part. When `saga.persistence.enabled` is `true` and Saga engine has detected persistent SPI, it will replace default configuration with SPI defined by users. So users only need to implement `io.shardingsphere.transaction.saga.persistence.SagaPersistence`. Please refer to `sharding-transaction-base-saga-persistence-jpa` for more details. Introduce Maven:

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga-persistence-jpa</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>
```

### 2.5 Notice for Saga

- Backward SQL needs **primary key**, please make sure it is defined in the table structure.
- `INSERT` statements need to show the **primary key value** in SQL, like `INSERT INTO ${table_name} (id, value, ...) VALUES (11111, '', ....) (id is table primary key)`.
- ShardingSphere distributed key can be used to generate primary key automatically (the distributed key can not be joint primary key).

## 3. Distributed Transaction Access

ShardingSphere stores its transactions in local variables of `TransactionTypeHolder`. Modifying the value before database connection, users can shift transaction types freely.

Notice: after database connection, transactions can not be changed.

### 3.1 Native API

```java
TransactionTypeHolder.set(TransactionType.LOCAL);
```

Or

```java
 TransactionTypeHolder.set(TransactionType.XA);
```

Or

```java
TransactionTypeHolder.set(TransactionType.BASE);
```

### 3.2 Spring Notes

```java
@ShardingTransactionType(TransactionType.LOCAL)
@Transactional
```

Or

```java
@ShardingTransactionType(TransactionType.XA)
@Transactional
```

Or

```java
@ShardingTransactionType(TransactionType.BASE)
@Transactional
```

Notice: `@ShardingTransactionType` needs to be used together with Spring `@Transactional` to make transactions take effect.

#### Spring boot starter

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-jdbc-spring-boot-starter</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${springframework.version}</version>
</dependency>

<aspectjweaver.version>1.8.9</aspectjweaver.version>
<springframework.version>[4.3.6.RELEASE,5.0.0.M1)</springframework.version>
```

#### Spring namespace 

Introduce Maven Dependency:

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-jdbc-spring</artifactId>
    <version>${shardingsphere-spi-impl.version}</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>${springframework.version}</version>
</dependency>

<aspectjweaver.version>1.8.9</aspectjweaver.version>
<springframework.version>[4.3.6.RELEASE,5.0.0.M1)</springframework.version>
```

Configurations

```xml
<import resource="classpath:META-INF/shardingTransaction.xml"/>
```

## Distributed Transaction Example

[transaction-example](https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example)