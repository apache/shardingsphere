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
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave"
       xmlns:orchestration="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd
">
    
    <orchestration:instance id="regCenter" orchestration-type="registry_center,config_center,metadata_center" instance-type="zookeeper" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo">
        <props>
            <prop key="overwrite">true</prop>
        </props>
     </orchestration:instance>
    <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" instance-ref="regCenter" />
</beans>
```
Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd)

<orchestration:instance />

| *Name*                              | *Type* | *Description*                                                                                    |
| ----------------------------------- | ----- | ------------------------------------------------------------------------------------------|
| id                                  | Attribute  | Orchestration instance name                                                                 |
| orchestration-type                  | Attribute  | Orchestration type, use commas to separate, such as: config_center,registry_center,metadata_center                       |
| instance-type                       | Attribute  | Orchestration instance type. Example:zookeeper, etcd, apollo, nacos                                                           |
| server-lists                        | Attribute  | The list of servers that connect to orchestration instance, including IP and port number; use commas to separate   |
| namespace (?)                       | Attribute  | Orchestration namespace                                                                         |
| props (?)                       | Attribute  | Properties for center instance config, such as options of zookeeper    

### Cluster

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:orchestration="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/cluster"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/cluster/cluster.xsd
                           ">
 
    <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" instance-ref="regCenter" cluster-ref="cluster" />
    <cluster:heartbeat id="cluster" sql="select 1" threadCount="1" interval="60" retryEnable="false" retryMaximum="3" retryInterval="3"/>
</beans>
```
Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/cluster-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/cluster-5.0.0.xsd)

<cluster:heartbeat />

| *Name*                              | *Type* | *Description*                                                                                    |
| ----------------------------------- | ----- | ------------------------------------------------------------------------------------------|
| id                                  | Attribute  | Heartbeat detection ID                                                                   |
| sql                       | Attribute  | Heartbeat detection SQL                                                           |
| threadCount                  | Attribute  | Thread pool size                       |
| interval                        | Attribute  | Heartbeat detection task interval (s)   |
| retryEnable                       | Attribute  | Whether to enable retry, set true or false                                                                         |
| retryMaximum (?)                       | Attribute  | Maximum number of retry, effective when retryEnable is true                                                                           |
| retryInterval (?)                       | Attribute  | Retry interval (s), effective when retryEnable is true                                                                           |
