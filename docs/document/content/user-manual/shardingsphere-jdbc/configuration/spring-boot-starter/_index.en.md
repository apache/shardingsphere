+++
title = "Spring Boot Starter Configuration"
weight = 3
+++

## Introduction

ShardingSphere-JDBC provides official Spring Boot Starter to make convenient for developers to integrate ShardingSphere-JDBC and Spring Boot.

## Spring Boot Start Configuration Item

### Data Source Configuration

#### Configuration Example

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=root
```

#### Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Data source name, multiple data sources are separated by commas

spring.shardingsphere.datasource.<datasource_name>.type= # Database connection pool type name
spring.shardingsphere.datasource.<datasource_name>.driver-class-name= # Database driver class name
spring.shardingsphere.datasource.<datasource_name>.url= # Database URL connection
spring.shardingsphere.datasource.<datasource_name>.username= # Database username
spring.shardingsphere.datasource.<datasource_name>.password= # Database password
spring.shardingsphere.datasource.<data-source-name>.xxx= # Other properties of database connection pool
```

### Rule Configuration

#### Configuration Example

```properties
spring.shardingsphere.rules.sharding.xxx=xxx
```

#### Configuration Item Explanation

```properties
spring.shardingsphere.rules.<rule-type>.xxx= # rule configurations
  # ... Specific rule configurations
```

Please refer to specific rule configuration for more details.

### Properties Configuration

#### Configuration Example

```properties
spring.shardingsphere.props.xxx.xxx=xxx
```

#### Configuration Item Explanation

```properties
spring.shardingsphere.props.xxx.xxx= # Properties key and value
```
