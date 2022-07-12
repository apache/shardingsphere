+++
title = "Data Source"
weight = 2
chapter = true
+++

## Background

ShardingSphere-JDBC Supports all JDBC drivers and database connection pools.

In this example, the database driver is MySQL, and the connection pool is HikariCP, which can be replaced with other database drivers and connection pools.
When using ShardingSphere JDBC, the property name of the JDBC pool depends on the definition of the respective JDBC pool and is not defined by ShardingSphere. For related processing, please refer to the class org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator.
For example, with Alibaba Druid 1.2.9, using url instead of jdbcUrl in the example below is the expected behavior.

## Parameters

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # Data source name
    dataSourceClassName: # Data source class name
    driverClassName: # The database driver class name is subject to the configuration of the database connection pool itself
    jdbcUrl: # The database URL connection is subject to the configuration of the database connection pool itself
    username: # Database user name, subject to the configuration of the database connection pool itself
    password: # The database password is subject to the configuration of the database connection pool itself
    # ... Other properties of data source pool
```
## Sample

```yaml
dataSources:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
      
  # Configure other data sources
```
