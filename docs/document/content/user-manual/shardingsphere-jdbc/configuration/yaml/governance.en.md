+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

```yaml
schemaName: #Optional. If it is not configured, logic_db is used as the schemaName by default. Through this parameter and management module, JDBC and PROXY can be online at the same time
mode:
  type: Cluster # Config persist mode。Such as：Cluster、Standalone、Memory(Default)
  repository:
    type: ZooKeeper # Governance instance type. Example: Cluster(Zookeeper, etcd), Standalone(Local)
    props:
      namespace: demo_yaml_ds_sharding # Registry center namespace
      server-lists: localhost:2181 # The list of servers that connect to governance instance, including IP and port number; use commas to separate
  overwrite: true # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
