+++
title = "Data Source Configuration"
weight = 1
+++

## Configuration Item Explanation

```yaml
schemaName: # Logic schema name
dataSourceCommon:
  username: # Database username
  password: # Database password
  connectionTimeoutMilliseconds: #Connection timeout milliseconds
  idleTimeoutMilliseconds: #Idle timeout milliseconds
  maxLifetimeMilliseconds: #Maximum life milliseconds
  maxPoolSize: 50 #Maximum connection count in the pool
  minPoolSize: 1  #Minimum connection count in the pool

dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool
    url: # Database URL
rules: # Keep consist with ShardingSphere-JDBC configuration
  # ...
```

## Override dataSourceCommon Configuration

If you want to override the 'dataSourceCommon' property, configure it separately for each data source.

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool
     url: # Database URL
     username: # Database username ,Override dataSourceCommon username property
     password: # Database password ,Override dataSourceCommon password property
     connectionTimeoutMilliseconds: #Connection timeout milliseconds ,Override dataSourceCommon connectionTimeoutMilliseconds property
     idleTimeoutMilliseconds: #Idle timeout milliseconds ,Override dataSourceCommon idleTimeoutMilliseconds property
     maxLifetimeMilliseconds: #Maximum life milliseconds ,Override dataSourceCommon maxLifetimeMilliseconds property
     maxPoolSize: 50 #Maximum connection count in the pool ,Override dataSourceCommon maxPoolSize property
     minPoolSize: 1  #Minimum connection count in the pool ,Override dataSourceCommon minPoolSize property
```