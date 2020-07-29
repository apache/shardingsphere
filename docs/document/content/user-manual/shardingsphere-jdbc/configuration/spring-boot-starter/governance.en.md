+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```properties
spring.shardingsphere.orchestration.spring_boot_ds_sharding.type= #Orchestration instance type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.orchestration.spring_boot_ds_sharding.server-lists= #The list of servers that connect to orchestration instance, including IP and port number; use commas to separate
spring.shardingsphere.orchestration.spring_boot_ds_sharding.namespace= #Orchestration namespace
spring.shardingsphere.orchestration.spring_boot_ds_sharding.properties.overwrite= #Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```

### Cluster

```properties
spring.shardingsphere.cluster.heartbeat.sql= #Heartbeat detection SQL
spring.shardingsphere.cluster.heartbeat.interval= #Heartbeat detection task interval (s)
spring.shardingsphere.cluster.heartbeat.threadCount= #Thread pool size
spring.shardingsphere.cluster.heartbeat.retryEnable= #Whether to enable retry, set true or false
spring.shardingsphere.cluster.heartbeat.retryInterval= #Retry interval (s), effective when retryEnable is true
spring.shardingsphere.cluster.heartbeat.retryMaximum= #Maximum number of retry, effective when retryEnable is true
```
