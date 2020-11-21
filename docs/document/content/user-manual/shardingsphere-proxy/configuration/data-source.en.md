+++
title = "Data Source Configuration"
weight = 1
+++

## Configuration Item Explanation

```yaml
schemaName: # Logic schema name

dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool
    url: # Database URL
    username: # Database username
    password: # Database password
    connectionTimeoutMilliseconds: #Connection timeout milliseconds
    idleTimeoutMilliseconds: #Idle timeout milliseconds
    maxLifetimeMilliseconds: #Maximum life milliseconds
    maxPoolSize: 50 #Maximum connection count in the pool
    minPoolSize: 1  #Minimum connection count in the pool        

rules: # Keep consist with ShardingSphere-JDBC configuration
  # ...
```
