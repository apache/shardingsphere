+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

```yaml
governance:
  registryCenter: # Registry center
    type: # Governance instance type. Example:Zookeeper, etcd
    namespace: # Registry center namespace
    serverLists: # The list of servers that connect to governance instance, including IP and port number; use commas to separate
  overwrite: # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
