+++
title = "Mode"
weight = 1
+++

## Parameters

```yaml
mode (?): # Default value is Standalone
  type: # Type of mode configuration. Values could be: Standalone or Cluster
  repository (?): # Persist repository configuration
```

### Standalone Mode

```yaml
mode:
  type: Standalone
  repository:
    type: # Type of persist repository
    props: # Properties of persist repository
      foo_key: foo_value
      bar_key: bar_value
```

### Cluster Mode (recommended)

```yaml
mode:
  type: Cluster
  repository:
    type: # Type of persist repository
    props: # Properties of persist repository
      namespace: # Namespace of registry center
      server-lists: # Server lists of registry center
      foo_key: foo_value
      bar_key: bar_value
```

## Notes

1. Cluster mode deployment is recommended for production environment.
1. The `ZooKeeper` registry center is recommended for cluster mode deployment.
1. If there is configuration information in the `ZooKeeper`, please refer to the config information there.

## Sample

### Standalone Mode

```yaml
mode:
  type: Standalone
  repository:
    type: JDBC
```

### Cluster Mode (recommended)

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props: 
      namespace: governance
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
```

## Related References

- [Installation and Usage of ZooKeeper Registry Center](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- Please refer to [Builtin Persist Repository List](/en/user-manual/common-config/builtin-algorithm/metadata-repository/) for more details about the type of repository.
