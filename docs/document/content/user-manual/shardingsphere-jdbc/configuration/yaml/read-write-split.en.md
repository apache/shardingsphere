+++
title = "Read-write Split"
weight = 2
+++

## Configuration Item Explanation

```yaml
dataSource: # Omit data source configuration

rules:
- !MASTER_SLAVE
  dataSources:
    <data-source-name> (+): # Logic data source name of master slave
      masterDataSourceName: # Master data source name
      slaveDataSourceNames: 
        - <slave-data-source-name> (+) # Slave data source name
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
