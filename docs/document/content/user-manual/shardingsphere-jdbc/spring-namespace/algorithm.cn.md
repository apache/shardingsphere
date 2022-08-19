+++
title = "算法配置"
weight = 4
+++

## 分片算法

```xml
<!-- algorithmName 由用户指定，需要和分片策略中的 algorithm-ref 属性一致 -->
<!-- type 和 props，请参考分片内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/sharding/ -->
<sharding:sharding-algorithm id="algorithmName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</sharding:sharding-algorithm>
```

## 加密算法

```xml
<!-- encryptorName 由用户指定，需要和加密规则中的 encrypt-algorithm-ref 属性一致 -->
<!-- type 和 props，请参考加密内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/encrypt/ -->
<encrypt:encrypt-algorithm id="encryptorName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</encrypt:encrypt-algorithm>
```

## 读写分离负载均衡算法

```xml
<!-- loadBalancerName 由用户指定，需要和读写分离规则中的 load-balance-algorithm-ref 属性一致 -->  
<!-- type 和 props，请参考读写分离负载均衡内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/load-balance/ -->
<readwrite-splitting:load-balance-algorithm id="loadBalancerName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</readwrite-splitting:load-balance-algorithm>
```

## 影子算法

```xml
<!-- shadowAlgorithmName 由用户指定，需要和影子库则中的 shadow-algorithm-ref 属性一致 -->  
<!-- type 和 props，请参考影子库内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/shadow/ -->
<shadow:shadow-algorithm id="shadowAlgorithmName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</shadow:shadow-algorithm>
```

## 高可用

```xml
<!-- discoveryTypeName 由用户指定，需要和数据库发现规则中的 discovery-type-name 属性一致 -->  
<database-discovery:discovery-type id="discoveryTypeName" type="xxx">
    <props>
        <prop key="xxx">xxx</prop>
    </props>
</database-discovery:discovery-type>
```
