+++
toc = true
title = "Spring Boot配置"
weight = 4
+++

## Spring Boot配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 配置示例

#### 分库分表
```yaml
sharding.jdbc.datasource.names=ds_0,ds_1

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/demo_ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/demo_ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds_${user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator-column-name=order_item_id
```

##### 分库分表配置项说明
同[分库分表Yaml配置](/06-sharding-jdbc/02-configuration/config-yaml)

#### 读写分离
```yaml
sharding.jdbc.datasource.names=ds_master,ds_slave_0,ds_slave_1

sharding.jdbc.datasource.ds_master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master.url=jdbc:mysql://localhost:3306/demo_ds_master
sharding.jdbc.datasource.ds_master.username=root
sharding.jdbc.datasource.ds_master.password=

sharding.jdbc.datasource.ds_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_0.url=jdbc:mysql://localhost:3306/demo_ds_slave_0
sharding.jdbc.datasource.ds_slave_0.username=root
sharding.jdbc.datasource.ds_slave_0.password=

sharding.jdbc.datasource.ds_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_1.url=jdbc:mysql://localhost:3306/demo_ds_slave_1
sharding.jdbc.datasource.ds_slave_1.username=root
sharding.jdbc.datasource.ds_slave_1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave_0,ds_slave_1

```

##### 读写分离配置项说明
同[读写分离Yaml配置](/06-sharding-jdbc/02-configuration/config-yaml)

#### 分库分表 + 读写分离
```java
sharding.jdbc.datasource.names=ds_master_0,ds_master_1,ds_master_0_slave_0,ds_master_0_slave_1,ds_master_1_slave_0,ds_master_1_slave_1

sharding.jdbc.datasource.ds_master_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0.url=jdbc:mysql://localhost:3306/demo_ds_master_0
sharding.jdbc.datasource.ds_master_0.username=root
sharding.jdbc.datasource.ds_master_0.password=

sharding.jdbc.datasource.ds_master_0_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0_slave_0.url=jdbc:mysql://localhost:3306/demo_ds_master_0_slave_0
sharding.jdbc.datasource.ds_master_0_slave_0.username=root
sharding.jdbc.datasource.ds_master_0_slave_0.password=
sharding.jdbc.datasource.ds_master_0_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0_slave_1.url=jdbc:mysql://localhost:3306/demo_ds_master_0_slave_1
sharding.jdbc.datasource.ds_master_0_slave_1.username=root
sharding.jdbc.datasource.ds_master_0_slave_1.password=

sharding.jdbc.datasource.ds_master_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1.url=jdbc:mysql://localhost:3306/demo_ds_master_1
sharding.jdbc.datasource.ds_master_1.username=root
sharding.jdbc.datasource.ds_master_1.password=

sharding.jdbc.datasource.ds_master_1_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1_slave_0.url=jdbc:mysql://localhost:3306/demo_ds_master_1_slave_0
sharding.jdbc.datasource.ds_master_1_slave_0.username=root
sharding.jdbc.datasource.ds_master_1_slave_0.password=
sharding.jdbc.datasource.ds_master_1_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1_slave_1.url=jdbc:mysql://localhost:3306/demo_ds_master_1_slave_1
sharding.jdbc.datasource.ds_master_1_slave_1.username=root
sharding.jdbc.datasource.ds_master_1_slave_1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds_${user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator-column-name=order_item_id

sharding.jdbc.config.sharding.master-slave-rules.ds_0.master-data-source-name=ds_master_0
sharding.jdbc.config.sharding.master-slave-rules.ds_0.slave-data-source-names=ds_master_0_slave_0, ds_master_0_slave_1
sharding.jdbc.config.sharding.master-slave-rules.ds_1.master-data-source-name=ds_master_1
sharding.jdbc.config.sharding.master-slave-rules.ds_1.slave-data-source-names=ds_master_1_slave_0, ds_master_1_slave_1

```


##### 配置项说明

##### 分库分表

##### YamlShardingRuleConfiguration

| *名称*                         | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| defaultDataSourceName?     | String      |   否   | 默认数据源名称，未配置分片规则的表将通过默认数据源定位                        |
| tables | Map\<String, YamlTableRuleConfiguration\> | 是 | 分表配置列表|
| defaultDatabaseStrategy? | YamlShardingStrategyConfiguration      |   否   | 默认分库策略  |
| defaultTableStrategy?    | YamlShardingStrategyConfiguration      |   否   | 默认分表策略  |
| defaultKeyGeneratorClass? | String |否|自增列值生成类名
| configMap                    |   Map\<String, Object\>         |   否   | 配置映射关系                                                            |
| props?                        |   Properties         |   否   | 相关属性配置     |
| bindingTables?            | List\<String\>      | 否| 绑定表列表|
| masterSlaveRules? | Map\<String, YamlMasterSlaveRuleConfiguration\>|否|读写分离配置|


##### YamlTableRuleConfiguration

| *名称*                         | *数据类型*  |  *必填* | *说明*  |
| --------------------         | ---------- | ------ | ------- |
| logicTable                 |  String     |   是   | 逻辑表名 |
| actualDataNodes             |  String     |   否   | 真实数据节点，由数据源名|
| databaseStrategy      |  YamlShardingStrategyConfiguration     |   否   | 分库策略  |
| tableStrategy            |  YamlShardingStrategyConfiguration     |   否   | 分表策略       |
| logicIndex                   |  String     |   否   | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表        |
| keyGeneratorColumnName | String | 否 | 自增列名|
| keyGeneratorClass  | String | 否| 自增列值生成类|


##### sharding.jdbc.config.sharding.default-table-strategy.standard

标准分片策略，用于单分片键的场景

| *名称*                        | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| sharding.jdbc.config.sharding.default-database-strategy.standard.sharding-column             |  String     |   是   | 分片列名                                                               |
| sharding.jdbc.config.sharding.default-database-strategy.standard.precise-algorithm-class-name      |  String     |   是   | 精确的分片算法类名称，用于=和IN。   |
| sharding.jdbc.config.sharding.default-database-strategy.standard.range-algorithm-class-name      |  String     |   否   | 范围的分片算法类名称，用于BETWEEN。 |


##### sharding.jdbc.config.sharding.default-table-strategy.complex

复合分片策略，用于多分片键的场景

| *名称*                        | *数据类型*  |  *必填* | *说明*                                              |
| ------------------------------ | ---------- | ------ | --------------------------------------------------- |
| sharding.jdbc.config.sharding.default-table-strategy.complex.sharding-columns             |  String     |   是  | 分片列名，多个列以逗号分隔                              |
| sharding.jdbc.config.sharding.default-table-strategy.complex.algorithm-class-name            |  String     |   是  | 分片算法全类名 |

##### sharding.jdbc.config.sharding.default-table-strategy.inline

inline表达式分片策略

| *名称*                         | *数据类型*  |  *必填* | *说明*       |
| ------------------------------- | ---------- | ------ | ------------ |
| sharding.jdbc.config.sharding.default-table-strategy.inline.sharding-column              |  String     |   是   | 分片列名      |
| sharding.jdbc.config.sharding.default-table-strategy.inline.algorithm-expression    |  String     |   是   | 分片算法表达式 |

##### sharding.jdbc.config.sharding.default-table-strategy.hint

Hint方式分片策略

| *名称*                         | *数据类型*  |  *必填* | *说明*                                              |
| ------------------------------- | ---------- | ------ | --------------------------------------------------- |
| sharding.jdbc.config.sharding.default-database-strategy.hint.algorithm-class-name            |  String     |   是  | 分片算法全类名 |

##### sharding.jdbc.config.sharding.default-database-strategy.none

不分片的策略

##### sharding.jdbc.config.sharding.props

| *名称*                               | *数据类型*  | *必填* | *说明*                              |
| ------------------------------------- | ---------- | ----- | ----------------------------------- |
| executor.size                       |  int       |   否   | 最大工作线程数量                      |

##### configMap

##### 读写分离

##### YamlMasterSlaveRuleConfiguration

| *名称*                        | *数据类型*  |  *必填* | *说明*                                   |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| name                        |  String     |   是   | 读写分离配置名称                          |
| masterDataSourceName      |   String        |   是   | 主库数据源                       |
| slaveDataSourceNames      |   Collection\<String\>       |   是   | 从库数据源列表       |
| loadBalanceAlgorithmType?               |  MasterSlaveLoadBalanceAlgorithmType     |   否   | 主从库复杂策略类型<br />可选值：ROUND_ROBIN, RANDOM<br />默认值：ROUND_ROBIN |
| loadBalanceAlgorithmClassName? | String | 否| 主从库复杂策略类名|
| configMap? | Map\<String, Object\> | 否 |配置映射关系|

##### configMap


#### 编排治理

##### Zookeeper配置示例

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

##### Etcd配置示例

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

##### 编排分库分表Spring Boot配置项说明
同[分库分表Yaml配置](/06-sharding-jdbc/02-configuration/config-yaml)
