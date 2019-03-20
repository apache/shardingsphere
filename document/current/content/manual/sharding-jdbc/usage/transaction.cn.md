+++
toc = true
title = "分布式事务"
weight = 5
+++

## 1. 两阶段提交-XA

### 1.1 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

XA事务管理器将以SPI的方式被Sharding-JDBC所加载。

### 1.2 Atomikos参数配置 (可选)

ShardingSphere默认的XA事务管理器为Atomikos，在项目的logs目录中会生成`xa_tx.log`, 这是XA崩溃恢复时所需的日志，请勿删除。

也可以通过在项目的classpath中添加`jta.properties`来定制化Atomikos配置项。具体的配置规则请参考Atomikos的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

## 2. 第三方BASE实现-Saga

目前Apache/incubator-shardingsphere暂无BASE事务的实现，但是仍然可以使用第三方实现的Saga事务。

### 2.1 引入Maven依赖

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

Saga事务管理器将以SPI的方式被Sharding-JDBC所加载。

### 2.2 Saga相关配置

可以通过在项目的classpath中添加`saga.properties`来定制化Saga事务的配置项。
配置项的属性及说明如下：

| *属性名称*                                        | *默认值* | *说明*                         |
| -------------------------------------------------| --------| -------------------------------|
| saga.actuator.executor.size                      |    5    | Saga引擎所使用的线程池大小        |
| saga.actuator.transaction.max.retries            |    5    | Saga引擎对失败SQL的最大重试次数   |
| saga.actuator.compensation.max.retries           |    5    | Saga引擎对失败SQL的最大尝试补偿次数|
| saga.actuator.transaction.retry.delay.milliseconds| 5000   | Saga引擎对失败SQL的重试间隔，单位毫秒|
| saga.actuator.compensation.retry.delay.milliseconds| 3000  | Saga引擎对失败SQL的补偿间隔，单位毫秒|
| saga.persistence.enabled                         |  false  | Saga引擎对快照及执行日志进行持久化  |
| saga.actuator.recovery.policy                    | ForwardRecovery | Saga引擎对失败事务的补偿策略，ForwardRecovery为最大努力送达，BackwardRecovery为反向SQL补偿|

### 2.3 Saga 快照及日志持久化

当`saga.persistence.enabled`设置为`true`时，Saga引擎将会对事务的快照及执行日志进行持久化操作。
持久化操作默认通过HikariCP链接池写入到MySQL、H2或PostgreSQL数据库中。
关于持久化的配置，同样添加在`saga.properties`中，配置项及说明如下：

| *属性名称*                                        | *默认值* | *说明*                         |
| -------------------------------------------------| --------| -------------------------------|
| saga.persistence.ds.url                          |    无   | Saga持久化的数据库JDBC链接        |
| saga.persistence.ds.username                     |    无   | Saga持久化的数据库用户名          |
| saga.persistence.ds.password                     |    无   | Saga持久化的数据库密码            |
| saga.persistence.ds.max.pool.size                |    50   | Saga持久化的数据库链接池最大连接数 |
| saga.persistence.ds.min.pool.size                |    1    | Saga持久化的数据库链接池最小连接数 |
| saga.persistence.ds.max.life.time.milliseconds   | 0(无限制)| Saga持久化的数据库链接最大存活时间，单位毫秒  |
| saga.persistence.ds.idle.timeout.milliseconds    | 60 * 1000 | Saga持久化的数据库链接空闲回收时间，单位毫秒|
| saga.persistence.ds.connection.timeout.milliseconds| 30 * 1000 | Saga持久化的数据库链接超时时间，单位毫秒|

由于用户使用的数据库类型与事务大小不一定一致，因此可以在项目的classpath中添加`schema-init.sql`来定制化持久化的表结构，Saga引擎会自动根据其中SQL创建。
如下以MySQL为例，用户可自行修改，以匹配不同数据库类型及字段长度。

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

### 2.4 Saga 快照及日志持久化SPI定制

默认通过数据库来持久化快照和日志并不一定能够满足用户对业务和性能的需求。因此Saga引擎提供SPI允许用户定制化持久化部分。
当`saga.persistence.enabled`设置为`true`且Saga引擎监测到有持久化SPI时，Saga引擎将通过用户实现的SPI代替默认持久化进行持久化工作。
用户只需要实现接口`io.shardingsphere.transaction.saga.persistence.SagaPersistence`即可实现持久化SPI。
具体可参考项目`sharding-transaction-base-saga-persistence-jpa`，Maven引入：

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-base-saga-persistence-jpa</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 3. 分布式事务接入端

ShardingSphere的事务类型存放在`TransactionTypeHolder`的本地线程变量中，因此在数据库连接创建前修改此值，可以达到自由切换事务类型的效果。

注意：数据库连接创建之后，事务类型将无法更改。

### 3.1 原生API

```java
TransactionTypeHolder.set(TransactionType.LOCAL);
```

或

 ```java
 TransactionTypeHolder.set(TransactionType.XA);
 ```

或

```java
TransactionTypeHolder.set(TransactionType.BASE);
```
### 3.2 Spring注解
#### 使用方式

```java
@ShardingTransactionType(TransactionType.LOCAL)
@Transactional
```

或

```java
@ShardingTransactionType(TransactionType.XA)
@Transactional
```

或

```java
@ShardingTransactionType(TransactionType.BASE)
@Transactional
```

注意：`@ShardingTransactionType`需要同Spring的`@Transactional`配套使用，事务才会生效。

#### Spring boot starter
引入Maven依赖：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

#### Spring namespace

引入Maven依赖：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring</artifactId>
    <version>${sharding-sphere.version}</version>
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
加载切面配置信息

```xml
<import resource="classpath:META-INF/shardingTransaction.xml"/>

```