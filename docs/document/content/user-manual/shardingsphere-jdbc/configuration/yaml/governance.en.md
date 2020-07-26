+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```yaml
orchestration:
  demo_yaml_ds_sharding: #Orchestration instance name
    instanceType: #Orchestration instance type. Example:zookeeper, etcd, apollo, nacos
    serverLists: #The list of servers that connect to orchestration instance, including IP and port number; use commas to separate
    namespace: #Orchestration namespace
    props: #Properties for center instance config, such as options of zookeeper
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
