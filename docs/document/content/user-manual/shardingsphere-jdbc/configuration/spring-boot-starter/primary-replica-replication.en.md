+++
title = "Primary-Replica Replication"
weight = 2
+++

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit data source configuration

spring.shardingsphere.rules.primary-replica-replication.data-sources.<primary-replica-replication-data-source-name>.primary-data-source-name= # Primary data source name
spring.shardingsphere.rules.primary-replica-replication.data-sources.<primary-replica-replication-data-source-name>.replica-data-source-names= # Replica data source names, multiple data source names separated with comma
spring.shardingsphere.rules.primary-replica-replication.data-sources.<primary-replica-replication-data-source-name>.load-balancer-name= # Load balance algorithm name

# Load balance algorithm configuration
spring.shardingsphere.rules.primary-replica-replication.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
spring.shardingsphere.rules.primary-replica-replication.load-balancers.<load-balance-algorithm-name>.props.xxx= # Load balance algorithm properties
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
