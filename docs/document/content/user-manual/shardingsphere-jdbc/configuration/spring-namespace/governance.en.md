+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

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

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd)

<governance:reg-center />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Registry center name                                                                                      |
| schema-name   | Attribute  | JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy    |
| type          | Attribute  | Registry center type. Example: ZooKeeper, etcd                                                            |
| namespace     | Attribute  | Registry center namespace                                                                                 |
| server-lists  | Attribute  | The list of servers that connect to registry center, including IP and port number; use commas to separate |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                       |
