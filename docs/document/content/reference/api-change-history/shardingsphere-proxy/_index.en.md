+++
title = "ShardingSphere-Proxy"
weight = 1
chapter = true
+++

## 5.0.0-beta

### Data Source Configuration Item Explanation

```yaml
schemaName: # Logic schema name.

dataSources: # Data sources configuration, multiple <data-source-name> available.
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool.
    url: # Database URL.
    username: # Database username.
    password: # Database password.
    connectionTimeoutMilliseconds: # Connection timeout milliseconds.
    idleTimeoutMilliseconds: # Idle timeout milliseconds.
    maxLifetimeMilliseconds: # Maximum life milliseconds.
    maxPoolSize: 50 # Maximum connection count in the pool.
    minPoolSize: 1  # Minimum connection count in the pool.        

rules: # Keep consist with ShardingSphere-JDBC configuration.
# ...
```

#### Authentication

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root # <username>@<hostname>:<password>
      - sharding@:sharding
    provider:
      type: NATIVE # Must be explicitly specified.
 ```

If the hostname is % or empty, it means no restrict to the user’s host.

The type of the provider must be explicitly specified. Refer to [5.11 Proxy](https://shardingsphere.apache.org/document/5.0.0-beta/en/dev-manual/proxy/) for more implementations.

#### Proxy Properties

```yaml
props:
  sql-show: # Whether show SQL or not in log. Print SQL details can help developers debug easier. The log details include: logic SQL, actual SQL and SQL parse result.Enable this property will log into log topic ShardingSphere-SQL, log level is INFO.
  sql-simple: # Whether show SQL details in simple style.
  executor-size: # The max thread size of worker group to execute SQL. One ShardingSphereDataSource will use a independent thread pool, it does not share thread pool even different data source in same JVM.
  max-connections-size-per-query: # Max opened connection size for each query.
  check-table-metadata-enabled: # Whether validate table meta data consistency when application startup or updated.
  proxy-frontend-flush-threshold: # Flush threshold for every records from databases for ShardingSphere-Proxy.
  proxy-transaction-type: # Default transaction type of ShardingSphere-Proxy. Include: LOCAL, XA and BASE.
  proxy-opentracing-enabled: # Whether enable opentracing for ShardingSphere-Proxy.
  proxy-hint-enabled: # Whether enable hint for ShardingSphere-Proxy. Using Hint will switch proxy thread mode from IO multiplexing to per connection per thread, which will reduce system throughput.
  xa-transaction-manager-type: # XA Transaction manager type. Include: Atomikos, Narayana and Bitronix.
```

## 5.0.0-alpha

### Data Source Configuration Item Explanation

```yaml
schemaName: # Logic schema name.

dataSourceCommon:
  username: # Database username.
  password: # Database password.
  connectionTimeoutMilliseconds: # Connection timeout milliseconds.
  idleTimeoutMilliseconds: # Idle timeout milliseconds.
  maxLifetimeMilliseconds: # Maximum life milliseconds.
  maxPoolSize: 50 # Maximum connection count in the pool.
  minPoolSize: 1  # Minimum connection count in the pool.

dataSources: # Data sources configuration, multiple <data-source-name> available.
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool.
    url: # Database URL.
rules: # Keep consist with ShardingSphere-JDBC configuration.
# ...
```

#### Override dataSourceCommon Configuration

If you want to override the ‘dataSourceCommon’ property, configure it separately for each data source.

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available.
  <data-source-name>: # Different from ShardingSphere-JDBC configuration, it does not need to be configured with database connection pool.
    url: # Database URL.
    username: # Database username, Override dataSourceCommon username property.
    password: # Database password, Override dataSourceCommon password property.
    connectionTimeoutMilliseconds: # Connection timeout milliseconds, Override dataSourceCommon connectionTimeoutMilliseconds property.
    idleTimeoutMilliseconds: # Idle timeout milliseconds, Override dataSourceCommon idleTimeoutMilliseconds property.
    maxLifetimeMilliseconds: # Maximum life milliseconds, Override dataSourceCommon maxLifetimeMilliseconds property.
    maxPoolSize: 50 # Maximum connection count in the pool, Override dataSourceCommon maxPoolSize property.
    minPoolSize: 1  # Minimum connection count in the pool, Override dataSourceCommon minPoolSize property.
```

#### Authentication

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
authentication:
  users:
    root: # Self-defined username.
      password: root # Self-defined password.
    sharding: # Self-defined username.
      password: sharding # Self-defined password.
      authorizedSchemas: sharding_db, replica_query_db # Schemas authorized to this user, please use commas to connect multiple schemas. Default authorized schemas is all of the schemas.
```

#### Proxy Properties

```yaml
props:
  sql-show: # Whether show SQL or not in log. Print SQL details can help developers debug easier. The log details include: logic SQL, actual SQL and SQL parse result.Enable this property will log into log topic ShardingSphere-SQL, log level is INFO.
  sql-simple: # Whether show SQL details in simple style.
  acceptor-size: # The max thread size of accepter group to accept TCP connections.
  executor-size: # The max thread size of worker group to execute SQL. One ShardingSphereDataSource will use a independent thread pool, it does not share thread pool even different data source in same JVM.
  max-connections-size-per-query: # Max opened connection size for each query.
  check-table-metadata-enabled: # Whether validate table meta data consistency when application startup or updated.
  query-with-cipher-column: # Whether query with cipher column for data encrypt. User you can use plaintext to query if have.
  proxy-frontend-flush-threshold: # Flush threshold for every records from databases for ShardingSphere-Proxy.
  proxy-transaction-type: # Default transaction type of ShardingSphere-Proxy. Include: LOCAL, XA and BASE.
  proxy-opentracing-enabled: # Whether enable opentracing for ShardingSphere-Proxy.
  proxy-hint-enabled: # Whether enable hint for ShardingSphere-Proxy. Using Hint will switch proxy thread mode from IO multiplexing to per connection per thread, which will reduce system throughput.
```

## ShardingSphere-4.x

### Data Source and Sharding Configuration Item Explanation

#### Data Sharding

```yaml
schemaName: # Logic data schema name.

dataSources: # Data source configuration, which can be multiple data_source_name.
  <data_source_name>: # Different from Sharding-JDBC configuration, it does not need to be configured with database connection pool.
    url: # Database url connection.
    username: # Database username.
    password: # Database password.
    connectionTimeoutMilliseconds: 30000 # Connection timeout.
    idleTimeoutMilliseconds: 60000 # Idle timeout setting.
    maxLifetimeMilliseconds: 1800000 # Maximum lifetime.
    maxPoolSize: 65 # Maximum connection number in the pool.

shardingRule: #Omit data sharding configuration and be consistent with Sharding-JDBC configuration.
```

#### Read-write splitting

```yaml
schemaName: # Logic data schema name.

dataSources: # Omit data source configurations; keep it consistent with data sharding.

masterSlaveRule: # Omit data source configurations; keep it consistent with Sharding-JDBC.
```

#### Data Masking

```yaml
dataSource: # Ignore data sources configuration.

encryptRule:
  encryptors:
    <encryptor-name>:
      type: # encryptor type.
      props: # Properties, e.g. `aes.key.value` for AES encryptor.
        aes.key.value:
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: # plaintext column name.
          cipherColumn: # ciphertext column name.
          assistedQueryColumn: # AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data.
          encryptor: # encrypt name.
props:
  query.with.cipher.column: true #Whether use cipherColumn to query or not
```

### Overall Configuration Explanation

#### Orchestration

It is the same with Sharding-JDBC configuration.

#### Proxy Properties

```yaml
# Omit configurations that are the same with Sharding-JDBC.

props:
  acceptor.size: # The thread number of accept connection; default to be 2 times of cpu core.
  proxy.transaction.type: # Support LOCAL, XA, BASE; Default is LOCAL transaction, for BASE type you should copy ShardingTransactionManager associated jar to lib directory.
  proxy.opentracing.enabled: # Whether to enable opentracing, default not to enable; refer to [APM](/en/features/orchestration/apm/) for more details.
  check.table.metadata.enabled: # Whether to check metadata consistency of sharding table when it initializes; default value: false.
```

#### Authentication

It is used to verify the authentication to log in Sharding-Proxy, which must use correct user name and password after the configuration of them.

```yaml
authentication:
  users:
    root: # self-defined username.
      password: root # self-defined password.
    sharding: # self-defined username.
      password: sharding # self-defined password.
      authorizedSchemas: sharding_db, masterslave_db # schemas authorized to this user, please use commas to connect multiple schemas. Default authorizedSchemas is all of the schemas.
```

## ShardingSphere-3.x

### Data sources and sharding rule configuration reference

#### Data Sharding

```yaml
schemaName: # Logic database schema name.

dataSources: # Data sources configuration, multiple `data_source_name` available.
  <data_source_name>: # Different with Sharding-JDBC, do not need configure data source pool here.
    url: # Database URL.
    username: # Database username.
    password: # Database password.
    autoCommit: true # The default config of hikari connection pool.
    connectionTimeout: 30000 # The default config of hikari connection pool.
    idleTimeout: 60000 # The default config of hikari connection pool.
    maxLifetime: 1800000 # The default config of hikari connection pool.
    maximumPoolSize: 65 # The default config of hikari connection pool.

shardingRule: # Ignore sharding rule configuration, same as Sharding-JDBC.
```

#### Read-write splitting

```yaml
schemaName: # Logic database schema name.

dataSources: # Ignore data source configuration, same as sharding.

masterSlaveRule: # Ignore read-write splitting rule configuration, same as Sharding-JDBC.
```

### Global configuration reference

#### Orchestration

Same as configuration of Sharding-JDBC.

#### Proxy Properties

```yaml
# Ignore configuration which same as Sharding-JDBC.

props:
  acceptor.size: # Max thread count to handle client's requests, default value is CPU*2.
  proxy.transaction.enabled: # Enable transaction, only support XA now, default value is false.
  proxy.opentracing.enabled: # Enable open tracing, default value is false. More details please reference[APM](/en/features/orchestration/apm/).
  check.table.metadata.enabled: # To check the metadata consistency of all the tables or not, default value : false.
```

#### Authorization

To perform Authorization for Sharding Proxy when login in. After configuring the username and password, you must use the correct username and password to login into the Proxy.

```yaml
authentication:
  username: root
  password:
```
