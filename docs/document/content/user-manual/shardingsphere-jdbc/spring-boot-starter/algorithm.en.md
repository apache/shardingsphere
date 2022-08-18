+++
title = "Algorithm"
weight = 4
+++

## Sharding

```properties
# sharding-algorithm-name is specified by users and its property should be consistent with that of sharding-algorithm-name in the sharding strategy.
# type and props, please refer to the built-in sharding algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/sharding/
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.type=xxx
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.props.xxx=xxx
```

## Encryption

```properties
# encrypt-algorithm-name is specified by users, and its property should be consistent with that of encryptor-name in encryption rules. 
# type and props, please refer to the built-in encryption algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/encrypt/
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type=xxx
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx=xxx
```

## Read/Write Splitting Load Balancer

```properties
# load-balance-algorithm-name is specified by users, and its property has to be consistent with that of load-balancer-name in read/write splitting rules. 
# type and props, please refer to the built-in read/write splitting algorithm load balancer: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/load-balance/
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type=xxx
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx=xxx
```

## Shadow DB

```properties
# shadow-algorithm-name is specified by users, and its property has to be consistent with that of shadow-algorithm-names in shadow DB rules. 
# type and props, please refer to the built-in shadow DB algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/shadow/
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type=xxx
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx=xxx
```

## High Availability

```properties
# discovery-type-name is specified by users, and its property has to be consistent with that of discovery-type-name in database discovery rules. 
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.type=xxx
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.props.xxx=xxx
```
