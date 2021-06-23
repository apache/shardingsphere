+++
title = "数据加密"
weight = 3
+++

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.cipher-column= # 加密列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-column= # 查询列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.plain-column= # 原文列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.encryptor-name= # 加密算法名称

# 加密算法配置
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type= # 加密算法类型
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx= # 加密算法属性配置

spring.shardingsphere.rules.encrypt.queryWithCipherColumn= # 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。
