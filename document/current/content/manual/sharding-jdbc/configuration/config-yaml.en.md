+++
toc = true
title = "Yaml"
weight = 2
+++

## Example

### Sharding

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
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
props:
  sql.show: true
```

### Read-write splitting

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

### Sharding + Read-write splitting

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
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ms_ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
  masterSlaveRules:
      ms_ds0:
        masterDataSourceName: ds0
        slaveDataSourceNames:
          - ds0_slave0
          - ds0_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key0: master-slave-value0
      ms_ds1:
        masterDataSourceName: ds1
        slaveDataSourceNames: 
          - ds1_slave0
          - ds1_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key1: master-slave-value1

props:
  sql.show: true
```

### Orchestration

```yaml
#Ignore sharding and master-slave configuration

orchestration:
  name: orchestration_ds
  overwrite: true
  registry:
    namespace: orchestration
    serverLists: localhost:2181
```

## Configuration reference

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
        
      keyGeneratorColumnName: #Column name of key generator, do not use Key generator if absent
      keyGeneratorClassName: #Key generator, use default key generator if absent. This class need to implements KeyGenerator, and require a no argument constructor
        
      logicIndex: #Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables
  bindingTables: #Binding table rule configurations
  - <logic_table_name1, logic_table_name2, ...> 
  - <logic_table_name3, logic_table_name4, ...>
  - <logic_table_name_x, logic_table_name_y, ...>
  bindingTables: #Broadcast table rule configurations
  - table_name1
  - table_name2
  - table_name_x
  
  defaultDataSourceName: #If table not configure at table rule, will route to defaultDataSourceName  
  defaultDatabaseStrategy: #Default strategy for sharding databases, same as databases sharding strategy
  defaultTableStrategy: #Default strategy for sharding tables, same as tables sharding strategy
  defaultKeyGeneratorClassName: #Default key generator class name, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator`. This class need to implements KeyGenerator, and require a no argument constructor
  
  masterSlaveRules: #Read-write splitting rule configuration, more details can reference Read-write splitting part
    <data_source_name>: #Data sources configuration, need consist with data source map, multiple `data_source_name` available
      masterDataSourceName: #more details can reference Read-write splitting part
      slaveDataSourceNames: #more details can reference Read-write splitting part
      loadBalanceAlgorithmType: #more details can reference Read-write splitting part
      loadBalanceAlgorithmClassName: #more details can reference Read-write splitting part
      configMap: #User-defined arguments
          key1: value1
          key2: value2
          keyx: valuex
  
props: #Properties
  sql.show: #To show SQLS or not, default value: false
  executor.size: #The number of working threads, default value: CPU count
  check.table.metadata.enabled: #To check the metadata consistency of all the tables or not, default value : false
    
configMap: #User-defined arguments
  key1: value1
  key2: value2
  keyx: valuex
```

### Read-write splitting

```yaml
dataSources: #Ignore data sources configuration, same as sharding

masterSlaveRule:
  name: #Name of master slave data source
  masterDataSourceName: #Name of master data source
  slaveDataSourceNames: #Names of Slave data sources
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmClassName: #Load balance algorithm class name. This class need to implements MasterSlaveLoadBalanceAlgorithm, and require a no argument constructor
  loadBalanceAlgorithmType: #Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `loadBalanceAlgorithmClassName` is present
  
props: #Properties
  sql.show: #To show SQLS or not, default value: false
  executor.size: #The number of working threads, default value: CPU count
  check.table.metadata.enabled: #To check the metadata consistency of all the tables or not, default value : false

configMap: #User-defined arguments
  key1: value1
  key2: value2
  keyx: valuex
```

### Orchestration

```yaml
dataSources: #Ignore data sources configuration
shardingRule: #Ignore sharding rule configuration
masterSlaveRule: #Ignore master slave rule configuration

orchestration:
  name: #Name of orchestration instance
  overwrite: #Use local configuration to overwrite registry center or not
  registry: #Registry configuration
    serverLists: #Registry servers list, multiple split as comma. Example: host1:2181,host2:2181
    namespace: #Namespace of registry
    digest: #Digest for registry. Default is not need digest.
    operationTimeoutMilliseconds: #Operation timeout time in milliseconds, default value is 500 milliseconds
    maxRetries: #Max number of times to retry, default value is 3
    retryIntervalMilliseconds: #Time interval in milliseconds on each retry, default value is 500 milliseconds
    timeToLiveSeconds: #Time to live in seconds of ephemeral keys, default value is 60 seconds
```

## Yaml syntax

`!!` means class instantiation

`-` means one or multiple available

`[]` means array, can replace `-` each other
