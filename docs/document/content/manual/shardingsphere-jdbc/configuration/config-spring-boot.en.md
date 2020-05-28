+++
title = "Spring Boot Configuration"
weight = 3
+++

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Configuration Example

### Data Sharding

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order_item.key-generator.column=order_item_id
spring.shardingsphere.sharding.tables.t_order_item.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.binding-tables=t_order,t_order_item
spring.shardingsphere.sharding.broadcast-tables=t_config

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
```

### Read-Write Split

```properties
spring.shardingsphere.datasource.names=master,slave0,slave1

spring.shardingsphere.datasource.master.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master.url=jdbc:mysql://localhost:3306/master
spring.shardingsphere.datasource.master.username=root
spring.shardingsphere.datasource.master.password=

spring.shardingsphere.datasource.slave0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave0.url=jdbc:mysql://localhost:3306/slave0
spring.shardingsphere.datasource.slave0.username=root
spring.shardingsphere.datasource.slave0.password=

spring.shardingsphere.datasource.slave1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.slave1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave1.url=jdbc:mysql://localhost:3306/slave1
spring.shardingsphere.datasource.slave1.username=root
spring.shardingsphere.datasource.slave1.password=

spring.shardingsphere.masterslave.load-balance-algorithm-type=round_robin
spring.shardingsphere.masterslave.name=ms
spring.shardingsphere.masterslave.master-data-source-name=master
spring.shardingsphere.masterslave.slave-data-source-names=slave0,slave1

spring.shardingsphere.props.sql.show=true
```
### Data Masking

```properties
spring.shardingsphere.datasource.name=ds

spring.shardingsphere.datasource.ds.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.url=jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=
spring.shardingsphere.datasource.ds.max-total=100

spring.shardingsphere.encrypt.encryptors.encryptor_aes.type=aes
spring.shardingsphere.encrypt.encryptors.encryptor_aes.props.aes.key.value=123456
spring.shardingsphere.encrypt.tables.t_order.columns.user_id.plainColumn=user_decrypt
spring.shardingsphere.encrypt.tables.t_order.columns.user_id.cipherColumn=user_encrypt
spring.shardingsphere.encrypt.tables.t_order.columns.user_id.assistedQueryColumn=user_assisted
spring.shardingsphere.encrypt.tables.t_order.columns.user_id.encryptor=encryptor_aes

spring.shardingsphere.props.sql.show=true
spring.shardingsphere.props.query.with.cipher.column=true
```

### Data Sharding + Read-Write Split

```properties
spring.shardingsphere.datasource.names=master0,master1,master0slave0,master0slave1,master1slave0,master1slave1

spring.shardingsphere.datasource.master0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master0.url=jdbc:mysql://localhost:3306/master0
spring.shardingsphere.datasource.master0.username=root
spring.shardingsphere.datasource.master0.password=

spring.shardingsphere.datasource.master0slave0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master0slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master0slave0.url=jdbc:mysql://localhost:3306/master0slave0
spring.shardingsphere.datasource.master0slave0.username=root
spring.shardingsphere.datasource.master0slave0.password=
spring.shardingsphere.datasource.master0slave1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master0slave1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master0slave1.url=jdbc:mysql://localhost:3306/master0slave1
spring.shardingsphere.datasource.master0slave1.username=root
spring.shardingsphere.datasource.master0slave1.password=

spring.shardingsphere.datasource.master1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master1.url=jdbc:mysql://localhost:3306/master1
spring.shardingsphere.datasource.master1.username=root
spring.shardingsphere.datasource.master1.password=

spring.shardingsphere.datasource.master1slave0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master1slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master1slave0.url=jdbc:mysql://localhost:3306/master1slave0
spring.shardingsphere.datasource.master1slave0.username=root
spring.shardingsphere.datasource.master1slave0.password=
spring.shardingsphere.datasource.master1slave1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.master1slave1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master1slave1.url=jdbc:mysql://localhost:3306/master1slave1
spring.shardingsphere.datasource.master1slave1.username=root
spring.shardingsphere.datasource.master1slave1.password=

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
spring.shardingsphere.sharding.tables.t_order.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order_item.key-generator.column=order_item_id
spring.shardingsphere.sharding.tables.t_order_item.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.binding-tables=t_order,t_order_item
spring.shardingsphere.sharding.broadcast-tables=t_config

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=master$->{user_id % 2}

spring.shardingsphere.sharding.master-slave-rules.ds0.master-data-source-name=master0
spring.shardingsphere.sharding.master-slave-rules.ds0.slave-data-source-names=master0slave0, master0slave1
spring.shardingsphere.sharding.master-slave-rules.ds1.master-data-source-name=master1
spring.shardingsphere.sharding.master-slave-rules.ds1.slave-data-source-names=master1slave0, master1slave1
```

### Data Sharding + Data Masking

```properties
spring.shardingsphere.datasource.names=ds_0,ds_1

spring.shardingsphere.datasource.ds_0.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds_0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds_0.jdbc-url=jdbc:mysql://localhost:3306/demo_ds_0
spring.shardingsphere.datasource.ds_0.username=root
spring.shardingsphere.datasource.ds_0.password=

spring.shardingsphere.datasource.ds_1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds_1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds_1.jdbc-url=jdbc:mysql://localhost:3306/demo_ds_1
spring.shardingsphere.datasource.ds_1.username=root
spring.shardingsphere.datasource.ds_1.password=

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds_$->{user_id % 2}

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds_$->{0..1}.t_order_$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order_$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
spring.shardingsphere.sharding.tables.t_order.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds_$->{0..1}.t_order_item_$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order_item.key-generator.column=order_item_id
spring.shardingsphere.sharding.tables.t_order_item.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.encrypt-rule.encryptors.encryptor_aes.type=aes
spring.shardingsphere.sharding.encrypt-rule.encryptors.encryptor_aes.props.aes.key.value=123456
spring.shardingsphere.sharding.encrypt-rule.tables.t_order.columns.user_id.plainColumn=user_decrypt
spring.shardingsphere.sharding.encrypt-rule.tables.t_order.columns.user_id.cipherColumn=user_encrypt
spring.shardingsphere.sharding.encrypt-rule.tables.t_order.columns.user_id.assistedQueryColumn=user_assisted
spring.shardingsphere.sharding.encrypt-rule.tables.t_order.columns.user_id.encryptor=encryptor_aes
```

### Orchestration

```properties
spring.shardingsphere.datasource.names=ds,ds0,ds1
spring.shardingsphere.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds.driver-class-name=org.h2.Driver
spring.shardingsphere.datasource.ds.url=jdbc:mysql://localhost:3306/ds
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

spring.shardingsphere.sharding.default-data-source-name=ds
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order_item.key-generator.column=order_item_id
spring.shardingsphere.sharding.binding-tables=t_order,t_order_item
spring.shardingsphere.sharding.broadcast-tables=t_config

spring.shardingsphere.orchestration.spring_boot_ds_sharding.orchestration-type=registry_center,config_center,metadata_center
spring.shardingsphere.orchestration.spring_boot_ds_sharding.instance-type=zookeeper
spring.shardingsphere.orchestration.spring_boot_ds_sharding.server-lists=localhost:2181
spring.shardingsphere.orchestration.spring_boot_ds_sharding.namespace=orchestration-spring-boot-sharding-test
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.overwrite=true
```

### JNDI
In the above configuration example, all datasource configurations can be replaced with JNDI, such as `Data Sharding`:
```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.jndi-name=java:comp/env/jdbc/ds0
spring.shardingsphere.datasource.ds1.jndi-name=jdbc/ds1

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order.key-generator.column=order_id
spring.shardingsphere.sharding.tables.t_order.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
spring.shardingsphere.sharding.tables.t_order_item.key-generator.column=order_item_id
spring.shardingsphere.sharding.tables.t_order_item.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.binding-tables=t_order,t_order_item
spring.shardingsphere.sharding.broadcast-tables=t_config

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}
```

## Configuration Item Explanation

### Data Sharding

```properties
spring.shardingsphere.datasource.names= #Data source name; multiple data sources are separated by commas

spring.shardingsphere.datasource.<data-source-name>.type= #Database connection pool type name
spring.shardingsphere.datasource.<data-source-name>.driver-class-name= #Database driver class name
spring.shardingsphere.datasource.<data-source-name>.url= #Database url connection
spring.shardingsphere.datasource.<data-source-name>.username= #Database username
spring.shardingsphere.datasource.<data-source-name>.password= #Database password
spring.shardingsphere.datasource.<data-source-name>.xxx= #Other properties of database connection pool

spring.shardingsphere.sharding.tables.<logic-table-name>.actual-data-nodes= #It is consisted of data source name + table name, separated by decimal points; multiple tables are separated by commas and support inline expressions; default means using existing data sources and logic table names to generate data nodes; it can be applied in broadcast tables (each database needs a same table for relevance query, dictionary table mostly) or the situation with sharding database but without sharding table (table structures of all the databases are consistent)

#Database sharding strategy; default means using default database sharding strategy; it can only choose one of the following sharding strategies

#It is applied in standard sharding situation of single-sharding key
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #Sharding column name
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #Precise algorithm class name, applied in = and IN; the class needs to implement PreciseShardingAlgorithm interface and provide parameter-free constructor
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #Range sharding algorithm class name, applied in BETWEEN, optional; the class should implement RangeShardingAlgorithm interface and provide parameter-free constructor

#It is applied in complex sharding situations with multiple sharding keys
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #Sharding column name, with multiple columns separated by commas
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #Complex sharding algorithm class name; the class needs to implement ComplexKeysShardingAlgorithm interface and provide parameter-free constructor

#Inline expression sharding strategy
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #Sharding column name
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-expression= #Inline expression of sharding algorithm, which needs to conform to groovy statements

#Hint Sharding Strategy
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint algorithm class name;  the class needs to implement HintShardingAlgorithm interface and provide parameter-free constructor

#Table sharding strategy, same as database sharding strategy
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.xxx= #Omitted

spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.column= #Auto-increment column name; default means not using auto-increment key generator
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.type= #Auto-increament key generator type; default means using default auto-increament key generator; user defined generator or internal generator (SNOWFLAKE, UUID) can both be selected
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.props.<property-name>= #Properties, Notice: when use SNOWFLAKE, `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` need to be set. To use the generated value of this algorithm as sharding value, it is recommended to configure `max.vibration.offset`

spring.shardingsphere.sharding.binding-tables[0]= #Binding table rule list
spring.shardingsphere.sharding.binding-tables[1]= #Binding table rule list
spring.shardingsphere.sharding.binding-tables[x]= #Binding table rule list

spring.shardingsphere.sharding.broadcast-tables[0]= #Broadcast table rule list
spring.shardingsphere.sharding.broadcast-tables[1]= #Broadcast table rule list
spring.shardingsphere.sharding.broadcast-tables[x]= #Broadcast table rule list

spring.shardingsphere.sharding.default-data-source-name= #Tables without sharding rules will be located through default data source
spring.shardingsphere.sharding.default-database-strategy.xxx= #Default database sharding strategy
spring.shardingsphere.sharding.default-table-strategy.xxx= #Default table sharding strategy
spring.shardingsphere.sharding.default-key-generator.type= #Default auto-increament key generator of type; it will use org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator in default; user defined generator or internal generator (SNOWFLAKE or UUID) can both be used
spring.shardingsphere.sharding.default-key-generator.props.<property-name>= #Auto-increament key generator property configuration, such as worker.id and max.tolerate.time.difference.milliseconds of SNOWFLAKE algorithm

spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #Refer to read-write split part for more details
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #Refer to read-write split part for more details
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #Refer to read-write split part for more details
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #Refer to read-write split part for more details
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #Refer to read-write split part for more details
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #Refer to read-write split part for more details

spring.shardingsphere.props.sql.show= #Show SQL or not; default value: false
spring.shardingsphere.props.executor.size= #Executing thread number; default value: CPU core number
```

### Read-Write Split

```properties
#Omit data source configurations; keep it consistent with data sharding

spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #Data source name of master database
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #Data source name list of slave database
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #Data source name list of slave database
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #Data source name list of slave database
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #Load balance algorithm class name; the class needs to implement MasterSlaveLoadBalanceAlgorithm interface and provide parameter-free constructor
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #Load balance algorithm class of slave database; optional value: ROUND_ROBIN and RANDOM; if there is load-balance-algorithm-class-name, the configuration can be omitted

spring.shardingsphere.props.sql.show= #Show SQL or not; default value: false
spring.shardingsphere.props.executor.size= #Executing thread number; default value: CPU core number
spring.shardingsphere.props.check.table.metadata.enabled= #Whether to check meta-data consistency of sharding table when it initializes; default value: false
```

### Data Masking

```properties
#Omit data source configurations; keep it consistent with data sharding

spring.shardingsphere.encrypt.encryptors.<encryptor-name>.type= #Type of encryptor，use user-defined ones or built-in ones, e.g. MD5/AES  
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.props.<property-name>= #Properties, Notice: when use AES encryptor, `aes.key.value` for AES encryptor need to be set
spring.shardingsphere.encrypt.tables.<table-name>.columns.<logic-column-name>.plainColumn= #Plain column name
spring.shardingsphere.encrypt.tables.<table-name>.columns.<logic-column-name>.cipherColumn= #Cipher column name 
spring.shardingsphere.encrypt.tables.<table-name>.columns.<logic-column-name>.assistedQueryColumn= #AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data
spring.shardingsphere.encrypt.tables.<table-name>.columns.<logic-column-name>.encryptor= #Encryptor name
```

### Orchestration

```properties
#Omit data source, data sharding, read-write split and data masking configurations

spring.shardingsphere.orchestration.spring_boot_ds_sharding.orchestration-type= The type of orchestration center: config_center or registry_center or metadata_center
spring.shardingsphere.orchestration.spring_boot_ds_sharding.instance-type= #Center instance type. Example:zookeeper#Registry center type. Example:zookeeper
spring.shardingsphere.orchestration.spring_boot_ds_sharding.server-lists= #The list of servers that connect to registry center, including IP and port number; use commas to separate
spring.shardingsphere.orchestration.spring_boot_ds_sharding.namespace= #Center namespace
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.overwrite= #Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.digest= #The token that connects to the center; default means there is no need for authentication
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.operation-timeout-milliseconds= #The millisecond number for operation timeout; default value: 500 milliseconds
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.max-retries= #Maximum retry time after failing; default value: 3 times
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.retry-interval-milliseconds= #Interval time to retry; default value: 500 milliseconds
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.time-to-live-seconds= #Living time of temporary nodes; default value: 60 seconds
```
