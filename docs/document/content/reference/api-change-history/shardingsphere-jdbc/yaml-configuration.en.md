+++
title = "YAML configuration"
weight = 1
+++

## 5.0.0-alpha

### Data Sharding

#### Configuration Item Explanation

```yaml
dataSources: # Omit the data source configuration, please refer to the usage

rules:
- !SHARDING
  tables: # Sharding table configuration
    <logic-table-name> (+): # Logic table name
      actualDataNodes (?): # Describe data source names and actual tables (refer to Inline syntax rules)
      databaseStrategy (?): # Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.
        standard: # For single sharding column scenario
          shardingColumn: # Sharding column name
          shardingAlgorithmName: # Sharding algorithm name
        complex: # For multiple sharding columns scenario
          shardingColumns: # Sharding column names, multiple columns separated with comma
          shardingAlgorithmName: # Sharding algorithm name
        hint: # Sharding by hint
          shardingAlgorithmName: # Sharding algorithm name
        none: # Do not sharding
      tableStrategy: # Tables sharding strategy, same as database sharding strategy
      keyGenerateStrategy: # Key generator strategy
        column: # Column name of key generator
        keyGeneratorName: # Key generator name
  autoTables: # Auto Sharding table configuration
    t_order_auto: # Logic table name
      actualDataSources (?): # Data source names
      shardingStrategy: # Sharding strategy
        standard: # For single sharding column scenario
          shardingColumn: # Sharding column name
          shardingAlgorithmName: # Auto sharding algorithm name
  bindingTables (+): # Binding tables
    - <logic_table_name_1, logic_table_name_2, ...> 
    - <logic_table_name_1, logic_table_name_2, ...> 
  broadcastTables (+): # Broadcast tables
    - <table-name>
    - <table-name>
  defaultDatabaseStrategy: # Default strategy for database sharding
  defaultTableStrategy: # Default strategy for table sharding
  defaultKeyGenerateStrategy: # Default Key generator strategy
  
  # Sharding algorithm configuration
  shardingAlgorithms:
    <sharding-algorithm-name> (+): # Sharding algorithm name
      type: # Sharding algorithm type
      props: # Sharding algorithm properties
      # ...
  
  # Key generate algorithm configuration
  keyGenerators:
    <key-generate-algorithm-name> (+): # Key generate algorithm name
      type: # Key generate algorithm type
      props: # Key generate algorithm properties
      # ...

props:
  # ...
```

### Replica Query

#### Configuration Item Explanation

```yaml
dataSources: # Omit the data source configuration, please refer to the usage

rules:
- !REPLICA_QUERY
  dataSources:
    <data-source-name> (+): # Logic data source name of replica query
      primaryDataSourceName: # Primary data source name
      replicaDataSourceNames: 
        - <replica-data_source-name> (+) # Replica data source name
      loadBalancerName: # Load balance algorithm name
  
  # Load balance algorithm configuration
  loadBalancers:
    <load-balancer-name> (+): # Load balance algorithm name
      type: # Load balance algorithm type
      props: # Load balance algorithm properties
        # ...

props:
  # ...
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.

### Encryption

#### Configuration Item Explanation

```yaml
dataSource: # Omit the data source configuration, please refer to the usage

rules:
- !ENCRYPT
  tables:
    <table-name> (+): # Encrypt table name
      columns:
        <column-name> (+): # Encrypt logic column name
          cipherColumn: # Cipher column name
          assistedQueryColumn (?):  # Assisted query column name
          plainColumn (?): # Plain column name
          encryptorName: # Encrypt algorithm name
  
  # Encrypt algorithm configuration
  encryptors:
    <encrypt-algorithm-name> (+): # Encrypt algorithm name
      type: # Encrypt algorithm type
      props: # Encrypt algorithm properties
        # ...

  queryWithCipherColumn: # Whether query with cipher column for data encrypt. User you can use plaintext to query if have
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt) for more details about type of algorithm.

### Shadow DB

#### Configuration Item Explanation

```yaml
dataSources: # Omit the data source configuration, please refer to the usage

rules:
- !SHADOW
  column: # Shadow column name
  sourceDataSourceNames: # Source Data Source names
     # ...
  shadowDataSourceNames: # Shadow Data Source names
     # ... 

props:
  # ...
```

### Governance

#### Configuration Item Explanation

```yaml
governance:
  name: # Governance name
  registryCenter: # Registry center
    type: # Governance instance type. Example:Zookeeper, etcd
    serverLists: # The list of servers that connect to governance instance, including IP and port number; use commas to separate 
  overwrite: # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```

## ShardingSphere-4.x

### Data Sharding

#### Configuration Item Explanation

```yaml
dataSources: # Data sources configuration, multiple `data_source_name` available
  <data_source_name>: # <!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: # Class name of database driver
    url: # Database URL
    username: # Database username
    password: # Database password
    # ... Other properties for data source pool

shardingRule:
  tables: # Sharding rule configuration, multiple `logic_table_name` available
    <logic_table_name>: # Name of logic table
      actualDataNodes: # Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7}

      databaseStrategy: # Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one
        standard: # Standard sharding scenario for single sharding column
          shardingColumn: # Name of sharding column
            preciseAlgorithmClassName: # Precise algorithm class name used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm, and require a no argument constructor
            rangeAlgorithmClassName: # Range algorithm class name used for `BETWEEN`. This class need to implements RangeShardingAlgorithm, and require a no argument constructor
          complex: # Complex sharding scenario for multiple sharding columns
            shardingColumns: # Names of sharding columns. Multiple columns separated with comma
            algorithmClassName: # Complex sharding algorithm class name. This class need to implements ComplexKeysShardingAlgorithm, and require a no argument constructor
          inline: # Inline expression sharding scenario for single sharding column
            shardingColumn: # Name of sharding column
            algorithmInlineExpression: # Inline expression for sharding algorithm
          hint: # Hint sharding strategy
            algorithmClassName: # Hint sharding algorithm class name. This class need to implements HintShardingAlgorithm, and require a no argument constructor
            none: # Do not sharding
      tableStrategy: # Tables sharding strategy, Same as databases sharding strategy
      keyGenerator:
        column: # Column name of key generator
        type: # Type of key generator, use default key generator if absent, and there are three types to choose, that is, SNOWFLAKE/UUID
        props: # Properties, Notice: when use SNOWFLAKE, `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` need to be set. To use the generated value of this algorithm as sharding value, it is recommended to configure `max.vibration.offset`         

  bindingTables: # Binding table rule configurations
    - <logic_table_name1, logic_table_name2, ...>
    - <logic_table_name3, logic_table_name4, ...>
    - <logic_table_name_x, logic_table_name_y, ...>
  broadcastTables: # Broadcast table rule configurations
    - table_name1
    - table_name2
    - table_name_x

  defaultDataSourceName: # If table not configure at table rule, will route to defaultDataSourceName  
  defaultDatabaseStrategy: # Default strategy for sharding databases, same as databases sharding strategy
  defaultTableStrategy: # Default strategy for sharding tables, same as tables sharding strategy
  defaultKeyGenerator:
    type: # Type of default key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`
    column: # Column name of default key generator
    props: # Properties of default key generator, e.g. `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` 

  masterSlaveRules: # Read-write splitting rule configuration, more details can reference Read-write splitting part
    <data_source_name>: # Data sources configuration, need consist with data source map, multiple `data_source_name` available
      masterDataSourceName: # more details can reference Read-write splitting part
      slaveDataSourceNames: # more details can reference Read-write splitting part
      loadBalanceAlgorithmType: # more details can reference Read-write splitting part
      props: # Properties configuration of load balance algorithm
        <property-name>: # property key value pair

props: # Properties
  sql.show: # To show SQLS or not, default value: false
  executor.size: # The number of working threads, default value: CPU count
  check.table.metadata.enabled: # To check the metadata consistency of all the tables or not, default value : false
  max.connections.size.per.query: # The maximum connection number allocated by each query of each physical database. default value: 1
```

### Read-Write Split

#### Configuration Item Explanation

```yaml
dataSources: # Omit data source configurations; keep it consistent with data sharding

masterSlaveRule:
  name: # Read-write split data source name
  masterDataSourceName: # Master data source name
  slaveDataSourceNames: # Slave data source name
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmType: # Slave database load balance algorithm type; optional value, ROUND_ROBIN and RANDOM, can be omitted if `loadBalanceAlgorithmClassName` exists
  props: # Properties configuration of load balance algorithm
    <property-name>: # property key value pair

props: # Property configuration
  sql.show: # Show SQL or not; default value: false
  executor.size: # Executing thread number; default value: CPU core number
  check.table.metadata.enabled: # Whether to check table metadata consistency when it initializes; default value: false
  max.connections.size.per.query: # The maximum connection number allocated by each query of each physical database. default value: 1
```

Create a `DataSource` through the `YamlMasterSlaveDataSourceFactory` factory class:

```java
DataSource dataSource = YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### Data Masking

#### Configuration Item Explanation

```yaml
dataSource: # Ignore data sources configuration

encryptRule:
  encryptors:
    <encryptor-name>:
      type: # Encryptor type
      props: # Properties, e.g. `aes.key.value` for AES encryptor
        aes.key.value:
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: # Plaintext column name
          cipherColumn: # Ciphertext column name
          assistedQueryColumn: # AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data
          encryptor: # Encrypt name
```

### Orchestration

#### Configuration Item Explanation

```yaml
dataSources: # Omit data source configurations
shardingRule: # Omit sharding rule configurations
masterSlaveRule: # Omit read-write split rule configurations
encryptRule: # Omit encrypt rule configurations

orchestration:
  name: # Orchestration instance name
  overwrite: # Whether to overwrite local configurations with registry center configurations; if it can, each initialization should refer to local configurations
  registry: # Registry center configuration
    type: # Registry center type. Example:zookeeper
    serverLists: # The list of servers that connect to registry center, including IP and port number; use commas to seperate addresses, such as: host1:2181,host2:2181
    namespace: # Registry center namespace
    digest: # The token that connects to the registry center; default means there is no need for authentication
    operationTimeoutMilliseconds: # Default value: 500 milliseconds
    maxRetries: # Maximum retry time after failing; default value: 3 times
    retryIntervalMilliseconds: # Interval time to retry; default value: 500 milliseconds
    timeToLiveSeconds: # Living time of temporary nodes; default value: 60 seconds
```

## ShardingSphere-3.x

### Data Sharding

#### Configuration Item Explanation

```yaml
dataSources: # Data sources configuration, multiple `data_source_name` available
  <data_source_name>: # <!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: # Class name of database driver
    url: # Database URL
    username: # Database username
    password: # Database password
    # ... Other properties for data source pool

shardingRule:
  tables: # Sharding rule configuration, multiple `logic_table_name` available
    <logic_table_name>: # Name of logic table
      actualDataNodes: # Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7}

      databaseStrategy: # Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one
        standard: # Standard sharding scenario for single sharding column
          shardingColumn: # Name of sharding column
            preciseAlgorithmClassName: # Precise algorithm class name used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm, and require a no argument constructor
            rangeAlgorithmClassName: # Range algorithm class name used for `BETWEEN`. This class need to implements RangeShardingAlgorithm, and require a no argument constructor
          complex: # Complex sharding scenario for multiple sharding columns
            shardingColumns: # Names of sharding columns. Multiple columns separated with comma
            algorithmClassName: # Complex sharding algorithm class name. This class need to implements ComplexKeysShardingAlgorithm, and require a no argument constructor
          inline: # Inline expression sharding scenario for single sharding column
            shardingColumn: # Name of sharding column
            algorithmInlineExpression: # Inline expression for sharding algorithm
          hint: # Hint sharding strategy
            algorithmClassName: # Hint sharding algorithm class name. This class need to implements HintShardingAlgorithm, and require a no argument constructor
            none: # Do not sharding
      tableStrategy: # Tables sharding strategy, Same as databases sharding strategy

      keyGeneratorColumnName: # Column name of key generator, do not use Key generator if absent
      keyGeneratorClassName: # Key generator, use default key generator if absent. This class need to implements KeyGenerator, and require a no argument constructor

      logicIndex: # Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables
  bindingTables: # Binding table rule configurations
    - <logic_table_name1, logic_table_name2, ...>
    - <logic_table_name3, logic_table_name4, ...>
    - <logic_table_name_x, logic_table_name_y, ...>
  bindingTables: # Broadcast table rule configurations
    - table_name1
    - table_name2
    - table_name_x

  defaultDataSourceName: # If table not configure at table rule, will route to defaultDataSourceName  
  defaultDatabaseStrategy: # Default strategy for sharding databases, same as databases sharding strategy
  defaultTableStrategy: # Default strategy for sharding tables, same as tables sharding strategy
  defaultKeyGeneratorClassName: # Default key generator class name, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator`. This class need to implements KeyGenerator, and require a no argument constructor

  masterSlaveRules: # Read-write splitting rule configuration, more details can reference Read-write splitting part
    <data_source_name>: # Data sources configuration, need consist with data source map, multiple `data_source_name` available
      masterDataSourceName: # more details can reference Read-write splitting part
      slaveDataSourceNames: # more details can reference Read-write splitting part
      loadBalanceAlgorithmType: # more details can reference Read-write splitting part
      loadBalanceAlgorithmClassName: # more details can reference Read-write splitting part
      configMap: # User-defined arguments
        key1: value1
        key2: value2
        keyx: valuex

props: # Properties
  sql.show: # To show SQLS or not, default value: false
  executor.size: # The number of working threads, default value: CPU count
  check.table.metadata.enabled: #T o check the metadata consistency of all the tables or not, default value : false

configMap: # User-defined arguments
  key1: value1
  key2: value2
  keyx: valuex
```

### Read-Write Split

#### Configuration Item Explanation

```yaml
dataSources: # Ignore data sources configuration, same as sharding

masterSlaveRule:
  name: # Name of master slave data source
  masterDataSourceName: # Name of master data source
  slaveDataSourceNames: # Names of Slave data sources
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmClassName: # Load balance algorithm class name. This class need to implements MasterSlaveLoadBalanceAlgorithm, and require a no argument constructor
  loadBalanceAlgorithmType: # Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `loadBalanceAlgorithmClassName` is present

props: # Properties
  sql.show: # To show SQLS or not, default value: false
  executor.size: # The number of working threads, default value: CPU count
  check.table.metadata.enabled: # To check the metadata consistency of all the tables or not, default value : false

configMap: # User-defined arguments
  key1: value1
  key2: value2
  keyx: valuex
```

Create a `DataSource` through the `YamlMasterSlaveDataSourceFactory` factory class:

```java
DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### Orchestration

#### Configuration Item Explanation

```yaml
dataSources: # Ignore data sources configuration
shardingRule: # Ignore sharding rule configuration
masterSlaveRule: # Ignore master slave rule configuration

orchestration:
  name: # Name of orchestration instance
  overwrite: # Use local configuration to overwrite registry center or not
  registry: # Registry configuration
    serverLists: # Registry servers list, multiple split as comma. Example: host1:2181,host2:2181
    namespace: # Namespace of registry
    digest: # Digest for registry. Default is not need digest.
    operationTimeoutMilliseconds: # Operation timeout time in milliseconds, default value is 500 milliseconds
    maxRetries: # Max number of times to retry, default value is 3
    retryIntervalMilliseconds: # Time interval in milliseconds on each retry, default value is 500 milliseconds
    timeToLiveSeconds: # Time to live in seconds of ephemeral keys, default value is 60 seconds
```

## ShardingSphere-2.x

### Data Sharding

#### Configuration Item Explanation

```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

shardingRule:
  tables:
    config:
      actualDataNodes: db${0..1}.t_config
    t_order: 
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
      keyGeneratorClass: io.shardingjdbc.core.yaml.fixture.IncrementKeyGenerator
    t_order_item:
      actualDataNodes: db${0..1}.t_order_item_${0..1}
      # The strategy of binding the rest of the tables in the table is the same as the strategy of the first table
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
  # Default database sharding strategy
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true
```

### Read-Write Split

#### concept

In order to relieve the pressure on the database, the write and read operations are separated into different data sources. The write library is called the master library, and the read library is called the slave library. One master library can be configured with multiple slave libraries.

#### Supported

1. Provides a read-write separation configuration with one master and multiple slaves, which can be used independently or with sub-databases and sub-meters.
2. Independent use of read-write separation to support SQL transparent transmission.
3. In the same thread and the same database connection, if there is a write operation, subsequent read operations will be read from the main library to ensure data consistency.
4. Spring namespace.
5. Hint-based mandatory main library routing.

#### Unsupported

1. Data synchronization between the master library and the slave library.
2. Data inconsistency caused by the data synchronization delay of the master library and the slave library.
3. Double writing or multiple writing in the main library.

#### rule configuration

```yaml
dataSources:
  db_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: org.h2.Driver
      url: jdbc:h2:mem:db_slave_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
      username: sa
      password: 
      maxActive: 100

masterSlaveRule:
  name: db_ms
  masterDataSourceName: db_master
  slaveDataSourceNames: [db_slave_0, db_slave_1]
  configMap:
    key1: value1
```

Create a `DataSource` through the `MasterSlaveDataSourceFactory` factory class:

```java
DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### Orchestration

#### Configuration Item Explanation

Zookeeper sharding table and database Orchestration Configuration Item Explanation

```yaml
dataSources: Data sources configuration

shardingRule: Sharding rule configuration

orchestration: Zookeeper Orchestration Configuration
  name: Orchestration name
  overwrite: Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
  zookeeper: Registry center Configuration
    namespace: Registry center namespace
    serverLists: The list of servers that connect to governance instance, including IP and port number, use commas to separate, such as: host1:2181,host2:2181
    baseSleepTimeMilliseconds: The initial millisecond value of the interval to wait for retry
    maxSleepTimeMilliseconds: The maximum millisecond value of the interval to wait for retry
    maxRetries: The maximum retry count
    sessionTimeoutMilliseconds: The session timeout milliseconds
    connectionTimeoutMilliseconds: The connecton timeout milliseconds
    digest: Permission token to connect to Zookeeper. default no authorization is required
```

Etcd sharding table and database Orchestration Configuration Item Explanation

```yaml
dataSources: Data sources configuration

shardingRule: Sharding rule configuration

orchestration: Etcd Orchestration Configuration
  name: Orchestration name
  overwrite: Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
  etcd: Registry center Configuration
    serverLists: The list of servers that connect to governance instance, including IP and port number, use commas to separate, such as: http://host1:2379,http://host2:2379
    timeToLiveSeconds: Time to live seconds for ephemeral nodes
    timeoutMilliseconds: The request timeout milliseconds
    maxRetries: The maximum retry count
    retryIntervalMilliseconds: The retry interval milliseconds
```

Sharding table and database Data source construction method

```java
DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

Read-Write split Data source construction method

```java
DataSource dataSource = OrchestrationMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```
