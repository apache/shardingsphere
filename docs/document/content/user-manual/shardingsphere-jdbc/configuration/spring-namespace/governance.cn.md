+++
title = "分布式治理"
weight = 5
+++

## 配置项说明

### 治理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd">

    <cluster:repository id="clusterRepository" type="ZooKeeper" namespace="regCenter" server-lists="localhost:2181">
        <props>
            <prop key="max-retries">3</prop>
            <prop key="operation-timeout-milliseconds">3000</prop>
        </props>
    </cluster:repository>
    <shardingsphere:data-source id="shardingDatabasesTablesDataSource" schema-name="sharding_db" data-source-names="demo_ds_0, demo_ds_1" rule-refs="shardingRule">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true"/>
    </shardingsphere:data-source>
</beans>
```

命名空间: [http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd)

<cluster:repository />

| *名称*         | *类型* | *说明*                                                                     |
| ------------- | ------ | ------------------------------------------------------------------------- |
| id            | 属性   | 注册中心实例名称                                                              |
| schema-name   | 属性   | JDBC数据源别名，该参数可实现JDBC与PROXY共享配置                                  |
| type          | 属性   | 注册中心类型。如：ZooKeeper, etcd                                              |
| namespace     | 属性   | 注册中心命名空间                                                              |
| server-lists  | 属性   | 注册中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |
