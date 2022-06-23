+++
title = "Readwrite-splitting"
weight = 2
+++

## Configuration Item Explanation

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    <data-source-name> (+): # Logic data source name of readwrite-splitting
      type: # Readwrite-splitting type, such as: Static, Dynamic
      props:
        auto-aware-data-source-name: # Auto aware data source name(Use with database discovery)
        write-data-source-query-enabled: # replicas all offline, primary data source whether provide query(Use with database discovery)
        write-data-source-name: # Write data source name
        read-data-source-names: # Read data source names, multiple data source names separated with comma
      loadBalancerName: # Load balance algorithm name
  
  # Load balance algorithm configuration
  loadBalancers:
    <load-balancer-name> (+): # Load balance algorithm name
      type: # Load balance algorithm type
      props: # Load balance algorithm properties
        # ...
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Use Norms](/en/features/readwrite-splitting/use-norms) for more details about query consistent routing.
