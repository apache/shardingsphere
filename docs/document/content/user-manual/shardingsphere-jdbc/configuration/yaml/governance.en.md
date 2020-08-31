+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```yaml
governance:
  name: #Governance name
  registryCenter: # Registry Center
    type: #Governance instance type. Example:Zookeeper, etcd
    serverLists: #The list of servers that connect to governance instance, including IP and port number; use commas to separate
  additionalConfigCenter:
    type: #Governance instance type. Example:Zookeeper, etcd, Apollo, Nacos
    serverLists: #The list of servers that connect to governance instance, including IP and port number; use commas to separate
  overwrite: #Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
