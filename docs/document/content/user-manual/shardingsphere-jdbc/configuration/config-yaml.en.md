+++
title = "Yaml Configuration"
weight = 2
+++

## Configuration Instance

### Data Sharding

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
  
props:
  sql.show: true
```

### Read-Write Split

```yaml
dataSources:
  ds_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
  ds_slave0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave0
    username: root
    password: 
  ds_slave1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave0
    - ds_slave1
       
props: 
  sql.show: true
```

### data encryption

```yaml
dataSource:  !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
  username: root
  password:

encryptRule:
  encryptors:
    aes_encryptor:
      type: aes
      props:
        aes.key.value: 123456abc
    md5_encryptor:
      type: md5
  tables:
    t_encrypt:
      columns:
        user_id:
          plainColumn: user_plain
          cipherColumn: user_cipher
          encryptorName: aes_encryptor
        order_id:
          cipherColumn: order_cipher
          encryptorName: md5_encryptor
props:
  query.with.cipher.column: true
```

### Data Sharding + Read-Write Split

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds0_slave0: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds0_slave0
      username: root
      password: 
  ds0_slave1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds0_slave1
      username: root
      password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 
  ds1_slave0: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds1_slave0
        username: root
        password: 
  ds1_slave1: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds1_slave1
        username: root
        password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ms_ds${0..1}.t_order${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
  
  masterSlaveRules:
      ms_ds0:
        masterDataSourceName: ds0
        slaveDataSourceNames:
          - ds0_slave0
          - ds0_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
      ms_ds1:
        masterDataSourceName: ds1
        slaveDataSourceNames: 
          - ds1_slave0
          - ds1_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
props:
  sql.show: true
```

### Data Sharding + data encryption

```yaml
dataSources:
  ds_0: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0
    username: root
    password:
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1
    username: root
    password:

shardingRule:
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds_${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds_${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item 
  
  defaultTableStrategy:
    none:

  encryptRule:
    encryptors:
      aes_encryptor:
        type: aes
        props:
          aes.key.value: 123456abc
    tables:
      t_order:
        columns:
          order_id:
            plainColumn: order_plain
            cipherColumn: order_cipher
            encryptorName: aes_encryptor

props:
  sql.show: true
```

### Orchestration

```yaml
#Omit data sharding, read-write split and encrypt configurations

orchestration:
  orchestration_ds:
    orchestrationType: config_center,registry_center,metadata_center
    instanceType: zookeeper
    serverLists: localhost:2181
    namespace: orchestration
    props:
      overwrite: true
```

## Configuration Item Explanation

### Sharding

```yaml
dataSources: #Data sources configuration, multiple `data_source_name` available
  <data_source_name>: #<!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: #Class name of database driver
    url: #Database URL
    username: #Database username
    password: #Database password
    # ... Other properties for data source pool

shardingRule:
  tables: #Sharding rule configuration, multiple `logic_table_name` available
    <logic_table_name>: #Name of logic table
      actualDataNodes: #Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7}
        
      databaseStrategy: #Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.
        standard: #Standard sharding scenario for single sharding column
          shardingColumn: #Name of sharding column
            preciseAlgorithmClassName: #Precise algorithm class name used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm, and require a no argument constructor
            rangeAlgorithmClassName: #Range algorithm class name used for `BETWEEN`. This class need to implements RangeShardingAlgorithm, and require a no argument constructor
          complex: #Complex sharding scenario for multiple sharding columns
            shardingColumns: #Names of sharding columns. Multiple columns separated with comma
            algorithmClassName: #Complex sharding algorithm class name. This class need to implements ComplexKeysShardingAlgorithm, and require a no argument constructor
          inline: #Inline expression sharding scenario for single sharding column
            shardingColumn: #Name of sharding column
            algorithmInlineExpression: #Inline expression for sharding algorithm
          hint: #Hint sharding strategy
            algorithmClassName: #Hint sharding algorithm class name. This class need to implements HintShardingAlgorithm, and require a no argument constructor
           none: #Do not sharding
      tableStrategy: #Tables sharding strategy, Same as databases sharding strategy
      keyGenerator:   
        column: #Column name of key generator
        type: #Type of key generator, use default key generator if absent, and there are three types to choose, that is, SNOWFLAKE/UUID
        props: #Properties, Notice: when use SNOWFLAKE, `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` need to be set. To use the generated value of this algorithm as sharding value, it is recommended to configure `max.vibration.offset`         

  bindingTables: #Binding table rule configurations
  - <logic_table_name1, logic_table_name2, ...> 
  - <logic_table_name3, logic_table_name4, ...>
  - <logic_table_name_x, logic_table_name_y, ...>
  broadcastTables: #Broadcast table rule configurations
  - table_name1
  - table_name2
  - table_name_x
  
  defaultDataSourceName: #If table not configure at table rule, will route to defaultDataSourceName  
  defaultDatabaseStrategy: #Default strategy for sharding databases, same as databases sharding strategy
  defaultTableStrategy: #Default strategy for sharding tables, same as tables sharding strategy
  defaultKeyGenerator:
    type: #Type of default key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`
    column: #Column name of default key generator
    props: #Properties of default key generator, e.g. `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` 
  
  masterSlaveRules: #Read-write splitting rule configuration, more details can reference Read-write splitting part
    <data_source_name>: #Data sources configuration, need consist with data source map, multiple `data_source_name` available
      masterDataSourceName: #more details can reference Read-write splitting part
      slaveDataSourceNames: #more details can reference Read-write splitting part
      loadBalanceAlgorithmType: #more details can reference Read-write splitting part
      props: #Properties configuration of load balance algorithm
            <property-name>: #property key value pair

props: #Properties
  sql.show: #To show SQLS or not, default value: false
  executor.size: #The number of working threads, default value: CPU count
  check.table.metadata.enabled: #To check the metadata consistency of all the tables or not, default value : false
  max.connections.size.per.query: #The maximum connection number allocated by each query of each physical database. default value: 1
```

### Read-Write Split

```yaml
dataSources: #Omit data source configurations; keep it consistent with data sharding

masterSlaveRule:
  name: #Read-write split data source name
  masterDataSourceName: #Master data source name
  slaveDataSourceNames: #Slave data source name
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmType: #Slave database load balance algorithm type; optional value, ROUND_ROBIN and RANDOM, can be omitted if `loadBalanceAlgorithmClassName` exists
  props: #Properties configuration of load balance algorithm
      <property-name>: #property key value pair
  
props: #Property configuration
  sql.show: #Show SQL or not; default value: false
  executor.size: #Executing thread number; default value: CPU core number
  check.table.metadata.enabled: # Whether to check table metadata consistency when it initializes; default value: false
  max.connections.size.per.query: #The maximum connection number allocated by each query of each physical database. default value: 1
```

### data encryption
```yaml
dataSource: #Ignore data sources configuration

encryptRule:
  encryptors:
    <encrypt-algorithm-name>:
      type: #encrypt algorithm type
      props: #Properties, e.g. `aes.key.value` for AES encrypt algorithm
        aes.key.value: 
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: #plaintext column name
          cipherColumn: #ciphertext column name
          assistedQueryColumn: #AssistedColumns for query，when use QueryAssistedEncryptAlgorithm, it can help query encrypted data
          encryptorName: #encrypt name
```

### Orchestration

```yaml
dataSources: #Omit data source configurations
shardingRule: #Omit sharding rule configurations
masterSlaveRule: #Omit read-write split rule configurations
encryptRule: #Omit encrypt rule configurations

orchestration:
  orchestration_ds: #Orchestration instance name
    orchestrationType: #The type of orchestration center: config_center or registry_center or metadata_center
    instanceType: #Center instance type. Example:zookeeper
    serverLists: #The list of servers that connect to registry center, including IP and port number; use commas to seperate addresses, such as: host1:2181,host2:2181
    namespace: #Center namespace
    props: #Other properties
      overwrite: #Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
      digest: #The token that connects to the center; default means there is no need for authentication
      operationTimeoutMilliseconds: #Default value: 500 milliseconds
      maxRetries: #Maximum retry time after failing; default value: 3 times
      retryIntervalMilliseconds: #Interval time to retry; default value: 500 milliseconds
      timeToLiveSeconds: #Living time of temporary nodes; default value: 60 seconds
```

## Yaml Syntax Explanation

`!!` means instantiation of that class

`-` means one or multiple can be included

`[]` means array, substitutable with `-` 