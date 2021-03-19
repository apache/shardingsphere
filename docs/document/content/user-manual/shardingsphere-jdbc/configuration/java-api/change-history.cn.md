+++
title = "变更历史"
weight = 7
+++

## ShardingSphere-5.0.0-alpha

### 读写分离

#### 配置入口

类名称：ReplicaQueryRuleConfiguration

可配置属性：

| *名称*             | *数据类型*                                             | *说明*            |
| ----------------- | ----------------------------------------------------- | ----------------- |
| dataSources (+)   | Collection\<ReplicaQueryDataSourceRuleConfiguration\> | 主从数据源配置      |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>   | 从库负载均衡算法配置 |

#### 主从数据源配置

类名称：ReplicaQueryDataSourceRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*             | *说明*             | *默认值*       |
| -------------------------- | -------------------- | ------------------ | ------------- |
| name                       | String               | 读写分离数据源名称   | -             |
| primaryDataSourceName      | String               | 主库数据源名称      | -              |
| replicaDataSourceNames (+) | Collection\<String\> | 从库数据源名称列表   | -              |
| loadBalancerName (?)       | String               | 从库负载均衡算法名称 | 轮询负载均衡算法 |

算法类型的详情，请参见[内置负载均衡算法列表](/docs/document/content/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance.cn.md)。

## ShardingSphere-4.x

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                | *数据类型*                   | *说明*               |
| :-------------------- | :--------------------------- | :------------------- |
| dataSourceMap         | Map<String, DataSource>      | 数据源与其名称的映射 |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则         |
| props (?)             | Properties                   | 属性配置             |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                   | *数据类型*                      | *说明*             |
| :----------------------- | :------------------------------ | :----------------- |
| name                     | String                          | 读写分离数据源名称 |
| masterDataSourceName     | String                          | 主库数据源名称     |
| slaveDataSourceNames     | Collection<String>              | 从库数据源名称列表 |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法   |

#### Properties

属性配置项，可以为以下属性。

| *名称*                             | *数据类型* | *说明*                                                 |
| :--------------------------------- | :--------- | :----------------------------------------------------- |
| sql.show (?)                       | boolean    | 是否打印SQL解析和改写日志，默认值: false               |
| executor.size (?)                  | int        | 用于SQL执行的工作线程数量，为零则表示无限制。默认值: 0 |
| max.connections.size.per.query (?) | int        | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1  |
| check.table.metadata.enabled (?)   | boolean    | 是否在启动时检查分表元数据一致性，默认值: false        |

## ShardingSphere-3.x

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                | *数据类型*                   | *说明*               |
| :-------------------- | :--------------------------- | :------------------- |
| dataSourceMap         | Map<String, DataSource>      | 数据源与其名称的映射 |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则         |
| configMap (?)         | Map<String, Object>          | 用户自定义配置       |
| props (?)             | Properties                   | 属性配置             |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                   | *数据类型*                      | *说明*             |
| :----------------------- | :------------------------------ | :----------------- |
| name                     | String                          | 读写分离数据源名称 |
| masterDataSourceName     | String                          | 主库数据源名称     |
| slaveDataSourceNames     | Collection<String>              | 从库数据源名称列表 |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法   |

#### configMap

用户自定义配置。

#### PropertiesConstant

属性配置项，可以为以下属性。

| *名称*                             | *数据类型* | *说明*                                                 |
| :--------------------------------- | :--------- | :----------------------------------------------------- |
| sql.show (?)                       | boolean    | 是否打印SQL解析和改写日志，默认值: false               |
| executor.size (?)                  | int        | 用于SQL执行的工作线程数量，为零则表示无限制。默认值: 0 |
| max.connections.size.per.query (?) | int        | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1  |
| check.table.metadata.enabled (?)   | boolean    | 是否在启动时检查分表元数据一致性，默认值: false        |

## ShardingSphere-2.x

### 读写分离

#### 概念

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。

#### 支持项

1. 提供了一主多从的读写分离配置，可独立使用，也可配合分库分表使用。
2. 独立使用读写分离支持SQL透传。
3. 同一线程且同一数据库连接内，如有写入操作，以后的读操作均从主库读取，用于保证数据一致性。
4. Spring命名空间。
5. 基于Hint的强制主库路由。

#### 不支持范围

1. 主库和从库的数据同步。
2. 主库和从库的数据同步延迟导致的数据不一致。
3. 主库双写或多写。

#### 代码开发示例

##### 仅读写分离

```java
// 构建读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理. masterDataSource, slaveDataSource0, slaveDataSource1等为使用DBCP等连接池配置的真实数据源
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource", masterDataSource);
dataSourceMap.put("slaveDataSource0", slaveDataSource0);
dataSourceMap.put("slaveDataSource1", slaveDataSource1);

// 构建读写分离配置
MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig.setName("ms_ds");
masterSlaveRuleConfig.setMasterDataSourceName("masterDataSource");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource0");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource1");

DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig);
```

##### 分库分表 + 读写分离

```java
// 构建读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理. masterDataSource0, slaveDataSource00, slaveDataSource01等为使用DBCP等连接池配置的真实数据源
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource0", masterDataSource0);
dataSourceMap.put("slaveDataSource00", slaveDataSource00);
dataSourceMap.put("slaveDataSource01", slaveDataSource01);

dataSourceMap.put("masterDataSource1", masterDataSource1);
dataSourceMap.put("slaveDataSource10", slaveDataSource10);
dataSourceMap.put("slaveDataSource11", slaveDataSource11);

// 构建读写分离配置
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

// 通过ShardingSlaveDataSourceFactory继续创建ShardingDataSource
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig0);
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig1);

DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## ShardingSphere-1.x

### 读写分离

#### 概念

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。

#### 支持项

1. 提供了一主多从的读写分离配置，可独立使用，也可配合分库分表使用。
2. 同一线程且同一数据库连接内，如有写入操作，以后的读操作均从主库读取，用于保证数据一致性。
3. Spring命名空间。
4. 基于Hint的强制主库路由。

#### 不支持范围

1. 主库和从库的数据同步。
2. 主库和从库的数据同步延迟导致的数据不一致。
3. 主库双写或多写。

#### 代码开发示例

```java
// 构建读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理. masterDataSource0, slaveDataSource00, slaveDataSource01等为使用DBCP等连接池配置的真实数据源
Map<String, DataSource> slaveDataSourceMap0 = new HashMap<>();
slaveDataSourceMap0.put("slaveDataSource00", slaveDataSource00);
slaveDataSourceMap0.put("slaveDataSource01", slaveDataSource01);
// 可选择主从库负载均衡策略, 默认是ROUND_ROBIN, 还有RANDOM可以选择, 或者自定义负载策略
DataSource masterSlaveDs0 = MasterSlaveDataSourceFactory.createDataSource("ms_0", "masterDataSource0", masterDataSource0, slaveDataSourceMap0, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

Map<String, DataSource> slaveDataSourceMap1 = new HashMap<>();
slaveDataSourceMap1.put("slaveDataSource10", slaveDataSource10);
slaveDataSourceMap1.put("slaveDataSource11", slaveDataSource11);
DataSource masterSlaveDs1 = MasterSlaveDataSourceFactory.createDataSource("ms_1", "masterDataSource1", masterDataSource1, slaveDataSourceMap1, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

// 构建分库分表数据源
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ms_0", masterSlaveDs0);
dataSourceMap.put("ms_1", masterSlaveDs1);

// 通过ShardingDataSourceFactory继续创建ShardingDataSource
```