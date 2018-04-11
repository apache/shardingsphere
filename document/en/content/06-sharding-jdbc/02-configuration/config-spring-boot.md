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

| *名称*                         | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| sharding.jdbc.config.sharding.default-data-source-name?     | String      |   否   | 默认数据源名称，未配置分片规则的表将通过默认数据源定位                        |
| sharding.jdbc.config.sharding.tables | Map\<String, YamlTableRuleConfiguration\> | 是 | 分表配置列表|
| sharding.jdbc.config.sharding.default-database-strategy? | YamlShardingStrategyConfiguration      |   否   | 默认分库策略  |
| sharding.jdbc.config.sharding.default-table-strategy?    | YamlShardingStrategyConfiguration      |   否   | 默认分表策略  |
| sharding.jdbc.config.sharding.default-key-generator-class? | String |否|自增列值生成类名
| sharding.jdbc.config.sharding.config-map?                    |   Map\<String, Object\>         |   否   | 配置映射关系                                                            |
| sharding.jdbc.config.sharding.props?                        |   Properties         |   否   | 相关属性配置     |
| sharding.jdbc.config.sharding.binding-tables?            | List\<String\>      | 否| 绑定表列表|
| sharding.jdbc.config.sharding.master-slave-rules? | Map\<String, YamlMasterSlaveRuleConfiguration\>|否|读写分离配置|

##### sharding.jdbc.datasource

| *名称*                         | *数据类型*  |  *必填* | *说明*  |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.datasource.names | String      |   是   | 数据源列表,多个以逗号分隔 |

##### sharding.jdbc.datasource.ds_name

| *名称*                         | *数据类型*  |  *必填* | *说明*  |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.datasource.ds_name.type| String | 是 | 数据源类型,例如：org.apache.commons.dbcp.BasicDataSource|
| sharding.jdbc.datasource.ds_name.driver-class-name | String|是 | 数据源驱动类名|
| sharding.jdbc.datasource.ds_name.url | String|是 |数据源链接url|
| sharding.jdbc.datasource.ds_name.username | String |是 | 数据源链接用户名|
| sharding.jdbc.datasource.ds_name.password | String |是 | 数据源链接密码|

##### sharding.jdbc.config.sharding.tables.tb_name

| *名称*                         | *数据类型*  |  *必填* | *说明*  |
| --------------------         | ---------- | ------ | ------- |
| sharding.jdbc.config.sharding.tables.tb_name.logic-table                 |  String     |   是   | 逻辑表名 |
| sharding.jdbc.config.sharding.tables.tb_name.actual-dataNodes?             |  String     |   否   | 真实数据节点|
| sharding.jdbc.config.sharding.tables.tb_name.database-strategy?      |  YamlShardingStrategyConfiguration     |   否   | 分库策略  |
| sharding.jdbc.config.sharding.tables.tb_name.table-strategy?            |  YamlShardingStrategyConfiguration     |   否   | 分表策略       |
| sharding.jdbc.config.sharding.tables.tb_name.logic-index?                   |  String     |   否   | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表        |
| sharding.jdbc.config.sharding.tables.tb_name.key-generator-columnName? | String | 否 | 自增列名|
| sharding.jdbc.config.sharding.tables.tb_name.key-generator-class?  | String | 否| 自增列值生成类|


##### sharding.jdbc.config.sharding.default-table-strategy.standard

标准分片策略，用于单分片键的场景

| *名称*                        | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| sharding.jdbc.config.sharding.default-database-strategy.standard.sharding-column             |  String     |   是   | 分片列名                                                               |
| sharding.jdbc.config.sharding.default-database-strategy.standard.precise-algorithm-class-name      |  String     |   是   | 精确的分片算法类名称，用于=和IN。   |
| sharding.jdbc.config.sharding.default-database-strategy.standard.range-algorithm-class-name?      |  String     |   否   | 范围的分片算法类名称，用于BETWEEN。 |


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

| *名称*                                   | *数据类型*  | *必填* | *说明*                              |
| ------------------------------------   | ---------- | ----- | ----------------------------------- |
| sharding.jdbc.config.sharding.props.sql.show       |  boolean   |   是   | 是否开启SQL显示，默认为false不开启     |
| sharding.jdbc.config.sharding.props.executor.size?                            |  int       |   否   | 最大工作线程数量                      |

##### configMap

##### Read-write splitting

##### sharding.jdbc.config.masterslave

| *名称*                        | *数据类型*  |  *必填* | *说明*                                   |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| sharding.jdbc.config.masterslave.name                        |  String     |   是   | 读写分离配置名称                          |
| sharding.jdbc.config.masterslave.master-data-sourceName      |   String        |   是   | 主库数据源                       |
| sharding.jdbc.config.masterslave.slave-data-source-names      |   Collection\<String\>       |   是   | 从库数据源列表       |
| sharding.jdbc.config.masterslave.load-balance-algorithm-type?               |  MasterSlaveLoadBalanceAlgorithmType     |   否   | 主从库访问策略类型<br />可选值：ROUND_ROBIN, RANDOM<br />默认值：ROUND_ROBIN |
| sharding.jdbc.config.masterslave.load-balance-algorithm-class-name? | String | 否| 主从库访问策略类名|
| sharding.jdbc.config.masterslave.config-map? | Map\<String, Object\> | 否 |配置映射关系|

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

