+++
title = "数据加密"
weight = 3
+++

## 配置示例

```properties
spring.shardingsphere.datasource.names=ds

spring.shardingsphere.datasource.ds.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.url=jdbc:mysql://localhost:3306/ds
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=root

spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.cipher-column=encrypt_user
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.assisted-query-column=assisted_user
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.plain-column=plain_user
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.encryptor-name=aes_encryptor

spring.shardingsphere.rules.encrypt.encryptors.aes_encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.aes_encryptor.props.aes.key.value=123456
```

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置

spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.cipher-column= # 加密列名称
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.assisted-query-column= # 查询列名称
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.plain-column= # 原文列名称
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.encryptor-name= # 加密算法名称

# 加密算法配置
spring.shardingsphere.rules.encrypt.encryptors.<encryptor-name>.type= # 加密算法类型
spring.shardingsphere.rules.encrypt.encryptors.<encryptor-name>.props.xxx= # 加密算法属性配置
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。
