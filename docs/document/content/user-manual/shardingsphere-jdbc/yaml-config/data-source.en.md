+++
title = "Data Source"
weight = 2
chapter = true
+++

It is divided into single data source configuration and multi data source configuration.
ShardingSphere-JDBC Supports all JDBC drivers and database connection pools.

In this example, the database driver is MySQL, and connection pool is HikariCP, which can be replaced with other database drivers and connection pools.

## Single Data Source Configuration

Used for data encryption rules.

### Configuration Item Explanation

```yaml
dataSource: # <!!Data source pool implementation class> `!!` means class instantiation
  driverClassName: # Class name of database driver, ref property of connection pool
  url: # Database URL, ref property of connection pool
  username: # Database username, ref property of connection pool
  password: # Database password, ref property of connection pool
  # ... Other properties for data source pool
```

### Example

```yaml
dataSource: !!com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.jdbc.Driver
  jdbcUrl: jdbc:mysql://localhost:3306/ds
  username: root
  password:
```

## Multiple Data Source Configuration

Used for fragmentation, readwrite-splitting and other rules.
If features such as encryption and sharding are used in combination, a multi data source configuration should be used.

### Configuration Item Explanation

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # <!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: # Class name of database driver, ref property of connection pool
    url: # Database URL, ref property of connection pool
    username: # Database username, ref property of connection pool
    password: # Database password, ref property of connection pool
    # ... Other properties for data source pool
```

### Example

```yaml
dataSources:
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:
  ds_2: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
  
  # Configure other data sources
```
