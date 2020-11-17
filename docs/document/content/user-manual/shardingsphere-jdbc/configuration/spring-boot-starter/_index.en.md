+++
title = "Spring Boot Starter Configuration"
weight = 3
+++

## Introduction

ShardingSphere-JDBC provides official Spring Boot Starter to make convenient for developers to integrate ShardingSphere-JDBC and Spring Boot.

## Data Source Configuration

```properties
spring.shardingsphere.datasource.names= # Data source name, multiple data sources are separated by commas

spring.shardingsphere.datasource.<datasource-name>.url= # Database URL connection
spring.shardingsphere.datasource.<datasource-name>.type= # Database connection pool type name
spring.shardingsphere.datasource.<datasource-name>.driver-class-name= # Database driver class name
spring.shardingsphere.datasource.<datasource-name>.username= # Database username
spring.shardingsphere.datasource.<datasource-name>.password= # Database password
spring.shardingsphere.datasource.<datasource-name>.xxx= # Other properties of database connection pool
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
