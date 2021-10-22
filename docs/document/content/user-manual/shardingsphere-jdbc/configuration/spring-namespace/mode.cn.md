+++
title = "Mode"
weight = 5
+++

## 配置项说明

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

| *名称*         | *类型* | *说明*                                                                                    |
| ------------- | ------ | ---------------------------------------------------------------------------------------  |
| id            | 属性   | Standalone 模式实例名称                                                                     |
| type          | 属性   | Standalone 配置持久化类型。如：File                                                         |
| props (?)     | 属性   | Standalone 配置持久化的属性 如：path 路径                                                    |

<shardingsphere:data-source />

| *名称*            | *类型* | *说明*                                                                                    |
| ----------------- | ----- | ---------------------------------------------------------------------------------------  |
| schema-name (?)   | 属性   | JDBC 数据源别名，该参数可实现 JDBC 与 PROXY 共享配置                                               |

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

| *名称*         | *类型* | *说明*                                                                                    |
| ------------- | ------ | ---------------------------------------------------------------------------------------  |
| id            | 属性   | Cluster 模式实例名称                                                                       |
| type          | 属性   | Cluster 模式类型。如：ZooKeeper, Etcd                                                      |
| namespace     | 属性   | Cluster 模式命名空间                                                                       |
| server-lists  | 属性   | Zookeeper 或 Etcd 服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                                         |

<shardingsphere:data-source />

| *名称*             | *类型* | *说明*                                                                                    |
| ----------------- | ------ | ---------------------------------------------------------------------------------------  |
| schema-name (?)   | 属性   | JDBC 数据源别名，该参数可实现 JDBC 与 PROXY 共享配置                                               |
