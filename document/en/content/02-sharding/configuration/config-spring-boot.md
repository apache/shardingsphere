+++
toc = true
title = "Spring Boot configuration"
weight = 5
prev = "/02-sharding/configuration/config-spring-namespace/"
next = "/02-sharding/sql-supported/"
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

#### Sharding Configuration
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
```

#### The details on some Config options for Sharding 
Refer to [The Yaml Configuration for Sharding](#The config items for Sharding)

#### Read-write splitting Configuration
```yaml
sharding.jdbc.datasource.names=ds_master,ds_slave_0,ds_slave_1

sharding.jdbc.datasource.ds_master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master.url=jdbc:mysql://localhost:3306/demo_ds_master
sharding.jdbc.datasource.ds_master.username=root
sharding.jdbc.datasource.ds_master.password=

sharding.jdbc.datasource.ds_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_0.url=jdbc:mysql://localhost:3306/demo_ds_slave_0
sharding.jdbc.datasource.ds_slave_0.username=root
sharding.jdbc.datasource.ds_slave_0.password=

sharding.jdbc.datasource.ds_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_1.url=jdbc:mysql://localhost:3306/demo_ds_slave_1
sharding.jdbc.datasource.ds_slave_1.username=root
sharding.jdbc.datasource.ds_slave_1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave_0,ds_slave_1

```

#### The details on some Config options for Reading-writing splitting
Refer to [The Yaml configuration for Reading-writing splitting](#The config items for Read-write splitting)
