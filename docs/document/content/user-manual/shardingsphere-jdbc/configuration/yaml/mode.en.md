+++
title = "Mode"
weight = 5
+++

## Configuration Item Explanation

### Memory mode
```yaml
schema:
  name: # JDBC data source alias. Optional, if it is not configured, logic_db is used as the schemaName by default, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Memory
```

### Standalone mode
```yaml
schema:
  name: # JDBC data source alias. Optional, if it is not configured, logic_db is used as the schemaName by default, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Standalone
  repository:
    type: # Standalone Configuration persist type, such as: File
    props:
      path: # Configuration persist path
  overwrite: true # Local configurations overwrite file configurations or not; if they overwrite, each start takes reference of local configurations.
```

### Cluster mode
```yaml
schema:
  name: # JDBC data source alias. Optional, if it is not configured, logic_db is used as the schemaName by default, this parameter can help the configuration shared between JDBC driver and Proxy
mode:
  type: # Cluster
  repository:
    type: # Cluster persist type. Such as : Zookeeper，Etcd
    props:
      namespace: # Cluster instance namespace
      server-lists: # Zookeeper or Etcd server list. including IP and port number; use commas to separate
  overwrite: true # Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations.
```
