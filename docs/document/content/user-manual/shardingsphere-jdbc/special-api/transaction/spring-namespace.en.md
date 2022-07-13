+++
title = "Use Spring Namespace"
weight = 3
+++

## Background

ShardingSphere-JDBC can be used through spring namespace.

## Prerequisites

Introducing Maven denpendency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- This module is required when using XA transactions -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- This module is required when using XA's Narayana mode -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
    <version>${project.version}</version>
</dependency>

<!-- This module is required when using BASE transactions -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Procedure

1. Configure the transaction manager
2. Use distributed transactions

## Sample

### Configure the transaction manager

```xml
<!-- Configuration of ShardingDataSource -->
<!-- ...  -->

<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="shardingDataSource" />
</bean>
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="shardingDataSource" />
</bean>
<tx:annotation-driven />

<!-- Enable automatic scanning of @ShardingSphereTransactionType annotation and use Spring's native AOP for class and method enhancements -->
<sharding:tx-type-annotation-driven />
```

### Use distributed transactions

```java
@Transactional
@ShardingSphereTransactionType(TransactionType.XA)  // support TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
public void insert() {
        jdbcTemplate.execute("INSERT INTO t_order (user_id, status) VALUES (?, ?)", (PreparedStatementCallback<Object>) ps -> {
        ps.setObject(1, i);
        ps.setObject(2, "init");
        ps.executeUpdate();
        });
        }
```
