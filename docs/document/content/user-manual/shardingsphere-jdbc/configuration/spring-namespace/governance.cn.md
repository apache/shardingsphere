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
命名空间: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration-5.0.0.xsd)

<orchestration:reg-center />

| *名称*         | *类型* | *说明*                                                                        |
| ------------- | ------ | ----------------------------------------------------------------------------- |
| id            | 属性   | 注册中心实例名称                                                                 |
| type          | 属性   | 注册中心类型。如：ZooKeeper, etcd                                                |
| server-lists  | 属性   | 注册中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |

<orchestration:config-center />

| *名称*         | *类型* | *说明*                                                                         |
| ------------- | ------ | ----------------------------------------------------------------------------- |
| id            | 属性   | 配置中心实例名称                                                                 |
| type          | 属性   | 配置中心类型。如：ZooKeeper, etcd, Apollo, Nacos                                 |
| server-lists  | 属性   | 配置中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |

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
 
    <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" reg-center-ref="regCenter" cluster-ref="cluster" />
    <cluster:heartbeat id="cluster" sql="select 1" threadCount="1" interval="60" retryEnable="false" retryMaximum="3" retryInterval="3"/>
</beans>
```

命名空间: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/cluster-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/cluster-5.0.0.xsd)

<cluster:heartbeat />

| *名称*             | *类型* | *说明*                              |
| ----------------- | ------ | ----------------------------------- |
| id                | 属性   | 心跳检测配置 ID                       |
| sql               | 属性   | 心跳检测 SQL                         |
| threadCount       | 属性   | 心跳检测线程池大小                    |
| interval          | 属性   | 心跳检测间隔秒数                      |
| retryEnable       | 属性   | 是否支持失败重试，可设置 true 或 false |
| retryMaximum (?)  | 属性   | 最大重试次数                         |
| retryInterval (?) | 属性   | 重试间隔时间 (s)                     |
