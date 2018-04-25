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

### Read-write splitting

### Orchestration by Zookeeper

### Orchestration by Etcd
