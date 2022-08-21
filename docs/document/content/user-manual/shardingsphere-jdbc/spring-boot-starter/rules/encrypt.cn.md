+++
title = "数据加密"
weight = 4
+++

## 背景信息

数据加密 Spring Boot Starter 配置方式适用于使用 SpringBoot 的业务场景，能够最大程度地利用 SringBoot 配置初始化及 Bean 管理的能力，完成 ShardingSphereDataSource 对象的创建，减少不必要的编码工作。

## 参数解释

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.encrypt.tables.<table-name>.query-with-cipher-column= # 该表是否使用加密列进行查询
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.cipher-column= # 加密列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-column= # 查询列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.plain-column= # 原文列名称
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.encryptor-name= # 加密算法名称

# 加密算法配置
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type= # 加密算法类型
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx= # 加密算法属性配置

spring.shardingsphere.rules.encrypt.queryWithCipherColumn= # 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/common-config/builtin-algorithm/encrypt)。

## 操作步骤

1. 在 SpringBoot 文件中配置数据加密规则，包含数据源、加密规则、全局属性等配置项；
2. 启动 SpringBoot 程序，会自动加载配置，并初始化 ShardingSphereDataSource。

## 配置示例

```properties
spring.shardingsphere.datasource.names=ds

spring.shardingsphere.datasource.ds.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.jdbc-url=jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=

spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.props.aes-key-value=123456abc

spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.cipher-column=username
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.encryptor-name=name-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.cipher-column=pwd
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.encryptor-name=pwd-encryptor

spring.shardingsphere.props.query-with-cipher-column=true
spring.shardingsphere.props.sql-show=true
```

## 相关参考

- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
