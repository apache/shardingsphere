+++
title = "Read-write Split"
weight = 2
+++

## Configuration Example

```properties
spring.shardingsphere.datasource.names=master_ds,slave_ds0,slave_ds1

spring.shardingsphere.datasource.master_ds.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.master_ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.master_ds.url=jdbc:mysql://localhost:3306/master_ds
spring.shardingsphere.datasource.master_ds.username=root
spring.shardingsphere.datasource.master_ds.password=root

spring.shardingsphere.datasource.slave_ds0.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.slave_ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave_ds0.url=jdbc:mysql://localhost:3306/slave_ds0
spring.shardingsphere.datasource.slave_ds0.username=root
spring.shardingsphere.datasource.slave_ds0.password=root

spring.shardingsphere.datasource.slave_ds1.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.slave_ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.slave_ds1.url=jdbc:mysql://localhost:3306/slave_ds1
spring.shardingsphere.datasource.slave_ds1.username=root
spring.shardingsphere.datasource.slave_ds1.password=root

spring.shardingsphere.rules.master-slave.data-sources.ms_ds.master-data-source-name=master_ds
spring.shardingsphere.rules.master-slave.data-sources.ms_ds.slave-data-source-names=slave_ds0,slave_ds1
spring.shardingsphere.rules.master-slave.data-sources.ms_ds.load-balancer-name=random

spring.shardingsphere.rules.master-slave.load-balancers.random.type=RANDOM
```

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit data source configuration

spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.master-data-source-name= # Master data source name
spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.slave-data-source-names= # Slave data source names, multiple data source names separated with comma
spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.load-balancer-name= # Load balance algorithm name

spring.shardingsphere.rules.master-slave.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
