+++
title = "Readwrite splitting"
weight = 2
+++

## Background
The read-write splitting configuration method of Spring Boot Starter is suitable for business scenarios using SpringBoot and can maximize the capabilities of initializing SringBoot configuration process and bean management to complete the creation of ShardingSphereDataSource object, reducing unnecessary coding work.

## Parameters Explained

### Static Readwrite-splitting

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.static-strategy.write-data-source-name= # Write data source name
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.static-strategy.read-data-source-names= # Read data source names, multiple data source names separated with comma
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # Load balance algorithm name

# Load balance algorithm configuration
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx= # Load balance algorithm properties
```

### Dynamic Readwrite-splitting

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.auto-aware-data-source-name= # Database discovery logic data source name
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.write-data-source-query-enabled= # All read data source are offline, write data source whether the data source is responsible for read traffic
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # Load balance algorithm name

# Load balance algorithm configuration
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx= # Load balance algorithm properties
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/common-config/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Read-write splitting-Core features](/en/features/readwrite-splitting/) for more details about query consistent routing.

## Operating Procedure
1. Add read/write splitting data source.
2. Set load-balancing algorithm.
3. Use read/write splitting data source.

## Configuration Examples
```properties
spring.shardingsphere.rules.readwrite-splitting.data-sources.readwrite_ds.static-strategy.write-data-source-name=write-ds
spring.shardingsphere.rules.readwrite-splitting.data-sources.readwrite_ds.static-strategy.read-data-source-names=read-ds-0,read-ds-1
spring.shardingsphere.rules.readwrite-splitting.data-sources.readwrite_ds.load-balancer-name=round_robin
spring.shardingsphere.rules.readwrite-splitting.load-balancers.round_robin.type=ROUND_ROBIN
```

## References
- [Read-write splitting-Core features](/en/features/readwrite-splitting/)
- [Java API: read-write splitting](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
- [YAML Configuration: read-write splitting](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)
- [Spring namespace: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting/)
