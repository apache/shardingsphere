+++
toc = true
title = "Spring Boot Configuration"
weight = 3
+++

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Configuration Example

### Data Sharding

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

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator.column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator.column=order_item_id
sharding.jdbc.config.sharding.binding-tables=t_order,t_order_item
sharding.jdbc.config.sharding.broadcast-tables=t_config

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
```

### Read-Write Split

```properties
sharding.jdbc.datasource.names=master,slave0,slave1

sharding.jdbc.datasource.master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master.url=jdbc:mysql://localhost:3306/master
sharding.jdbc.datasource.master.username=root
sharding.jdbc.datasource.master.password=

sharding.jdbc.datasource.slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.slave0.url=jdbc:mysql://localhost:3306/slave0
sharding.jdbc.datasource.slave0.username=root
sharding.jdbc.datasource.slave0.password=

sharding.jdbc.datasource.slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.slave1.url=jdbc:mysql://localhost:3306/slave1
sharding.jdbc.datasource.slave1.username=root
sharding.jdbc.datasource.slave1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ms
sharding.jdbc.config.masterslave.master-data-source-name=master
sharding.jdbc.config.masterslave.slave-data-source-names=slave0,slave1

sharding.jdbc.config.props.sql.show=true
```

### Data Sharding + Read-Write Split

```properties
sharding.jdbc.datasource.names=master0,master1,master0slave0,master0slave1,master1slave0,master1slave1

sharding.jdbc.datasource.master0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master0.url=jdbc:mysql://localhost:3306/master0
sharding.jdbc.datasource.master0.username=root
sharding.jdbc.datasource.master0.password=

sharding.jdbc.datasource.master0slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master0slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master0slave0.url=jdbc:mysql://localhost:3306/master0slave0
sharding.jdbc.datasource.master0slave0.username=root
sharding.jdbc.datasource.master0slave0.password=
sharding.jdbc.datasource.master0slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master0slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master0slave1.url=jdbc:mysql://localhost:3306/master0slave1
sharding.jdbc.datasource.master0slave1.username=root
sharding.jdbc.datasource.master0slave1.password=

sharding.jdbc.datasource.master1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master1.url=jdbc:mysql://localhost:3306/master1
sharding.jdbc.datasource.master1.username=root
sharding.jdbc.datasource.master1.password=

sharding.jdbc.datasource.master1slave0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master1slave0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master1slave0.url=jdbc:mysql://localhost:3306/master1slave0
sharding.jdbc.datasource.master1slave0.username=root
sharding.jdbc.datasource.master1slave0.password=
sharding.jdbc.datasource.master1slave1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.master1slave1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.master1slave1.url=jdbc:mysql://localhost:3306/master1slave1
sharding.jdbc.datasource.master1slave1.username=root
sharding.jdbc.datasource.master1slave1.password=

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator.column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator.column=order_item_id
sharding.jdbc.config.sharding.binding-tables=t_order,t_order_item
sharding.jdbc.config.sharding.broadcast-tables=t_config

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=master$->{user_id % 2}

sharding.jdbc.config.sharding.master-slave-rules.ds0.master-data-source-name=master0
sharding.jdbc.config.sharding.master-slave-rules.ds0.slave-data-source-names=master0slave0, master0slave1
sharding.jdbc.config.sharding.master-slave-rules.ds1.master-data-source-name=master1
sharding.jdbc.config.sharding.master-slave-rules.ds1.slave-data-source-names=master1slave0, master1slave1
```

### Data Sharding + Data Masking

```properties
sharding.jdbc.datasource.names=ds_0,ds_1

sharding.jdbc.datasource.ds_0.type=com.zaxxer.hikari.HikariDataSource
sharding.jdbc.datasource.ds_0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.jdbc-url=jdbc:mysql://localhost:3306/demo_ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=com.zaxxer.hikari.HikariDataSource
sharding.jdbc.datasource.ds_1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.jdbc-url=jdbc:mysql://localhost:3306/demo_ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds_$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds_$->{0..1}.t_order_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator.column=order_id
sharding.jdbc.config.sharding.tables.t_order.key-generator.type=SNOWFLAKE
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds_$->{0..1}.t_order_item_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator.column=order_item_id
sharding.jdbc.config.sharding.tables.t_order_item.key-generator.type=SNOWFLAKE
sharding.jdbc.config.sharding.tables.t_order_item.encryptor.type=MD5
sharding.jdbc.config.sharding.tables.t_order_item.encryptor.columns=status
sharding.jdbc.config.sharding.tables.t_order_encrypt.actual-data-nodes=ds_$->{0..1}.t_order_encrypt_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_encrypt.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_encrypt.table-strategy.inline.algorithm-expression=t_order_encrypt_$->{order_id % 2}

sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.type=QUERY
sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.columns=encrypt_id
sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.assistedQueryColumns=query_id
```

### Data Orchestration

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
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.key-generator.column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.key-generator.column=order_item_id
sharding.jdbc.config.sharding.binding-tables=t_order,t_order_item
sharding.jdbc.config.sharding.broadcast-tables=t_config

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=master$->{user_id % 2}

sharding.jdbc.config.orchestration.name=spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.registry.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.registry.server-lists=localhost:2181
```

## Configuration Item Explanation

### Data Sharding

```properties
sharding.jdbc.datasource.names= #Data source name; multiple data sources are separated by commas

sharding.jdbc.datasource.<data-source-name>.type= #Database connection pool type name
sharding.jdbc.datasource.<data-source-name>.driver-class-name= #Database driver class name
sharding.jdbc.datasource.<data-source-name>.url= #Database url connection
sharding.jdbc.datasource.<data-source-name>.username= #Database username
sharding.jdbc.datasource.<data-source-name>.password= #Database password
sharding.jdbc.datasource.<data-source-name>.xxx= #Other properties of database connection pool

sharding.jdbc.config.sharding.tables.<logic-table-name>.actual-data-nodes= #It is consisted of data source name + table name, separated by decimal points; multiple tables are separated by commas and support inline expressions; default means using existing data sources and logic table names to generate data nodes; it can be applied in broadcast tables (each database needs a same table for relevance query, dictionary table mostly) or the situation with sharding database but without sharding table (table structures of all the databases are consistent)

#Database sharding strategy; default means using default database sharding strategy; it can only choose one of the following sharding strategies

#It is applied in standard sharding situation of single-sharding key
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #Sharding column name
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #Precise algorithm class name, applied in = and IN; the class needs to implement PreciseShardingAlgorithm interface and provide parameter-free constructor
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #Range sharding algorithm class name, applied in BETWEEN, optional; the class should implement RangeShardingAlgorithm interface and provide parameter-free constructor

#It is applied in complex sharding situations with multiple sharding keys
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #Sharding column name, with multiple columns separated by commas
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #Complex sharding algorithm class name; the class needs to implement ComplexKeysShardingAlgorithm interface and provide parameter-free constructor

#Inline expression sharding strategy
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #Sharding column name
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-expression= #Inline expression of sharding algorithm, which needs to conform to groovy statements

#Hint Sharding Strategy
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint algorithm class name;  the class needs to implement HintShardingAlgorithm interface and provide parameter-free constructor

#Table sharding strategy, same as database sharding strategy
sharding.jdbc.config.sharding.tables.<logic-table-name>.table-strategy.xxx= #Omitted

sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator.column= #Auto-increment column name; default means not using auto-increment key generator
sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator.type= #Auto-increament key generator type; default means using default auto-increament key generator; user defined generator or internal generator (SNOWFLAKE or UUID) can both be selected
sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator.props.<property-name>= #Property configuration of auto-increament key generator, such as worker.id and max.tolerate.time.difference.milliseconds or SNOWFLAKE algorithm

sharding.jdbc.config.sharding.tables.<logic-table-name>.logic-index= #Logic index name; for table-sharding DROP INDEX XXX in Oracle/PostgreSQL, logic index name needs to be configured to locate actual sharding tables of the executing SQL

sharding.jdbc.config.sharding.binding-tables[0]= #Binding table rule list
sharding.jdbc.config.sharding.binding-tables[1]= #Binding table rule list
sharding.jdbc.config.sharding.binding-tables[x]= #Binding table rule list

sharding.jdbc.config.sharding.broadcast-tables[0]= #Broadcast table rule list
sharding.jdbc.config.sharding.broadcast-tables[1]= #Broadcast table rule list
sharding.jdbc.config.sharding.broadcast-tables[x]= #Broadcast table rule list

sharding.jdbc.config.sharding.default-data-source-name= #Tables without sharding rules will be located through default data source
sharding.jdbc.config.sharding.default-database-strategy.xxx= #Default database sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.xxx= #Default table sharding strategy
sharding.jdbc.config.sharding.default-key-generator.type= #Default auto-increament key generator of type; it will use org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator in default; user defined generator or internal generator (SNOWFLAKE or UUID) can both be used
sharding.jdbc.config.sharding.default-key-generator.props.<property-name>= #Auto-increament key generator property configuration, such as worker.id and max.tolerate.time.difference.milliseconds of SNOWFLAKE algorithm

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #Refer to read-write split part for more details
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #Refer to read-write split part for more details
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #Refer to read-write split part for more details
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #Refer to read-write split part for more details
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #Refer to read-write split part for more details
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #Refer to read-write split part for more details

sharding.jdbc.config.props.sql.show= #Show SQL or not; default value: false
sharding.jdbc.config.props.executor.size= #Executing thread number; default value: CPU core number
```

### Read-Write Split

```properties
#Omit data source configurations; keep it consistent with data sharding

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #Data source name of master database
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #Data source name list of slave database
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #Data source name list of slave database
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #Data source name list of slave database
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #Load balance algorithm class name; the class needs to implement MasterSlaveLoadBalanceAlgorithm interface and provide parameter-free constructor
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #Load balance algorithm class of slave database; optional value: ROUND_ROBIN and RANDOM; if there is load-balance-algorithm-class-name, the configuration can be omitted

sharding.jdbc.config.props.sql.show= #Show SQL or not; default value: false
sharding.jdbc.config.props.executor.size= #Executing thread number; default value: CPU core number
sharding.jdbc.config.props.check.table.metadata.enabled= #Whether to check meta-data consistency of sharding table when it initializes; default value: false
```

### Data Masking

```properties
sharding.jdbc.config.sharding.tables.<logic-table-name>.encryptor.type= #Type of encryptor，use user-defined ones or built-in ones, e.g. MD5/AES 
sharding.jdbc.config.sharding.tables.<logic-table-name>.encryptor.columns= #Column name of key generator      
sharding.jdbc.config.sharding.tables.<logic-table-name>.encryptor.assistedQueryColumns= #assistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data
sharding.jdbc.config.sharding.tables.<logic-table-name>.encryptor.props..<property-name>= #Properties, e.g. `aes.key.value` for AES encryptor  
```

### Data Orchestration

```properties
#Omit data source, data sharding and read-write split configurations

sharding.jdbc.config.sharding.orchestration.name= #Data orchestration instance name
sharding.jdbc.config.sharding.orchestration.overwrite= #Whether to overwrite local configurations with registry center configurations; if it can, each initialization should refer to local configurations
sharding.jdbc.config.sharding.orchestration.registry.type= #Registry center type. Example:zookeeper
sharding.jdbc.config.sharding.orchestration.registry.server-lists= #The list of servers that connect to registry center, including IP and port number; use commas to separate
sharding.jdbc.config.sharding.orchestration.registry.namespace= #Registry center namespace
sharding.jdbc.config.sharding.orchestration.registry.digest= #The token that connects to the registry center; default means there is no need for authentication
sharding.jdbc.config.sharding.orchestration.registry.operation-timeout-milliseconds= #The millisecond number for operation timeout; default value: 500 milliseconds
sharding.jdbc.config.sharding.orchestration.registry.max-retries= #Maximum retry time after failing; default value: 3 times
sharding.jdbc.config.sharding.orchestration.registry.retry-interval-milliseconds= #Interval time to retry; default value: 500 milliseconds
sharding.jdbc.config.sharding.orchestration.registry.time-to-live-seconds= #Living time of temporary nodes; default value: 60 seconds
sharding.jdbc.config.sharding.orchestration.registry.props= #Customize registry center props.
```
