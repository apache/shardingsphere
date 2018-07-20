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
sharding.jdbc.datasource.names=ds0,ds1

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id
```

### Read-write splitting

```properties
sharding.jdbc.datasource.names=ds_master,ds_slave0,ds_slave1

sharding.jdbc.datasource.ds-master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-master.url=jdbc:mysql://localhost:3306/ds_master
sharding.jdbc.datasource.ds-master.username=root
sharding.jdbc.datasource.ds-master.password=

sharding.jdbc.datasource.ds-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave0.url=jdbc:mysql://localhost:3306/ds_slave0
sharding.jdbc.datasource.ds-slave0.username=root
sharding.jdbc.datasource.ds-slave0.password=

sharding.jdbc.datasource.ds-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds-slave1.url=jdbc:mysql://localhost:3306/ds_slave1
sharding.jdbc.datasource.ds-slave1.username=root
sharding.jdbc.datasource.ds-slave1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave0,ds_slave1
```

### Sharding + Read-write splitting

```properties
sharding.jdbc.datasource.names=ds_master0,ds_master1,ds_master0_slave0,ds_master0_slave1,ds_master1_slave0,ds_master1_slave1

sharding.jdbc.datasource.d-master0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master0.url=jdbc:mysql://localhost:3306/ds_master0
sharding.jdbc.datasource.d-master0.username=root
sharding.jdbc.datasource.d-master0.password=

sharding.jdbc.datasource.d-master0-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master0-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master0-slave0.url=jdbc:mysql://localhost:3306/ds_master0_slave0
sharding.jdbc.datasource.d-master0-slave0.username=root
sharding.jdbc.datasource.d-master0-slave0.password=
sharding.jdbc.datasource.d-master0-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master0-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master0-slave1.url=jdbc:mysql://localhost:3306/ds_master0_slave1
sharding.jdbc.datasource.d-master0-slave1.username=root
sharding.jdbc.datasource.d-master0-slave1.password=

sharding.jdbc.datasource.d-master1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master1.url=jdbc:mysql://localhost:3306/ds_master1
sharding.jdbc.datasource.d-master1.username=root
sharding.jdbc.datasource.d-master1.password=

sharding.jdbc.datasource.d-master1-slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master1-slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master1-slave0.url=jdbc:mysql://localhost:3306/ds_master1_slave0
sharding.jdbc.datasource.d-master1-slave0.username=root
sharding.jdbc.datasource.d-master1-slave0.password=
sharding.jdbc.datasource.d-master1-slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.d-master1-slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.d-master1-slave1.url=jdbc:mysql://localhost:3306/ds_master1_slave1
sharding.jdbc.datasource.d-master1-slave1.username=root
sharding.jdbc.datasource.d-master1-slave1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.sharding.master-slave-rules.ds0.master-data-source-name=ds_master0
sharding.jdbc.config.sharding.master-slave-rules.ds0.slave-data-source-names=ds_master0_slave0, ds_master0_slave1
sharding.jdbc.config.sharding.master-slave-rules.ds1.master-data-source-name=ds_master1
sharding.jdbc.config.sharding.master-slave-rules.ds1.slave-data-source-names=ds_master1_slave0, ds_master1_slave1
```

### Orchestration by Zookeeper

```properties
sharding.jdbc.datasource.names=ds,ds0,ds1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-inline-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-inline-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

### Orchestration by Etcd

```properties
sharding.jdbc.datasource.names=ds,ds0,ds1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t-order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order.table-strategy.inline.algorithm-inline-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order.key-generator-column-name=order_id
sharding.jdbc.config.sharding.tables.t-order-item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t-order-item.table-strategy.inline.algorithm-inline-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t-order-item.key-generator-column-name=order_item_id

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

## Configuration reference

### Sharding

```properties
sharding.jdbc.datasource.names= #Names of data sources. Multiple data sources separated with comma

sharding.jdbc.datasource.<data-source-name>.type= #Class name of data source pool
sharding.jdbc.datasource.<data-source-name>.driver-class-name= #Class name of database driver
sharding.jdbc.datasource.<data-source-name>.url= #Database URL
sharding.jdbc.datasource.<data-source-name>.username= #Database username
sharding.jdbc.datasource.<data-source-name>.password= #Database password
sharding.jdbc.datasource.<data-source-name>.xxx= #Other properties for data source pool

sharding.jdbc.config.sharding.tables.<logic-table-name>.actual-data-nodes= #Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7}

#Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.

#Standard sharding scenario for single sharding column
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #Name of sharding column
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #Precise algorithm class name used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm, and require a no argument constructor
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #Range algorithm class name used for `BETWEEN`. This class need to implements RangeShardingAlgorithm, and require a no argument constructor

#Complex sharding scenario for multiple sharding columns
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #Names of sharding columns. Multiple columns separated with comma
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #Complex sharding algorithm class name. This class need to implements ComplexKeysShardingAlgorithm, and require a no argument constructor

#Inline expression sharding scenario for si-gle s-arding column
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #Name of sharding column
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-inline-expression= #Inline expression for sharding algorithm

#Hint sharding strategy
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint sharding algorithm class name. This class need to implements HintShardingAlgorithm, and require a no argument constructor

#Tables sharding strategy, Same as database- shar-ing strategy
sharding.jdbc.config.sharding.tables.<logic-table-name>.table-strategy.xxx= #Ignore

sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-column-name= #Column name of key generator, do not use Key generator if absent
sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-class-name= #Key generator, use default key generator if absent. This class need to implements KeyGenerator, and require a no argument constructor

sharding.jdbc.config.sharding.tables.<logic-table-name>.logic-index= #Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables

sharding.jdbc.config.sharding.binding-tables[0]= #Binding table rule configurations
sharding.jdbc.config.sharding.binding-tables[1]= #Binding table rule configurations
sharding.jdbc.config.sharding.binding-tables[x]= #Binding table rule configurations

sharding.jdbc.config.sharding.default-data-source-name= #If table not configure at table rule, will route to defaultDataSourceName
sharding.jdbc.config.sharding.default-database-strategy.xxx= #Default strategy for sharding databases, same as databases sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.xxx= #Default strategy for sharding tables, same as tables sharding strategy
sharding.jdbc.config.sharding.default-key-generator-class-name= #Default key generator class name, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator`. This class need to implements KeyGenerator, and require a no argument constructor

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key1= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key2= #more details can reference Read-write splitting part
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.keyx= #more details can reference Read-write splitting part

sharding.jdbc.config.sharding.props.sql.show= #To show SQLS or not, default value: false
sharding.jdbc.config.sharding.props.executor.size= #The number of working threads, default value: CPU count

sharding.jdbc.config.sharding.config.map.key1= #User-defined arguments
sharding.jdbc.config.sharding.config.map.key2= #User-defined arguments
sharding.jdbc.config.sharding.config.map.keyx= #User-defined arguments
```

### Read-write splitting

```properties
#Ignore data sources configuration, same as sharding

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #Name of master data source
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #Names of Slave data sources
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #Load balance algorithm class name. This class need to implements MasterSlaveLoadBalanceAlgorithm, and require a no argument constructor 
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `load-balance-algorithm-class-name` is present 

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key1= #User-defined arguments
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.key2= #User-defined arguments
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.config.map.keyx= #User-defined arguments

sharding.jdbc.config.masterslave.props.sql.show=true
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
