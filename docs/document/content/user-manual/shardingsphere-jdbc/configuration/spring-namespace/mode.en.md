+++
title = "Mode"
weight = 5
+++

## Configuration Item Explanation

### Standalone mode

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:standalone="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository.xsd">
    <standalone:repository id="standaloneRepository" type="File">
        <props>
            <prop key="path">target</prop>
        </props>
    </standalone:repository>
    <shardingsphere:data-source id="shardingDatabasesTablesDataSource" data-source-names="demo_ds_0, demo_ds_1" rule-refs="shardingRule" schema-name="sharding_db">
        <shardingsphere:mode type="Standalone" repository-ref="standaloneRepository" overwrite="true"/>
    </shardingsphere:data-source>
</beans>
```

<standalone:repository />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Standalone mode instance name                                                                              |
| type          | Attribute  | Standalone Configuration persist type, such as: File                                                       |
| props (?)     | Attribute  | Configuration persist properties, such as: path                                                            |

<shardingsphere:data-source />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| schema-name   | Attribute  | JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy    |

### Cluster mode

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd">
    <cluster:repository id="clusterRepository" type="Zookeeper" namespace="regCenter" server-lists="localhost:3182">
        <props>
            <prop key="max-retries">3</prop>
            <prop key="operation-timeout-milliseconds">1000</prop>
        </props>
    </cluster:repository>
    <shardingsphere:data-source id="shardingDatabasesTablesDataSource" data-source-names="demo_ds_0, demo_ds_1" rule-refs="shardingRule" schema-name="sharding_db">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true"/>
    </shardingsphere:data-source>
</beans>
```

<cluster:repository />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| id            | Attribute  | Cluster mode instance name                                                                                |
| type          | Attribute  | Cluster mode type. Example: ZooKeeper, etcd                                                               |
| namespace     | Attribute  | Cluster mode namespace                                                                                    |
| server-lists  | Attribute  | Zookeeper or Etcd server list, including IP and port number; use commas to separate                       |
| props (?)     | Attribute  | Properties for center instance config, such as options of zookeeper                                       |

<shardingsphere:data-source />

| *Name*        | *Type*     | *Description*                                                                                             |
| ------------- | ---------- | --------------------------------------------------------------------------------------------------------- |
| schema-name   | Attribute  | JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy    |
