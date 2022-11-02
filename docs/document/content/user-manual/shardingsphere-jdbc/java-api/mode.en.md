+++
title = "Mode"
weight = 1
chapter = true
+++

## Background

Build the running mode through Java API.

## Parameters

Class name: org.apache.shardingsphere.infra.config.mode.ModeConfiguration

Attributes:

| *Name*     | *DataType*                     | *Description*                                                                                                                                                    | *Default Value* |
| ---------- | ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| type       | String                         | Type of mode configuration<br />Values could be: Standalone or Cluster                                                                                           | Standalone      |
| repository | PersistRepositoryConfiguration | Persist repository configuration<br />Standalone type uses StandalonePersistRepositoryConfiguration<br />Cluster type uses ClusterPersistRepositoryConfiguration |                 |

### Standalone Persist Configuration

Class name: org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

Attributes:

| *Name* | *DataType* | *Description*                    |
| ------ | ---------- | -------------------------------- |
| type   | String     | Type of persist repository       |
| props  | Properties | Properties of persist repository |

### Cluster Persist Configuration

Class name: org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

Attributes:

| *Name*       | *Data Type* | *Description*                    |
| ------------ | ----------- | -------------------------------- |
| type         | String      | Type of persist repository       |
| namespace    | String      | Namespace of registry center     |
| server-lists | String      | Server lists of registry center  |
| props        | Properties  | Properties of persist repository |

## Notes

1. Cluster mode deployment is recommended for production environment.
1. The `ZooKeeper` registry center is recommended for cluster mode deployment. 
1. If there is configuration information in the `ZooKeeper`, please refer to the config information there.

## Procedure

### Introduce Maven Dependency

```xml
<dependency>
 <groupId>org.apache.shardingsphere</groupId>
 <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
 <version>${latest.release.version}</version>
</dependency>
```

> Notice: Please change `${latest.release.version}` to the actual version.

## Sample

### Standalone Mode

```java
ModeConfiguration modeConfig = createModeConfiguration();
Map<String, DataSource> dataSourceMap = ... // Building real data sources
Collection<RuleConfiguration> ruleConfigs = ... // Build specific rules
Properties props = ... // Build property configuration
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);

private ModeConfiguration createModeConfiguration() {
    return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("JDBC", new Properties()));
}
```

### Cluster Mode (Recommended)

```java
ModeConfiguration modeConfig = createModeConfiguration();
Map<String, DataSource> dataSourceMap = ... // Building real data sources
Collection<RuleConfiguration> ruleConfigs = ... // Build specific rules
Properties props = ... // Build property configuration
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);

private ModeConfiguration createModeConfiguration() {
    return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "governance-sharding-db", "localhost:2181", new Properties()));
}
```

## Related References

- [Installation and Usage of ZooKeeper Registry Center](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- Please refer to [Builtin Persist Repository List](/en/user-manual/common-config/builtin-algorithm/metadata-repository/) for more details about type of repository.
