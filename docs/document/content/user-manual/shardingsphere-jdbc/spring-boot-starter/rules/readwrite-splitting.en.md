+++
title = "Readwrite splitting"
weight = 2
+++

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.auto-aware-data-source-name= # Auto aware data source name(Use with database discovery)
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.primary-data-source-name= # Write data source name
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.replica-data-source-names= # Read data source names, multiple data source names separated with comma
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # Load balance algorithm name

# Load balance algorithm configuration
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx= # Load balance algorithm properties
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Use Norms](/en/features/readwrite-splitting/use-norms) for more details about query consistent routing.
