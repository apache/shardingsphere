+++
title = "模式配置"
weight = 1
+++

## Parameters

```properties
spring.shardingsphere.mode.type= # Type of mode configuration. Value could be: Standalone or Cluster
spring.shardingsphere.mode.repository= # Persist repository configuration
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```

### Standalone Mode

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type= # Type of persist repository
spring.shardingsphere.mode.repository.props.<key>= # Properties of persist repository
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```

### Cluster Mode (Recommended)

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type= # Type of persist repository
spring.shardingsphere.mode.repository.props.namespace= # Namespace of registry center
spring.shardingsphere.mode.repository.props.server-lists= # Server lists of registry center
spring.shardingsphere.mode.repository.props.<key>= # Properties of persist repository
spring.shardingsphere.mode.overwrite= # Whether overwrite persistent configuration with local configuration
```

## Notes

1. Cluster mode deployment is recommended for production environments.
1. The 'ZooKeeper' registry center is recommended for cluster mode deployment.

## Procedure
1. Import MAVEN dependency.

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> Note: please change `${latest.release.version}' to the actual version number.
> 
## Sample

### Standalone Mode

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type=File
spring.shardingsphere.mode.repository.props.path=.shardingsphere
spring.shardingsphere.mode.overwrite=false
```

### Cluster Mode (Recommended)

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type=ZooKeeper
spring.shardingsphere.mode.repository.props.namespace=governance
spring.shardingsphere.mode.repository.props.server-lists=localhost:2181
spring.shardingsphere.mode.repository.props.retryIntervalMilliseconds=500
spring.shardingsphere.mode.repository.props.timeToLiveSeconds=60
spring.shardingsphere.mode.overwrite=false
```

## Related References

- [Installation and Usage of ZooKeeper Registry Center](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- Please refer to [Builtin Persist Repository List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/) for more details about type of repository.
