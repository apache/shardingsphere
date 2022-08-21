+++
title = "Readwrite-splitting"
weight = 2
+++

## Background
Read/write splitting YAML configuration is highly readable. The YAML format enables you to quickly understand the dependencies between read/write sharding rules. ShardingSphere automatically creates the ShardingSphereDataSource object according to the YAML configuration, which reduces unnecessary coding for users.

## Parameters

### Static Readwrite-splitting

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    <data-source-name> (+): # Logic data source name of readwrite-splitting
      static-strategy: # Readwrite-splitting type
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

### Dynamic Readwrite-splitting

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    <data-source-name> (+): # Logic data source name of readwrite-splitting
      dynamic-strategy: # Readwrite-splitting type
        auto-aware-data-source-name: # Database discovery logic data source name
        write-data-source-query-enabled: # All read data source are offline, write data source whether the data source is responsible for read traffic
      loadBalancerName: # Load balance algorithm name
  
  # Load balance algorithm configuration
  loadBalancers:
    <load-balancer-name> (+): # Load balance algorithm name
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
      staticStrategy:
        writeDataSourceName: write_ds
        readDataSourceNames:
          - read_ds_0
          - read_ds_1
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

## Related References

- [Read-write splitting-Core features](/en/features/readwrite-splitting/)
- [Java API: read-write splitting](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
- [Spring Boot Starter: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting/)
- [Spring namespace: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting/)
