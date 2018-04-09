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

##### 分库分表配置项说明
同[分库分表Yaml配置](#分库分表配置项说明)

#### 读写分离
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

##### 读写分离配置项说明
同[读写分离Yaml配置](#读写分离配置项说明)


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
同[分库分表Yaml配置](#分库分表编排配置项说明)
