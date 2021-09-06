+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

```properties
spring.shardingsphere.schema.name= # JDBC data source alias
spring.shardingsphere.mode.type=Cluster  # Config persist mode。Such as：Cluster、Standalone、Memory(Default)
spring.shardingsphere.mode.repository.type=ZooKeeper  # Governance instance type. Example: Cluster(Zookeeper, etcd), Standalone(Local)
spring.shardingsphere.mode.repository.props.namespace=demo_spring_boot_ds_sharding # Registry center namespace
spring.shardingsphere.mode.repository.props.server-lists=localhost:2181 # The list of servers that connect to governance instance, including IP and port number; use commas to separate
spring.shardingsphere.mode.overwrite=true # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
