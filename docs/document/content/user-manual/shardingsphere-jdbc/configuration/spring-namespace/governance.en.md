+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:orchestration="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd
">
    <orchestration:reg-center id="regCenter" type="ZooKeeper" server-lists="localhost:2181" />
    <orchestration:config-center id="configCenter" type="ZooKeeper" server-lists="localhost:2182" />
    <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" reg-center-ref="regCenter" config-center-ref="configCenter" overwrite="true" />
</beans>
```

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd)

<orchestration:reg-center />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Registry center name                                                                                      |
| type          | Attribute  | Registry center type. Example: ZooKeeper, etcd                                                            |
| server-lists  | Attribute  | The list of servers that connect to registry center, including IP and port number; use commas to separate |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                       |

<orchestration:config-center />

| *Name*        | *Type*     | *Description*                                                                                           |
| ------------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Config center name                                                                                      |
| type          | Attribute  | Config center type. Example: ZooKeeper, etcd, Nacos, Apollo                                             |
| server-lists  | Attribute  | The list of servers that connect to config center, including IP and port number; use commas to separate |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                     |
