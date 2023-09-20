+++
title = "Data Source"
weight = 4
chapter = true
+++

## Background

ShardingSphere-PROXY supports common database connection pools: HikariCP, C3P0, DBCP.

The connection pool can be specified through the parameter `dataSourceClassName`. When not specified, the default database connection pool is HikariCP.

## Parameters

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data_source_name>: # Data source name
    dataSourceClassName: # Data source connection pool full class name
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
    dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  ds_3:
    dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_3
    username: root
    password:
  
  # Configure other data sources
```
