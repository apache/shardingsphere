+++
title = "分布式治理"
weight = 6
+++

## 配置项说明

### 治理

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
Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

<orchestration:instance />

| *名称*                              | *类型* | *说明*                                                                                    |
| ----------------------------------- | ----- | ------------------------------------------------------------------------------------------|
| id                                  | 属性  | 治理实例名称                                                                 |
| orchestration-type                  | 属性  | 治理类型，多个类型用逗号分隔，例如 config_center, registry_center, metadata_center                       |
| instance-type                       | 属性  | 治理实例类型。如：zookeeper, etcd, apollo, nacos                                                           |
| server-lists                        | 属性  | 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181   |
| namespace (?)                       | 属性  | 治理命名空间                                                                         |
| props (?)                       | 属性  | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                                                                           |

### 集群管理

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
Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/cluster/cluster.xsd](http://shardingsphere.apache.org/schema/shardingsphere/cluster/cluster.xsd)

<cluster:heartbeat />

| *名称*                              | *类型* | *说明*                                                                                    |
| ----------------------------------- | ----- | ------------------------------------------------------------------------------------------|
| id                                  | 属性  | 心跳检测配置 ID                                                                   |
| sql                       | 属性  | 心跳检测 SQL                                                           |
| threadCount                  | 属性  | 心跳检测线程池大小                       |
| interval                        | 属性  | 心跳检测间隔时间 (s)   |
| retryEnable                       | 属性  | 是否支持失败重试，可设置 true 或 false                                                                         |
| retryMaximum (?)                       | 属性  | 最大重试次数                                                                           |
| retryInterval (?)                       | 属性  | 重试间隔时间 (s)                                                                           |
