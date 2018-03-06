+++
toc = true
date = "2017-08-23T22:38:50+08:00"
title = "Usage Example"
weight = 2
prev = "/01-start/quick-start"
next = "/01-start/faq"

+++

Please refer to the usage examples of Sharding-JDBC in [Usage Example](https://github.com/shardingjdbc/sharding-jdbc-example)

# Notices:

1. Please run resources/manual_shcema.sql on the test MySQL database to automatically create the database for testing. If PostgreSQL, SQLServer, or Oracle is used, you need to create test database by running your scripts. 

1. Users need to modify the database URL, driver, username, password, yaml or Spring configuration in the example.

1. In the example of Read-write splitting, user needs to build the Master-Slave replication relationship of the databases, otherwise the null value will be obtained when querying in the Slave.


# The usage example in native JDBC

## sharding-jdbc-raw-jdbc-java-example

### Read-write splitting:

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaMasterSlaveOnlyMain 
```

### Sharding:

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseAndTableMain
```

### Database Sharding merely:

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseOnlyMain
```

### Table Sharding merely:

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingTableOnlyMain
```

### Sharding + Read-write splitting:

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingAndMasterSlaveMain
```

# The usage example in Yaml

## sharding-jdbc-raw-jdbc-yaml-example

### Read-write splitting:

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlMasterSlaveOnlyMain 
```

### Sharding:

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseAndTableMain
```

### Database Sharding merely:

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseOnlyMain
```

### Table Sharding merely:

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingTableOnlyMain
```

### Sharding + Read-write splitting:

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingAndMasterSlaveMain
```

# The usage example in Spring based on JPA

## sharding-jdbc-spring-namespace-jpa-example

### Read-write splitting:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaMasterSlaveOnlyMain 
```

### Sharding:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndTableMain
```

### Database Sharding merely:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseOnlyMain
```

### Table Sharding merely:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingTableMain
```

### Sharding + Read-write splitting:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndMasterSlaveMain
```

# The usage example in Spring based on Mybatis

## sharding-jdbc-spring-namespace-mybatis-example

### Read-write splitting:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisMasterSlaveOnlyMain 
```

### Sharding:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndTableMain
```

### Database Sharding merely:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseOnlyMain
```

### Table Sharding merely:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingTableMain
```

### Sharding + Read-write splitting:

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndMasterSlaveMain
```

# The usage example in Spring Boot based on Spring Data JPA

## sharding-jdbc-spring-boot-data-jpa-example

### The entry class

```java
io.shardingjdbc.example.spring.boot.starter.jpa.SpringBootDataJpaMain
```

### The configuration introduction
To switch example configuration by modifying spring.profiles.active in resources/applicaiton.properties file.

```xml
spring.profiles.active=sharding
#spring.profiles.active=sharding-db
#spring.profiles.active=sharding-tbl
#spring.profiles.active=masterslave
#spring.profiles.active=sharding-masterslave
```

# The usage example in Spring Boot based on Spring Data Mybatis

## sharding-jdbc-spring-namespace-mybatis-example

### The entry class

```java
io.shardingjdbc.example.spring.boot.jpa.SpringBootDataMybatisMain
```

### The configuration introduction
To switch example configuration by modifying spring.profiles.active in resources/applicaiton.properties file.

```xml
spring.profiles.active=sharding
#spring.profiles.active=sharding-db
#spring.profiles.active=sharding-tbl
#spring.profiles.active=masterslave
#spring.profiles.active=sharding-masterslave
```

# The examples for the orchestration of the databases

To set up Zookeeper environment, whose address in this example is localhost:2181.

## sharding-jdbc-orchestration-java-example 

1. run

```java
io.shardingjdbc.example.orchestration.OrchestrationShardingMain
```

## sharding-jdbc-orchestration-yaml-example 

1. run

```java
io.shardingjdbc.example.orchestration.yaml.OrchestrationYamlShardingMain
```

## sharding-jdbc-orchestration-spring-namespace-example 

1. run

```java
io.shardingjdbc.example.orchestration.spring.namespace.OrchestrationSpringMybatisShardingShardingMain
```

## sharding-jdbc-orchestration-spring-boot-example 

1. run

```java
io.shardingjdbc.example.orchestration.spring.boot.OrchestrationSpringBootDataJpaMain
```

# The usage example of B.A.S.E transaction

## sharding-jdbc-transaction-example 

```java
io.shardingjdbc.example.transaction.TransactionMain
```
