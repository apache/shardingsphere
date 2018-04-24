+++
toc = true
title = "Spring Boot"
weight = 4
+++

## Spring Boot configuration

### Import the dependency of maven


```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding 
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

##### The details on some Config options for Sharding 
Refer to [The Yaml Configuration for Sharding](/06-sharding-jdbc/02-configuration/config-yaml)

#### Read-write splitting
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

##### The details on some Config options for Read-write splitting
Refer to [The Yaml Configuration for Sharding](/06-sharding-jdbc/02-configuration/config-yaml)

#### Sharding + Read-write splitting

```yaml
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


##### Introduction for config items

##### Sharding

##### sharding.jdbc.config.sharding

| *Name*                         | *DataType*  |  *Required* | *Info*                                                                       |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| sharding.jdbc.config.sharding.default-data-source-name?     | String      |   N   | The default data source.                        |
| sharding.jdbc.config.sharding.tables | Map\<String, YamlTableRuleConfiguration\> | Y | The list of table rules.|
| sharding.jdbc.config.sharding.default-database-strategy? | YamlShardingStrategyConfiguration      |   N   | The default strategy for sharding databases.  |
| sharding.jdbc.config.sharding.default-table-strategy?    | YamlShardingStrategyConfiguration      |   N   | The default strategy for sharding tables.  |
| sharding.jdbc.config.sharding.default-key-generator-class? | String |N|The class name of key generator.
| sharding.jdbc.config.sharding.config-map?                    |   Map\<String, Object\>         |   N   | Config map.                                                            |
| sharding.jdbc.config.sharding.props?                        |   Properties         |   N   | Property Config.     |
| sharding.jdbc.config.sharding.binding-tables?            | List\<String\>      | N| Blinding Rule.|
| sharding.jdbc.config.sharding.master-slave-rules? | Map\<String, YamlMasterSlaveRuleConfiguration\>|N|The read-write-splitting configs|

##### sharding.jdbc.datasource

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.datasource.names | String      |   Y   |The list of datasource. |

##### sharding.jdbc.datasource.ds_name

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.datasource.ds_name.type| String | Y | The type of datasource e.g. org.apache.commons.dbcp.BasicDataSource|
| sharding.jdbc.datasource.ds_name.driver-class-name | String|Y | The driver class name|
| sharding.jdbc.datasource.ds_name.url | String|Y |The connection url of datasource|
| sharding.jdbc.datasource.ds_name.username | String |Y | Connection username|
| sharding.jdbc.datasource.ds_name.password | String |Y | Connection password|

##### sharding.jdbc.config.sharding.tables.tb_name

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.config.sharding.tables.tb_name.logic-table                 |  String     |   Y   | LogicTables |
| sharding.jdbc.config.sharding.tables.tb_name.actual-dataNodes?             |  String     |   N   | Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs separated with commas.|
| sharding.jdbc.config.sharding.tables.tb_name.database-strategy?      |  YamlShardingStrategyConfiguration     |   N   | The strategy for sharding database.  |
| sharding.jdbc.config.sharding.tables.tb_name.table-strategy?            |  YamlShardingStrategyConfiguration     |   N   | The strategy for sharding table.       |
| sharding.jdbc.config.sharding.tables.tb_name.logic-index?                   |  String     |   N   | The Logic index name. If you want to use *DROP INDEX XXX* SQL in Oracle/PostgreSQL，This property needs to be set for finding the actual tables.       |
| sharding.jdbc.config.sharding.tables.tb_name.key-generator-columnName? | String | N | The generate column|
| sharding.jdbc.config.sharding.tables.tb_name.key-generator-class?  | String | N| The class name of key generator.|


##### sharding.jdbc.config.sharding.default-table-strategy.standard

The standard sharding strategy for single sharding column.

| *Name*                         | *DataType*  |  *Required* | *Info*                                                                    |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| sharding.jdbc.config.sharding.default-database-strategy.standard.sharding-column             |  String     |   Y   |  The name of sharding column.                                                                  |
| sharding.jdbc.config.sharding.default-database-strategy.standard.precise-algorithm-class-name      |  String     |   Y   | The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.     |
| sharding.jdbc.config.sharding.default-database-strategy.standard.range-algorithm-class-name?      |  String     |   N   | The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed.  |


##### sharding.jdbc.config.sharding.default-table-strategy.complex

The complex sharding strategy for multiple sharding columns.

| *Name*                         | *DataType*  |  *Required* | *Info*         |                                             
| ------------------------------ | ---------- | ------ | --------------------------------------------------- |
| sharding.jdbc.config.sharding.default-table-strategy.complex.sharding-columns             |  String     |   Y  | The name of sharding column. Multiple names separated with commas.                              |
| sharding.jdbc.config.sharding.default-table-strategy.complex.algorithm-class-name            |  String     |   Y  | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

##### sharding.jdbc.config.sharding.default-table-strategy.inline

The inline-expression sharding strategy.

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| ------------------------------- | ---------- | ------ | ------------ |
| sharding.jdbc.config.sharding.default-table-strategy.inline.sharding-column              |  String     |   Y   | The  name of sharding column.       |
| sharding.jdbc.config.sharding.default-table-strategy.inline.algorithm-expression    |  String     |   Y   | The expression for sharding algorithm. |

##### sharding.jdbc.config.sharding.default-table-strategy.hint

The Hint-method sharding strategy.

| *Name*                         | *DataType*  |  *Required* | *Info*                                          |
| ------------------------------- | ---------- | ------ | --------------------------------------------------- |
| sharding.jdbc.config.sharding.default-database-strategy.hint.algorithm-class-name            |  String     |   Y  | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.  |

##### sharding.jdbc.config.sharding.default-database-strategy.none

The none sharding strategy.

##### sharding.jdbc.config.sharding.props

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| ------------------------------------   | ---------- | ----- | ----------------------------------- |
| sharding.jdbc.config.sharding.props.sql.show       |  boolean   |   Y   | To show SQLS or not, the default is false.     |
| sharding.jdbc.config.sharding.props.executor.size?                            |  int       |   N   | The number of running threads.                        |

##### configMap

##### Read-write splitting

##### sharding.jdbc.config.masterslave

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| sharding.jdbc.config.masterslave.name                        |  String     |   Y   | The name of rule configuration.                         |
| sharding.jdbc.config.masterslave.master-data-sourceName      |   String        |   Y   | The master datasource.                         |
| sharding.jdbc.config.masterslave.slave-data-source-names      |   Collection\<String\>       |   Y   |  The list of Slave databases, multiple items are separated by commas.        |
| sharding.jdbc.config.masterslave.load-balance-algorithm-type?               |  MasterSlaveLoadBalanceAlgorithmType     |   N   | The complex strategy type of Master-Slaves. <br />The options: ROUND_ROBIN, RANDOM<br />. The default: ROUND_ROBIN|
| sharding.jdbc.config.masterslave.load-balance-algorithm-class-name? | String | N| The class name of load balance algorithm for master and slaves.|
| sharding.jdbc.config.masterslave.config-map? | Map\<String, Object\> | N | Config map.|

##### configMap


#### Orchestration

##### Zookeeper

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

##### Etcd

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

##### The details on some Config options for Orchestration
Refer to [The Yaml Configuration for Sharding](/06-sharding-jdbc/02-configuration/config-yaml)

