+++
title = "Mode Configuration"
weight = 1
+++

## Configuration Item Explanation

```yaml
mode (?): # Default value is Memory
  type: # Type of mode configuration. Values could be: Memory, Standalone, Cluster
  repository (?): # Persist repository configuration. Memory type does not need persist
  overwrite: # Whether overwrite persistent configuration with local configuration
```

### Memory Mode

```yaml
mode:
  type: Memory
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
  overwrite: # Whether overwrite persistent configuration with local configuration
```

### Cluster Mode

```yaml
mode:
  type: Cluster
  repository:
    type: # Type of persist repository
    namespace: # Namespace of registry center
    serverLists: # Server lists of registry center
    props: # Properties of persist repository
      foo_key: foo_value
      bar_key: bar_value
  overwrite: # Whether overwrite persistent configuration with local configuration
```

Please refer to [Builtin Persist Repository List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/) for more details about type of repository.
