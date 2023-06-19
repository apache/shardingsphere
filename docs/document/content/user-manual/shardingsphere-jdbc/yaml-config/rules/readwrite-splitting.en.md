+++
title = "Readwrite-splitting"
weight = 3
+++

## Background
Read/write splitting YAML configuration is highly readable. The YAML format enables you to quickly understand the dependencies between read/write sharding rules. ShardingSphere automatically creates the ShardingSphereDataSource object according to the YAML configuration, which reduces unnecessary coding for users.

## Parameters

### Readwrite-splitting

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    <data_source_name> (+): # Logic data source name of readwrite-splitting
      write_data_source_name: # Write data source name
      read_data_source_names: # Read data source names, multiple data source names separated with comma
      transactionalReadQueryStrategy (?): # Routing strategy for read query within a transaction, values include: PRIMARY (to primary), FIXED (to fixed data source), DYNAMIC (to any data source), default value: DYNAMIC
      loadBalancerName: # Load balance algorithm name
  
  # Load balance algorithm configuration
  loadBalancers:
    <load_balancer_name> (+): # Load balance algorithm name
      type: # Load balance algorithm type
      props: # Load balance algorithm properties
        # ...
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/common-config/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Read-write splitting-Core features](/en/features/readwrite-splitting/) for more details about query consistent routing.

## Procedure
1. Add read/write splitting data source.
2. Set the load balancer algorithm.
3. Use read/write data source.

## Sample
```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    readwrite_ds:
      writeDataSourceName: write_ds
      readDataSourceNames:
        - read_ds_0
        - read_ds_1
      transactionalReadQueryStrategy: PRIMARY
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

## Related References

- [Read-write splitting-Core features](/en/features/readwrite-splitting/)
- [Java API: read-write splitting](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
