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
       xmlns:governance="http://shardingsphere.apache.org/schema/shardingsphere/governance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/governance
                           http://shardingsphere.apache.org/schema/shardingsphere/governance/governance.xsd
">
    
    <governance:reg-center id="regCenter" type="ZooKeeper" server-lists="localhost:2181" />
    <governance:config-center id="configCenter" type="ZooKeeper" server-lists="localhost:2182" />
    <governance:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" reg-center-ref="regCenter" config-center-ref="configCenter" overwrite="true" />
</beans>
```
命名空间: [http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd)

<governance:reg-center />

| *名称*         | *类型* | *说明*                                                                        |
| ------------- | ------ | ----------------------------------------------------------------------------- |
| id            | 属性   | 注册中心实例名称                                                                 |
| type          | 属性   | 注册中心类型。如：ZooKeeper, etcd                                                |
| server-lists  | 属性   | 注册中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |

<governance:config-center />

| *名称*         | *类型* | *说明*                                                                         |
| ------------- | ------ | ----------------------------------------------------------------------------- |
| id            | 属性   | 配置中心实例名称                                                                 |
| type          | 属性   | 配置中心类型。如：ZooKeeper, etcd, Apollo, Nacos                                 |
| server-lists  | 属性   | 配置中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |
