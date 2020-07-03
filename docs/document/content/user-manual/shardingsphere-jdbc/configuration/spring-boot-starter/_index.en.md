+++
title = "Spring Boot Starter Configuration"
weight = 3
+++

## Introduction

ShardingSphere-JDBC provides official Spring Boot Starter to make convenient for developers to integrate ShardingSphere-JDBC and Spring Boot.

## Data Source Configuration

```properties
spring.shardingsphere.datasource.names= # Data source name, multiple data sources are separated by commas

spring.shardingsphere.datasource.common.type= # Database connection pool type name
spring.shardingsphere.datasource.common.driver-class-name= # Database driver class name
spring.shardingsphere.datasource.common.username= # Database username
spring.shardingsphere.datasource.common.password= # Database password
spring.shardingsphere.datasource.common.xxx= # Other properties of database connection pool

spring.shardingsphere.datasource.<datasource-name>.url= # Database URL connection
```

## Override Data Source Common Configuration
```properties
spring.shardingsphere.datasource.<datasource-name>.url= # Database URL connection
spring.shardingsphere.datasource.<datasource-name>.type= # Database connection pool type name，Override common type property
spring.shardingsphere.datasource.<datasource-name>.driver-class-name= # Database driver class name，Override common driver-class-name property
spring.shardingsphere.datasource.<datasource-name>.username= # Database username ，Override common username property
spring.shardingsphere.datasource.<datasource-name>.password= # Database password ，Override common password property
spring.shardingsphere.datasource.<data-source-name>.xxx= # Other properties of database connection pool ，Override common other property
```

## Rule Configuration

```properties
spring.shardingsphere.rules.<rule-type>.xxx= # rule configurations
  # ... Specific rule configurations
```

Please refer to specific rule configuration for more details.

## Properties Configuration

```properties
spring.shardingsphere.props.xxx.xxx= # Properties key and value
```

Please refer to [Properties Configuration](/en/user-manual/shardingsphere-jdbc/configuration/props) for more details about type of algorithm.
