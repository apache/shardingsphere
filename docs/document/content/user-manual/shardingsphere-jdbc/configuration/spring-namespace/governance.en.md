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
       xmlns:governance="http://shardingsphere.apache.org/schema/shardingsphere/governance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/governance
                           http://shardingsphere.apache.org/schema/shardingsphere/governance/governance.xsd
">
    <governance:reg-center id="regCenter" type="ZooKeeper" server-lists="localhost:2181" />
    <governance:config-center id="configCenter" type="ZooKeeper" server-lists="localhost:2182" />
    <governance:data-source id="shardingDatabasesTablesDataSource" data-source-names="demo_ds_0, demo_ds_1" reg-center-ref="regCenter" config-center-ref="configCenter" rule-refs="shardingRule" overwrite="true" />
</beans>
```

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd)

<governance:reg-center />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Registry center name                                                                                      |
| type          | Attribute  | Registry center type. Example: ZooKeeper, etcd                                                            |
| server-lists  | Attribute  | The list of servers that connect to registry center, including IP and port number; use commas to separate |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                       |

<governance:config-center />

| *Name*        | *Type*     | *Description*                                                                                           |
| ------------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Config center name                                                                                      |
| type          | Attribute  | Config center type. Example: ZooKeeper, etcd, Nacos, Apollo                                             |
| server-lists  | Attribute  | The list of servers that connect to config center, including IP and port number; use commas to separate |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                     |
