+++
toc = true
title = "Yaml"
weight = 2
+++

## Example

### Sharding

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds_${user_id % 2}
  
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClass: io.shardingjdbc.core.keygen.DefaultKeyGenerator
  
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
  ds_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_0
    username: root
    password: 
  ds_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave_0
    - ds_slave_1
```

### Orchestration by Zookeeper

```yaml
#Ignore sharding and master-slave configuration

orchestration:
  name: orchestration_ds
  overwrite: true
  zookeeper:
    namespace: orchestration
    serverLists: localhost:2181
```

### Orchestration by Etcd

```yaml
#Ignore sharding and master-slave configuration

orchestration:
  name: orchestration_ds
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

## Configuration reference

### Sharding

```yaml
dataSources: #Data sources configuration, multiple `data_source_name` available
  <data_source_name>: #<!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: #Database driver class name
    url: #Database URL
    username: #Database username
    password: #Database password
    # ... Other properties for data source pool

shardingRule:
  tables: #Sharding rule configuration, multiple `logic_table_name` available
    <logic_table_name>: #Name of logic table
      actualDataNodes: #Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl_${0..7}
        
      databaseStrategy: #Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.
        standard: #Standard sharding scenario for single sharding column
          shardingColumn: #Name of sharding column
            preciseAlgorithmClassName: #Precise algorithm class name used for `=` and `IN`. No argument constructor required
            rangeAlgorithmClassName: #Precise algorithm class name used for `BETWEEN`. No argument constructor required
          complex: #Complex sharding scenario for multiple sharding columns
            shardingColumns: #Names of sharding columns. Multiple names separated with comma
            algorithmClassName: #Complex sharding algorithm class name. No argument constructor required
          inline: #Inline expression sharding scenario for single sharding column
            shardingColumn: #Name of sharding column
            algorithmInlineExpression: #Inline expression for sharding algorithm
          hint: #Hint sharding strategy
            algorithmClassName: #Hint sharding algorithm class name. No argument constructor required
           none: #Do not sharding
      tableStrategy: #Tables sharding strategy, Same as databases sharding strategy
        
      keyGeneratorColumnName: #Key generator column name, do not use Key generator if absent
      keyGeneratorClass: #Key generator, use default key generator if absent. No argument constructor required
        
      logicIndex: #Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables
  bindingTables: #Binding table rule configurations
  - <logic_table_name_1, logic_table_name_2, ...> 
  - <logic_table_name_3, logic_table_name_4, ...> 
  
  defaultDataSourceName: #If table not configure at table rule, will route to defaultDataSourceName  
  defaultDatabaseStrategy: #Default strategy for sharding databases, same as databases sharding strategy
  defaultTableStrategy: #Default strategy for sharding tables, same as tables sharding strategy
  defaultKeyGeneratorClass: #Default key generator class name, default value is `io.shardingjdbc.core.keygen.DefaultKeyGenerator`. No argument constructor required
  
  masterSlaveRules: #Read-write splitting rule configuration, more details can reference Read-write splitting part
    <data_source_name>: #Data sources configuration, need consist with data source map, multiple `data_source_name` available
      masterDataSourceName: #more details can reference Read-write splitting part
      slaveDataSourceNames: #more details can reference Read-write splitting part
      loadBalanceAlgorithmType: #more details can reference Read-write splitting part
      loadBalanceAlgorithmClassName: #more details can reference Read-write splitting part
      configMap: #User-defined arguments
          key1: value1
          key2: value2
  
  props: #Properties
    sql.show: #To show SQLS or not, default value: false
    executor.size: #The number of working threads, default value: CPU count
    
  configMap: #User-defined arguments
    key1: value1
    key2: value2
```

### Read-write splitting

```yaml
dataSources: #Ignore data sources configuration, same as sharding

masterSlaveRule:
  name: #Name of master slave data source
  masterDataSourceName: #Name of master data source
  slaveDataSourceNames: #Names of Slave data sources
    - <data_source_name_1>
    - <data_source_name_2>
  loadBalanceAlgorithmType: #Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`
  loadBalanceAlgorithmClassName: #Load balance algorithm class name. No argument constructor required
  
  configMap: #User-defined arguments
    key1: value1
    key2: value2
```

### Orchestration by Zookeeper

```yaml
dataSources: #Ignore data sources configuration
shardingRule: #Ignore sharding rule configuration
masterSlaveRule: #Ignore master slave rule configuration

orchestration:
  name: #Name of orchestration instance
  overwrite: #Use local configuration to overwrite registry center or not
  type: #Data source type, values should be: `sharding` or `masterslave`
  zookeeper: #Zookeeper configuration
    serverLists: #Zookeeper servers list, multiple split as comma. Example: host1:2181,host2:2181
    namespace: #Namespace of zookeeper
    baseSleepTimeMilliseconds: #Initial milliseconds of waiting for retry, default value is 1000 milliseconds
    maxSleepTimeMilliseconds: #Maximum milliseconds of waiting for retry, default value is 3000 milliseconds
    maxRetries: #Max retries times if connect failure, default value is 3
    sessionTimeoutMilliseconds: #Session timeout milliseconds
    connectionTimeoutMilliseconds: #Connection timeout milliseconds
    digest: #Connection digest
```

### Orchestration by Etcd

```yaml
dataSources: #Ignore data sources configuration
shardingRule: #Ignore sharding rule configuration
masterSlaveRule: #Ignore master slave rule configuration

orchestration:
  name: #Same as Zookeeper
  overwrite: #Same as Zookeeper
  type: #Same as Zookeeper
  etcd: #Etcd configuration
    serverLists: #Etcd servers list, multiple split as comma. Example: http://host1:2379,http://host2:2379
    timeToLiveSeconds: #Time to live of data, default is 60 seconds
    timeoutMilliseconds: #Timeout milliseconds, default is 500 milliseconds
    retryIntervalMilliseconds: #Milliseconds of retry interval, default is w00 milliseconds
    maxRetries: #Max retries times if request failure, default value is 3
```

## B.A.S.E Transaction

### Yaml configuration of asynchronous jobs

```yaml
#The target data source
targetDataSource:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password:
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:

#The data source of transaction logs
transactionLogDataSource:
  ds_trans: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trans_log
    username: root
    password:

#The registry configuration
zkConfig:
  #The url of the registry
  connectionString: localhost:2181
  
  #The namespace of jobs
  namespace: Best-Efforts-Delivery-Job
  
  #The initial value of the retry interval to connect to the registry
  baseSleepTimeMilliseconds: 1000
  
  #The max value of the retry interval to connect to the registry
  maxSleepTimeMilliseconds: 3000
  
  #The max number of retry to connect to the registry
  maxRetries: 3

#The job configuration
jobConfig:
  #The job name
  name: bestEffortsDeliveryJob
  
  #The cron expression to trigger jobs
  cron: 0/5 * * * * ?
  
  #The max number of transaction logs for each assignment
  transactionLogFetchDataCount: 100
  
  #The max number of retry to send the transactions
  maxDeliveryTryTimes: 3
  
  #The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs.
  maxDeliveryTryDelayMillis: 60000
```

## Yaml syntax

`!!` means class instantiation

`-` means one or multiple available

`[]` means array, can replace `-` each other
