+++
title = "mode"
weight = 5
+++

## Configuration Item Explanation

### Memory mode
```yaml
schema:
  name: # JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Memory mode
```

### Standalone mode
```yaml
schema:
  name: # JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Standalone mode
  repository:
    type: # File type
    props:
      path: # Configuration persist path
  overwrite: true # Local configurations overwrite file configurations or not; if they overwrite, each start takes reference of local configurations.
```

### Cluster mode
```yaml
schema:
  name: # JDBC data source alias, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Cluster mode
  repository:
    type: # ZooKeeper or Etcd
    props:
      namespace: # Cluster instance namespace
      server-lists: # Zookeeper or Etcd server list. including IP and port number; use commas to separate
  overwrite: true # Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations.
```
