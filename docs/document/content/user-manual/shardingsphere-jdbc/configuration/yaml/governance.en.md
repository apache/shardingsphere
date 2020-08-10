+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```yaml
orchestration:
  name: #Orchestration name
  registryCenter: # Registry Center
    type: #Orchestration instance type. Example:Zookeeper, etcd
    serverLists: #The list of servers that connect to orchestration instance, including IP and port number; use commas to separate
  additionalConfigCenter:
    type: #Orchestration instance type. Example:Zookeeper, etcd, Apollo, Nacos
    serverLists: #The list of servers that connect to orchestration instance, including IP and port number; use commas to separate
  overwrite: #Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```

### Cluster

```yaml
cluster:
  heartbeat:
    sql: #Heartbeat detection SQL
    threadCount: #Thread pool size 
    interval: #Heartbeat detection task interval (s)
    retryEnable: #Whether to enable retry, set true or false
    retryMaximum: #Maximum number of retry, effective when retryEnable is true
    retryInterval: #Retry interval (s), effective when retryEnable is true
```
