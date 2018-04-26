+++
toc = true
title = "Spring Boot"
weight = 3
+++

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Example

### Sharding

```properties
sharding.jdbc.datasource.names=ds_0,ds_1

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds_$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds_$->{0..1}.t_order_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds_$->{0..1}.t_order_item_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator-column-name=order_item_id
```

### Read-write splitting

```properties
sharding.jdbc.datasource.names=ds_master,ds_slave_0,ds_slave_1

sharding.jdbc.datasource.ds_master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master.url=jdbc:mysql://localhost:3306/ds_master
sharding.jdbc.datasource.ds_master.username=root
sharding.jdbc.datasource.ds_master.password=

sharding.jdbc.datasource.ds_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_0.url=jdbc:mysql://localhost:3306/ds_slave_0
sharding.jdbc.datasource.ds_slave_0.username=root
sharding.jdbc.datasource.ds_slave_0.password=

sharding.jdbc.datasource.ds_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_1.url=jdbc:mysql://localhost:3306/ds_slave_1
sharding.jdbc.datasource.ds_slave_1.username=root
sharding.jdbc.datasource.ds_slave_1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave_0,ds_slave_1
```

### Sharding + Read-write splitting

```properties
sharding.jdbc.datasource.names=ds_master_0,ds_master_1,ds_master_0_slave_0,ds_master_0_slave_1,ds_master_1_slave_0,ds_master_1_slave_1

sharding.jdbc.datasource.ds_master_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0.url=jdbc:mysql://localhost:3306/ds_master_0
sharding.jdbc.datasource.ds_master_0.username=root
sharding.jdbc.datasource.ds_master_0.password=

sharding.jdbc.datasource.ds_master_0_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0_slave_0.url=jdbc:mysql://localhost:3306/ds_master_0_slave_0
sharding.jdbc.datasource.ds_master_0_slave_0.username=root
sharding.jdbc.datasource.ds_master_0_slave_0.password=
sharding.jdbc.datasource.ds_master_0_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_0_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_0_slave_1.url=jdbc:mysql://localhost:3306/ds_master_0_slave_1
sharding.jdbc.datasource.ds_master_0_slave_1.username=root
sharding.jdbc.datasource.ds_master_0_slave_1.password=

sharding.jdbc.datasource.ds_master_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1.url=jdbc:mysql://localhost:3306/ds_master_1
sharding.jdbc.datasource.ds_master_1.username=root
sharding.jdbc.datasource.ds_master_1.password=

sharding.jdbc.datasource.ds_master_1_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1_slave_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1_slave_0.url=jdbc:mysql://localhost:3306/ds_master_1_slave_0
sharding.jdbc.datasource.ds_master_1_slave_0.username=root
sharding.jdbc.datasource.ds_master_1_slave_0.password=
sharding.jdbc.datasource.ds_master_1_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master_1_slave_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master_1_slave_1.url=jdbc:mysql://localhost:3306/ds_master_1_slave_1
sharding.jdbc.datasource.ds_master_1_slave_1.username=root
sharding.jdbc.datasource.ds_master_1_slave_1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds_$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds_$->{0..1}.t_order_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds_$->{0..1}.t_order_item_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator-column-name=order_item_id

sharding.jdbc.config.sharding.master-slave-rules.ds_0.master-data-source-name=ds_master_0
sharding.jdbc.config.sharding.master-slave-rules.ds_0.slave-data-source-names=ds_master_0_slave_0, ds_master_0_slave_1
sharding.jdbc.config.sharding.master-slave-rules.ds_1.master-data-source-name=ds_master_1
sharding.jdbc.config.sharding.master-slave-rules.ds_1.slave-data-source-names=ds_master_1_slave_0, ds_master_1_slave_1
```

### Orchestration by Zookeeper

```properties
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
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_$->{0..1}.t_order_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_$->{0..1}.t_order_item_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

### Orchestration by Etcd

```properties
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
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_$->{0..1}.t_order_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_$->{0..1}.t_order_item_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

## Configuration reference

### Sharding

```properties
sharding.jdbc.datasource.names= #Names of data sources. Multiple data sources separated with comma

sharding.jdbc.datasource.<data_source_name>.type= #Class name of data source pool
sharding.jdbc.datasource.<data_source_name>.driver-class-name= #Class name of database driver
sharding.jdbc.datasource.<data_source_name>.url= #Database URL
sharding.jdbc.datasource.<data_source_name>.username= #Database username
sharding.jdbc.datasource.<data_source_name>.password= #Database password
sharding.jdbc.datasource.<data_source_name>.xxx= #Other properties for data source pool

sharding.jdbc.config.sharding.tables.<logic_table_name>.actual-data-nodes= #Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl_${0..7}

#Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.

#Standard sharding scenario for single sharding column
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.standard.sharding-column= #Name of sharding column
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.standard.preciseAlgorithmClassName= #Precise algorithm class name used for `=` and `IN`. No argument constructor required
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.standard.rangeAlgorithmClassName= #Range algorithm class name used for `BETWEEN`. No argument constructor required

#Complex sharding scenario for multiple sharding columns
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.complex.shardingColumns= #Names of sharding columns. Multiple columns separated with comma
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.complex.algorithmClassName= #Complex sharding algorithm class name. No argument constructor required

#Inline expression sharding scenario for single sharding column
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.inline.shardingColumn= #Name of sharding column
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.inline.algorithmInlineExpression= #Inline expression for sharding algorithm

#Hint sharding strategy
sharding.jdbc.config.sharding.tables.<logic_table_name>.database-strategy.hint.algorithmClassName= #Hint sharding algorithm class name. No argument constructor required

#Tables sharding strategy, Same as databases sharding strategy
sharding.jdbc.config.sharding.tables.<logic_table_name>.table-strategy.xxx= #Ignore

sharding.jdbc.config.sharding.tables.<logic_table_name>.key-generator-column-name= #Column name of key generator, do not use Key generator if absent
sharding.jdbc.config.sharding.tables.<logic_table_name>.key-generator-class= #Key generator, use default key generator if absent. No argument constructor required

sharding.jdbc.config.sharding.tables.<logic_table_name>.logic-index= #Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables

sharding.jdbc.config.sharding.binding-tables[0]= #Binding table rule configurations
sharding.jdbc.config.sharding.binding-tables[1]= #Binding table rule configurations
sharding.jdbc.config.sharding.binding-tables[x]= #Binding table rule configurations

sharding.jdbc.config.sharding.default-data-source-name= #If table not configure at table rule, will route to defaultDataSourceName
sharding.jdbc.config.sharding.default-database-strategy.xxx= #Default strategy for sharding databases, same as databases sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.xxx= #Default strategy for sharding tables, same as tables sharding strategy
sharding.jdbc.config.sharding.default-key-generator-class.xxx= #Default key generator class name, default value is `io.shardingjdbc.core.keygen.DefaultKeyGenerator`. No argument constructor required

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.master-data-source-name= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[0]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[1]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[x]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.load-balance-algorithm-class-name= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.load-balance-algorithm-type= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.key1= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.key2= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.keyx= #more details can reference Read-write splitting part

sharding.jdbc.config.sharding.props.sql.show= #To show SQLS or not, default value: false
sharding.jdbc.config.sharding.props.executor.size= #The number of working threads, default value: CPU count

sharding.jdbc.config.sharding.config.map.key1= #User-defined arguments
sharding.jdbc.config.sharding.config.map.key2= #User-defined arguments
sharding.jdbc.config.sharding.config.map.keyx= #User-defined arguments
```

### Read-write splitting

```properties
#Ignore data sources configuration, same as sharding

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.master-data-source-name= #Name of master data source
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[0]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[1]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.slave-data-source-names[x]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.load-balance-algorithm-class-name= #Load balance algorithm class name. No argument constructor required
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.load-balance-algorithm-type= #Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `load-balance-algorithm-class-name` is present 

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.key1= #User-defined arguments
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.key2= #User-defined arguments
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data_source_name>.config.map.keyx= #User-defined arguments
```

### Orchestration by Zookeeper

```properties
#Ignore data sources, sharding and read-write splitting configuration

sharding.jdbc.config.sharding.orchestration.name= #Name of orchestration instance
sharding.jdbc.config.sharding.orchestration.overwrite= #Use local configuration to overwrite registry center or not
sharding.jdbc.config.sharding.orchestration.type= #Data source type, values should be: `sharding` or `masterslave`
sharding.jdbc.config.sharding.orchestration.zookeeper.server-lists= #Zookeeper servers list, multiple split as comma. Example: host1:2181,host2:2181
sharding.jdbc.config.sharding.orchestration.zookeeper.namespace= #Namespace of zookeeper
sharding.jdbc.config.sharding.orchestration.zookeeper.base-sleep-time-milliseconds= #Initial milliseconds of waiting for retry, default value is 1000 milliseconds
sharding.jdbc.config.sharding.orchestration.zookeeper.max-sleep-time-milliseconds= #Maximum milliseconds of waiting for retry, default value is 3000 milliseconds
sharding.jdbc.config.sharding.orchestration.zookeeper.max-retries= #Max retries times if connect failure, default value is 3
sharding.jdbc.config.sharding.orchestration.zookeeper.session-timeout-milliseconds= #Session timeout milliseconds, default value is 60000 milliseconds
sharding.jdbc.config.sharding.orchestration.zookeeper.connection-timeout-milliseconds= #Connection timeout milliseconds, default value is 15000 milliseconds
sharding.jdbc.config.sharding.orchestration.zookeeper.digest= #Connection digest
```

### Orchestration by Etcd

```properties
#Ignore data sources, sharding and read-write splitting configuration

sharding.jdbc.config.sharding.orchestration.name= #Same as Zookeeper
sharding.jdbc.config.sharding.orchestration.overwrite= #Same as Zookeeper
sharding.jdbc.config.sharding.orchestration.type= #Same as Zookeeper
sharding.jdbc.config.sharding.orchestration.etcd.server-lists= #Etcd servers list, multiple split as comma. Example: http://host1:2379,http://host2:2379
sharding.jdbc.config.sharding.orchestration.etcd.time-to-live-seconds= #Time to live of data, default is 60 seconds
sharding.jdbc.config.sharding.orchestration.etcd.timeout-milliseconds= #Timeout milliseconds, default is 500 milliseconds
sharding.jdbc.config.sharding.orchestration.etcd.retry-interval-milliseconds= #Milliseconds of retry interval, default is w00 milliseconds
sharding.jdbc.config.sharding.orchestration.etcd.max-retries= #Max retries times if request failure, default value is 3
```
