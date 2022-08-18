+++
title = "Algorithm"
weight = 4
+++

## Sharding

```xml
<!-- algorithmName is specified by users and its property should be consistent with that of algorithm-ref in the sharding strategy. -->
<!-- type and props, please refer to the built-in sharding algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/sharding/ -->
<sharding:sharding-algorithm id="algorithmName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</sharding:sharding-algorithm>
```

## Encryption

```xml
<!-- encryptorName is specified by users, and its property should be consistent with that of encrypt-algorithm-ref in encryption rules. -->
<!-- type and props, please refer to the built-in encryption algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/encrypt/ -->
<encrypt:encrypt-algorithm id="encryptorName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</encrypt:encrypt-algorithm>
```

## Read/Write Splitting Load Balancer

```xml
<!-- loadBalancerName is specified by users, and its property has to be consistent with that of load-balance-algorithm-ref in read/write splitting rules. -->  
<!-- type and props, please refer to the built-in read/write splitting algorithm load balancer: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/load-balance/ -->
<readwrite-splitting:load-balance-algorithm id="loadBalancerName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</readwrite-splitting:load-balance-algorithm>
```

## Shadow DB

```xml
<!-- shadowAlgorithmName is specified by users, and its property has to be consistent with that of shadow-algorithm-ref in shadow DB rules. -->  
<!-- type and props, please refer to the built-in shadow DB algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/shadow/ -->
<shadow:shadow-algorithm id="shadowAlgorithmName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</shadow:shadow-algorithm>
```

## High Availability

```xml
<!-- discoveryTypeName is specified by users, and its property has to be consistent with that of discovery-type-name in database discovery rules. -->  
<database-discovery:discovery-type id="discoveryTypeName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</database-discovery:discovery-type>
```
