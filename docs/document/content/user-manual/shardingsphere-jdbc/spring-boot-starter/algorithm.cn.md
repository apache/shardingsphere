+++
title = "算法配置"
weight = 4
+++

## 分片算法

```properties
# sharding-algorithm-name 由用户指定，需要和分片策略中的 sharding-algorithm-name 属性一致
# type 和 props，请参考分片内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/sharding/
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.type=xxx
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.props.xxx=xxx
```

## 加密算法

```properties
# encrypt-algorithm-name 由用户指定，需要和加密规则中的 encryptor-name 属性一致
# type 和 props，请参考加密内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/encrypt/
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type=xxx
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx=xxx
```

## 读写分离负载均衡算法

```properties
# load-balance-algorithm-name 由用户指定，需要和读写分离规则中的 load-balancer-name 属性一致
# type 和 props，请参考读写分离负载均衡内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/load-balance/
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type=xxx
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx=xxx
```

## 影子算法

```properties
# shadow-algorithm-name 由用户指定，需要和影子库规则中的 shadow-algorithm-names 属性一致
# type 和 props，请参考影子库内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/shadow/
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type=xxx
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx=xxx
```

## 高可用

```properties
# discovery-type-name 由用户指定，需要和数据库发现规则中的 discovery-type-name 属性一致
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.type=xxx
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.props.xxx=xxx
```
