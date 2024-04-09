+++
title = "Data Source"
weight = 4
chapter = true
+++

## Background

ShardingSphere-Proxy supports common data source connection pools: HikariCP, C3P0, DBCP (C3P0, DBCP need download plugin from [shardingsphere-plugins](https://github.com/apache/shardingsphere-plugin) repository).

The connection pool can be specified through the parameter `dataSourceClassName`. When not specified, the default data source connection pool is HikariCP.

## Parameters

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data_source_name>: # Data source name
    dataSourceClassName: # Data source connection pool full class name
    url: # The database URL connection
    username: # Database username
    password: # The database password
    # ... Other properties of data source pool
```
## Sample

```yaml
dataSources:
  ds_1:
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2:
    dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
    url: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  ds_3:
    dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
    url: jdbc:mysql://localhost:3306/ds_3
    username: root
    password:
  
  # Configure other data sources
```
