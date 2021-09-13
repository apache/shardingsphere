+++
title = "Mode"
weight = 5
+++

## Configuration Item Explanation

### Memory mode
```properties
spring.shardingsphere.mode.type= # Memory
```

### Standalone mode
```properties
spring.shardingsphere.mode.type= # Standalone
spring.shardingsphere.mode.repository.type= # Standalone Configuration persist type, such as: File
spring.shardingsphere.mode.repository.props.path= # Configuration persist path
spring.shardingsphere.mode.overwrite= # Local configurations overwrite file configurations or not; if they overwrite, each start takes reference of local configurations.
```

### Cluster mode
```properties
spring.shardingsphere.mode.type= # Cluster
spring.shardingsphere.mode.repository.type= # Cluster persist type. Such as : Zookeeper，Etcd
spring.shardingsphere.mode.repository.props.namespace= # Cluster instance namespace
spring.shardingsphere.mode.repository.props.server-lists= # Zookeeper or Etcd server list. including IP and port number; use commas to separate
spring.shardingsphere.mode.overwrite= # Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations.
```
