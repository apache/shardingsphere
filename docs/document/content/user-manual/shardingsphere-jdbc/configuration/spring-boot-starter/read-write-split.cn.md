+++
title = "读写分离"
weight = 2
+++

## 配置示例

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

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置

spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.master-data-source-name= # 主数据源名称
spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.slave-data-source-names= # 从数据源名称，多个从数据源用逗号分隔
spring.shardingsphere.rules.master-slave.data-sources.<master-slave-data-source-name>.load-balancer-name= # 负载均衡算法名称

spring.shardingsphere.rules.master-slave.load-balancers.<load-balance-algorithm-name>.type= # 负载均衡算法类型
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。
