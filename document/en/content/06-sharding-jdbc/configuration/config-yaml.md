+++
toc = true
title = "YAML"
weight = 3
+++


## YAML configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding 
```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo_ds_0
    username: root
    password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo_ds_1
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
      # t_order and t_order are all bindingTables of each other because of their same sharding strategies.
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  # The default sharding strategy
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

##### The config items for Sharding

```yaml
dataSources: # Config for data source
  <data_source_name> # Config for DB connection pool class. One or many configs are ok.
    driverClassName: # Class name for database driver.
    url: # The url for database connection.
    username: # Username used to access DB.
    password: # Password used to access DB.
    ... # Other configs for connection pool.

defaultDataSourceName: # Default datasource. Notice: Tables without sharding rules are accessed by using the default data source.

tables: # The config for sharding, One or many configs for logic_table_name are ok.
    <logic_table_name>: # Table name for LogicTables
        actualDataNodes: # Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs spliced with commas, supporting the inline expression. The default value is composed of configured datasources and logic table. This default config is to generate broadcast table (*The same table existed in every DB for cascade query*) or to split databases without spliting tables.
        databaseStrategy: # Strategy for sharding databases, only one strategy can be chosen from following strategies:
            standard: # Standard sharding strategy for single sharding column.
                shardingColumn: # Sharding Column
                preciseAlgorithmClassName: # The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.
                rangeAlgorithmClassName: # (Optional) The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed.
            complex: # Complex sharding strategy for multiple sharding columns.
                shardingColumns : # Sharding Column, multiple sharding columns spliced with commas. 
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            inline: inline # Inline sharding strategy.
                shardingColumn : # Sharding Column
                algorithmInlineExpression: #  The inline expression conformed to groovy dynamic syntax for sharding. 
            hint: # Hint sharding strategy
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            none: # No sharding
        tableStrategy: # Strategy for sharding tables. The details is same as Strategy for sharding databases.
  bindingTables: # Config for Blinding tables
  - A list of logic_table_name, multiple logic_table_names spliced with commas.
  
defaultDatabaseStrategy: # Default strategy for sharding databases. The details is same as databaseStrategy.
 
defaultTableStrategy: # Default strategy for sharding databases. The details is same as tableStrategy.

props: Property Configuration (Optional)
    sql.show: # To show SQL or not. Default: false
    executor.size: # The number of running thread. Default: The number of CPU cores.
```

#### The construction method for data source of Sharding

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

#### Read-write splitting Configuration

```yaml
dataSources:
  ds_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo_ds_master
    username: root
    password: 
  ds_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo_ds_slave_0
    username: root
    password: 
  ds_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo_ds_slave_1
    username: root
    password: 

masterSlaveRule:
  name: 
    ds_ms
  masterDataSourceName:
    ds_master
  slaveDataSourceNames: [ds_slave_0, ds_slave_1]

```

##### The config items for Read-write splitting

```yaml
dataSource: # Config for data sourc same as previous dataSource.

name: # Data source name for sharding.

masterDataSourceName: Datasource name for Master datasource

slaveDataSourceNames：Datasource name for Slave datasource, multiple datasource put in an Array.
```

##### The construction method for data source of Read-write splitting

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

##### More detail on YAML Configuration

!! :implementation class.

[] :multiple items.

(Refer to [YAML](http://yaml.org/))


##### Introduction for config items

##### Sharding

##### YamlShardingRuleConfiguration

| *Name*                        | *DataType*  |  *Required* | *Info*                                                                |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| defaultDataSourceName?     | String      |   N   | The default data source.                           |
| tables | Map\<String, YamlTableRuleConfiguration\> | Y |  The list of table rules. |
| defaultDatabaseStrategy? | YamlShardingStrategyConfiguration      |   N   | The default strategy for sharding databases.   |
| defaultTableStrategy?    | YamlShardingStrategyConfiguration      |   N   | The default strategy for sharding tables.   |
| defaultKeyGeneratorClass? | String |N|The class name of key generator.
| configMap?                    |   Map\<String, Object\>         |   N   |  config map.                                                            |
| props?                        |   Properties         |   N   | Property Config.     |
| bindingTables?            | List\<String\>      | N|  Blinding Rule|
| masterSlaveRules? | Map\<String, YamlMasterSlaveRuleConfiguration\>|N|The read-write-splitting configs.|


##### YamlTableRuleConfiguration

| *Name*                        | *DataType*  |  *Required* | *Info*  |
| --------------------         | ---------- | ------ | ------- |
| logicTable                 |  String     |   Y   | LogicTables. |
| actualDataNodes?             |  String     |   N   | Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs separated with commas.|
| databaseStrategy?      |  YamlShardingStrategyConfiguration     |   N   | The strategy for sharding databases.  |
| tableStrategy?            |  YamlShardingStrategyConfiguration     |   N   | The strategy for sharding tables.       |
| logicIndex?                   |  String     |   N   |The Logic index name. If you want to use *DROP INDEX XXX* SQL in Oracle/PostgreSQL，This property needs to be set for finding the actual tables.      |
| keyGeneratorColumnName? | String | N |  The generate column.|
| keyGeneratorClass?  | String | N| The class name of key generator.|


##### YamlStandardShardingStrategyConfiguration

The standard sharding strategy for single sharding column

| *Name*                        | *DataType*  |  *Required* | *Info*                                                               |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| shardingColumn             |  String     |   Y   | 分片列名                                                               |
| preciseAlgorithmClassName      |  String     |   Y   | The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.    |
| rangeAlgorithmClassName?      |  String     |   N   | The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed. |


##### YamlComplexShardingStrategyConfiguration

The complex sharding strategy for multiple sharding columns.

| *Name*                        | *DataType*  |  *Required* | *Info*                                             |
| ------------------------------ | ---------- | ------ | --------------------------------------------------- |
| shardingColumns             |  String     |   Y  | The name of sharding column. Multiple names separated with commas.                             |
| algorithmClassName             |  String     |   Y  | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

##### InlineShardingStrategyConfiguration

The inline-expression sharding strategy.

| *Name*                        | *DataType*  |  *Required* | *Info*       |
| ------------------------------- | ---------- | ------ | ------------ |
| shardingColumn              |  String     |   Y   | The name of sharding column.       |
| algorithmExpression    |  String     |   Y   | The expression for sharding algorithm. |

##### HintShardingStrategyConfiguration

The Hint-method sharding strategy.

| *Name*                        | *DataType*  |  *Required* | *Info*                                              |
| ------------------------------- | ---------- | ------ | --------------------------------------------------- |
| algorithmClassName            |  String     |   Y  |  The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

##### NoneShardingStrategyConfiguration

The none sharding strategy.

##### ShardingPropertiesConstant

| *Name*                        | *DataType*  |  *Required* | *Info*                             |
| -------------------------------- | ---------- | ----- | ----------------------------------- |
| sql.show                               |  boolean   |   Y   | To show SQLS or not, the default is false.   |
| executor.size?                         |  int       |   N   |  The number of running threads.                      |

##### configMap

##### Read-write-splitting

##### YamlMasterSlaveRuleConfiguration

| *Name*                        | *DataType*  |  *Required* | *Info*                                 |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| name                        |  String     |   Y   | The name of rule configuration.                              |
| masterDataSourceName      |   String        |   Y   | The master datasource.                        |
| slaveDataSourceNames      |   Collection\<String\>       |   Y   | The list of Slave databases, multiple items are separated by commas.         |
| loadBalanceAlgorithmType?               |  MasterSlaveLoadBalanceAlgorithmType     |   N   |  The complex strategy type of Master-Slaves. <br />The options: ROUND_ROBIN, RANDOM<br />. The default: ROUND_ROBIN |
| loadBalanceAlgorithmClassName? | String | N| The class name of load balance algorithm of master and slaves.|
| configMap? | Map\<String, Object\> | N |Config map.|

##### configMap

#### Orchestration

##### The introduction for orchestration configs of Sharding in Zookeeper
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
      #The strategies in other binding tables are same as the first binding table.
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
  #Defaut Sharding strategy
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  zookeeper:
    namespace: orchestration-yaml-demo
    serverLists: localhost:2181
```

##### The introduction for orchestration configs of Sharding in Etcd
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
  # The default strategy of Sharding 
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

##### The introduction for orchestration configs of Read-write splitting in Zookeeper

```yaml
dataSources: # The config of data source

shardingRule: # The config of Sharding rules

orchestration: # The orchestration configs in Zookeeper
  name: # The node name of the orchestration service
  overwrite: # to decide whether the local configuration can override the registry configuration. If true, the config in each boot is based on the local configuration.
  zookeeper: # The config of registry in Zookeeper
    namespace: # The namespace in Zookeeper
    serverLists: # The server list to connect to Zookeeper, including IP and port, mulitple addresses separated by commas, e.g. host1:2181,host2:2181.
    baseSleepTimeMilliseconds: # The initial value of the interval for retry, unit: Millisecond.
    maxSleepTimeMilliseconds: # The max value of the interval for retry, unit: Millisecond.
    maxRetries: # The number of retry. 
    sessionTimeoutMilliseconds: # Session timeout, unit: Millisecond.
    connectionTimeoutMilliseconds: # Connection timeout, unit: Millisecond.
    digest: # The permission token to connect to Zookeeper, and the default is no permission validation.
```

##### The introduction for orchestration configs of Read-write splitting in Etcd

```yaml
dataSources: 

shardingRule:  

orchestration:  
  name:  
  overwrite:  
  etcd: 
    serverLists: 
    timeToLiveSeconds: 
    timeoutMilliseconds: 
    maxRetries: 
    retryIntervalMilliseconds: 
```

##### Sharding DataSource Creation

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

##### Read-write splitting DataSource Creation

```java
    DataSource dataSource = OrchestrationMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```


#### B.A.S.E

##### The YAML configuration of asynchronous jobs

```yaml
# The target data source.
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

# The data source of transaction logs.
transactionLogDataSource:
  ds_trans: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trans_log
    username: root
    password:

# The registry configuration
zkConfig:
  # The url of the registry
  connectionString: localhost:2181
  
  # The namespace of jobs
  namespace: Best-Efforts-Delivery-Job
  
  # The inital value of the retry interval to connect to the registry.
  baseSleepTimeMilliseconds: 1000
  
  # The max value of the retry interval to connect to the registry.
  maxSleepTimeMilliseconds: 3000
  
  # The max number of retry to connect to the registry.
  maxRetries: 3

# The job configuration
jobConfig:
  # The job name
  name: bestEffortsDeliveryJob
  
  # The cron expression to trigger jobs
  cron: 0/5 * * * * ?
  
  # The max number of transaction logs for each assignment.
  transactionLogFetchDataCount: 100
  
  # The max number of retry to send the transactions.
  maxDeliveryTryTimes: 3
  
  # The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs.
  maxDeliveryTryDelayMillis: 60000
```
