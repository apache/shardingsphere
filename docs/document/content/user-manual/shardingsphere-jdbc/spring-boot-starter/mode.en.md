+++
title = "Mode Configuration"
weight = 1
+++

Default is Memory mode.

### Configuration Item Explanation

```properties
spring.shardingsphere.mode.type= # Type of mode configuration. Values could be: Memory, Standalone, Cluster
spring.shardingsphere.mode.repository= # Persist repository configuration. Memory type does not need persist
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```

### Memory Mode

```properties
spring.shardingsphere.mode.type=Memory
```

### Standalone Mode

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type= # Type of persist repository
spring.shardingsphere.mode.repository.props.<key>= # Properties of persist repository
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```

### Cluster Mode

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type= # Type of persist repository
spring.shardingsphere.mode.repository.namespace= # Namespace of registry center
spring.shardingsphere.mode.repository.serverLists= # Server lists of registry center
spring.shardingsphere.mode.repository.props.<key>= # Properties of persist repository
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```
