+++
title = "Distributed Transaction"
weight = 5
+++

Distributed transaction have been integrated into `ShardingDataSource`, you can use `TransactionTypeHolder` to modify transaction type before creating `ShardingConnection`.

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use XA transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use BASE transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Raw JDBC for sharding transaction

```java
TransactionTypeHolder.set(TransactionType.XA); // Support TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
try (Connection connection = dataSource.getConnection()) { // dataSource type is ShardingDataSource
    connection.setAutoCommit(false);
    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
    preparedStatement.setObject(1, i);
    preparedStatement.setObject(2, "init");
    preparedStatement.executeUpdate();
    connection.commit();
}
```

## Use Spring-namespace

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use XA transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use BASE transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Config Spring-namespace transaction manager

```xml
<!-- ShardingDataSource configuration -->
...

<!-- Enable auto scan @ShardingTransactionType annotation to inject the transaction type before connection created -->
<sharding:tx-type-annotation-driven />

<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="shardingDataSource" />
</bean>
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="shardingDataSource" />
</bean>
<tx:annotation-driven />

```

### Use sharding transaction in business code

```java
@Transactional
@ShardingTransactionType(TransactionType.XA)  // Support TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
public void insert() {
    jdbcTemplate.execute("INSERT INTO t_order (user_id, status) VALUES (?, ?)", (PreparedStatementCallback<Object>) preparedStatement -> {
        preparedStatement.setObject(1, i);
        preparedStatement.setObject(2, "init");
        preparedStatement.executeUpdate();
    });
}
```

## Use Spring-boot

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use XA transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- introduce this module if you want to use BASE transaction -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Config spring-boot transaction manager

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {
    
    @Bean
    public PlatformTransactionManager txManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

### Use sharding transaction in business code

```java
@Transactional
@ShardingTransactionType(TransactionType.XA)  // Support TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
public void insert() {
    jdbcTemplate.execute("INSERT INTO t_order (user_id, status) VALUES (?, ?)", (PreparedStatementCallback<Object>) preparedStatement -> {
        preparedStatement.setObject(1, i);
        preparedStatement.setObject(2, "init");
        preparedStatement.executeUpdate();
    });
}
```
## Other configuration for Sharding Transaction Manager

### XA Transaction (Optional)

Default ShardingSphere XA transaction manager is Atomikos. `xa_tx.log` generated in the project log is necessary for the recovery when XA crashes. Please do not delete it.

Or you can add `jta.properties` in `classpath` of the program to customize Atomikos configurations. 
For detailed configuration rules, please refer to the [official documentation](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos.

### BASE Transaction (SEATA-AT)

1.Download seata server according to [seata-work-shop](https://github.com/seata/seata-workshop), refer to step6 and step7 is OK.

2.Create `undo_log` table in each physical database.(only mysql supported before seata version 0.8.X)

```conf
CREATE TABLE IF NOT EXISTS `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'increment id',
  `branch_id`     BIGINT(20)   NOT NULL COMMENT 'branch transaction id',
  `xid`           VARCHAR(100) NOT NULL COMMENT 'global transaction id',
  `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
  `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created`   DATETIME     NOT NULL COMMENT 'create datetime',
  `log_modified`  DATETIME     NOT NULL COMMENT 'modify datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';
```

3.Config `seata.conf` file in classpath.

```conf
client {
    application.id = example   ## application unique id.
    transaction.service.group = my_test_tx_group   ## transaction group
}

```

4.Modify file.conf and registry.conf if you need.

## Distributed Transaction Example

* [official example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/sharding-example)