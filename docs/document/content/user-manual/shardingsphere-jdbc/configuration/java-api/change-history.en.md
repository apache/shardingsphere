+++
title = "Change History"
weight = 7
+++

## 5.0.0-alpha

### Replica Query

#### Root Configuration

Class name: ReplicaQueryRuleConfiguration

Attributes:

| *Name*            | *DataType*                                            | *Description*                                                          |
| ----------------- | ----------------------------------------------------- | ---------------------------------------------------------------------- |
| dataSources (+)   | Collection\<ReplicaQueryDataSourceRuleConfiguration\> | Data sources of primary and replicas                                   |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>   | Load balance algorithm name and configurations of replica data sources |

#### Replica Query Data Source Configuration

Class name: ReplicaQueryDataSourceRuleConfiguration

Attributes:

| *Name*                     | *DataType*           | *Description*                                  | *Default Value*                    |
| -------------------------- | -------------------- | ---------------------------------------------- | ---------------------------------- |
| name                       | String               | Replica query data source name                 | -                                  |
| primaryDataSourceName      | String               | Primary sources source name                    | -                                  |
| replicaDataSourceNames (+) | Collection\<String\> | Replica sources source name list               | -                                  |
| loadBalancerName (?)       | String               | Load balance algorithm name of replica sources | Round robin load balance algorithm |

Please refer to [Built-in Load Balance Algorithm List](/docs/document/content/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance.en.md) for more details about type of algorithm.

## ShardingSphere-4.x

### Readwrite-splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Mapping of data source and its name |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| props (?)             | Properties                   | Property configurations             |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| name                     | String                          | Readwrite-splitting data source name |
| masterDataSourceName     | String                          | Master database source name          |
| slaveDataSourceNames     | Collection<String>              | Slave database source name list      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Slave database load balance          |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| sql.show (?)                       | boolean     | Print SQL parse and rewrite log or not, default value: false |
| executor.size (?)                  | int         | Be used in work thread number implemented by SQL; no limits if it is 0. default value: 0 |
| max.connections.size.per.query (?) | int         | The maximum connection number allocated by each query of each physical database, default value: 1 |
| check.table.metadata.enabled (?)   | boolean     | Check meta-data consistency or not in initialization, default value: false |

## ShardingSphere-3.x

### Readwrite-splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Map of data sources and their names |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| configMap (?)         | Map<String, Object>          | Config map                          |
| props (?)             | Properties                   | Properties                          |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Description*                    |
| :----------------------- | :------------------------------ | :------------------------------- |
| name                     | String                          | Name of master slave data source |
| masterDataSourceName     | String                          | Name of master data source       |
| slaveDataSourceNames     | Collection<String>              | Names of Slave data sources      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Load balance algorithm           |

#### configMap

User-defined arguments.

#### PropertiesConstant

Enumeration of properties.

| *Name*                             | *DataType* | *Description*                                                |
| :--------------------------------- | :--------- | :----------------------------------------------------------- |
| sql.show (?)                       | boolean    | To show SQLS or not, default value: false                    |
| executor.size (?)                  | int        | The number of working threads, default value: CPU count      |
| max.connections.size.per.query (?) | int        | Max connection size for every query to every actual database. default value: 1 |
| check.table.metadata.enabled (?)   | boolean    | Check the metadata consistency of all the tables, default value : false |

## ShardingSphere-2.x

### Readwrite-splitting

#### concept

In order to relieve the pressure on the database, the write and read operations are separated into different data sources. The write library is called the master library, and the read library is called the slave library. One master library can be configured with multiple slave libraries.

#### Support item

1. Provides a readwrite-splitting configuration with one master and multiple slaves, which can be used independently or with sub-databases and sub-meters.
2. Independent use of readwrite-splitting to support SQL transparent transmission.
3. In the same thread and the same database connection, if there is a write operation, subsequent read operations will be read from the main library to ensure data consistency.
4. Spring namespace.
5. Hint-based mandatory main library routing.

#### Unsupported item

1. Data synchronization between the master library and the slave library.
2. Data inconsistency caused by the data synchronization delay of the master library and the slave library.
3. Double writing or multiple writing in the main library.

#### Code development example

##### only readwrite-splitting

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource", masterDataSource);
dataSourceMap.put("slaveDataSource0", slaveDataSource0);
dataSourceMap.put("slaveDataSource1", slaveDataSource1);

// Constructing readwrite-splitting configuration
MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig.setName("ms_ds");
masterSlaveRuleConfig.setMasterDataSourceName("masterDataSource");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource0");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource1");

DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig);
```

##### sharding table and database + readwrite-splitting

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource0", masterDataSource0);
dataSourceMap.put("slaveDataSource00", slaveDataSource00);
dataSourceMap.put("slaveDataSource01", slaveDataSource01);

dataSourceMap.put("masterDataSource1", masterDataSource1);
dataSourceMap.put("slaveDataSource10", slaveDataSource10);
dataSourceMap.put("slaveDataSource11", slaveDataSource11);

// Constructing readwrite-splitting configuration
MasterSlaveRuleConfiguration masterSlaveRuleConfig0 = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig0.setName("ds_0");
masterSlaveRuleConfig0.setMasterDataSourceName("masterDataSource0");
masterSlaveRuleConfig0.getSlaveDataSourceNames().add("slaveDataSource00");
masterSlaveRuleConfig0.getSlaveDataSourceNames().add("slaveDataSource01");

MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig1.setName("ds_1");
masterSlaveRuleConfig1.setMasterDataSourceName("masterDataSource1");
masterSlaveRuleConfig1.getSlaveDataSourceNames().add("slaveDataSource10");
masterSlaveRuleConfig1.getSlaveDataSourceNames().add("slaveDataSource11");

// Continue to create ShardingDataSource through ShardingSlaveDataSourceFactory
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig0);
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig1);

DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## ShardingSphere-1.x

### Readwrite-splitting

#### concept

In order to relieve the pressure on the database, the write and read operations are separated into different data sources. The write library is called the master library, and the read library is called the slave library. One master library can be configured with multiple slave libraries.

#### Support item

1. Provides a readwrite-splitting configuration with one master and multiple slaves, which can be used independently or with sub-databases and sub-meters.
2. In the same thread and the same database connection, if there is a write operation, subsequent read operations will be read from the main library to ensure data consistency.
3. Spring namespace.
4. Hint-based mandatory main library routing.

#### Unsupported item

1. Data synchronization between the master library and the slave library.
2. Data inconsistency caused by the data synchronization delay of the master library and the slave library.
3. Double writing or multiple writing in the main library.

#### Code development example

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> slaveDataSourceMap0 = new HashMap<>();
slaveDataSourceMap0.put("slaveDataSource00", slaveDataSource00);
slaveDataSourceMap0.put("slaveDataSource01", slaveDataSource01);
// You can choose the master-slave library load balancing strategy, the default is ROUND_ROBIN, and there is RANDOM to choose from, or customize the load strategy
DataSource masterSlaveDs0 = MasterSlaveDataSourceFactory.createDataSource("ms_0", "masterDataSource0", masterDataSource0, slaveDataSourceMap0, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

Map<String, DataSource> slaveDataSourceMap1 = new HashMap<>();
slaveDataSourceMap1.put("slaveDataSource10", slaveDataSource10);
slaveDataSourceMap1.put("slaveDataSource11", slaveDataSource11);
DataSource masterSlaveDs1 = MasterSlaveDataSourceFactory.createDataSource("ms_1", "masterDataSource1", masterDataSource1, slaveDataSourceMap1, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

// Constructing readwrite-splitting configuration
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ms_0", masterSlaveDs0);
dataSourceMap.put("ms_1", masterSlaveDs1);

// Continue to create ShardingDataSource through ShardingSlaveDataSourceFactory
```