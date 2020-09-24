+++
title = "Read-write Split"
weight = 2
+++

## Configuration Item Explanation

```yaml
dataSource: # Omit data source configuration

rules:
- !PRIMARY_REPLICA_REPLICATION
  dataSources:
    <data-source-name> (+): # Logic data source name of primary-replica replication
      primaryDataSourceName: # Primary data source name
      replicaDataSourceNames: 
        - <slave-data-source-name> (+) # Replica data source name
      loadBalancerName: # Load balance algorithm name
  
  # Load balance algorithm configuration
  loadBalancers:
    <load-balancer-name> (+): # Load balance algorithm name
      type: # Load balance algorithm type
      props: # Load balance algorithm properties
        # ...

props:
  # ...
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
