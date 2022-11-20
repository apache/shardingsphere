+++
title = "Encryption"
weight = 4
+++

## Background

The configuration method for Spring Boot Starter Data Encryption is suitable for business scenarios using SpringBoot and can make the most of SringBoot's configuration initialization and Bean management capabilities to complete the creation of ShardingSphereDataSource objects, reducing unnecessary coding work.

## Parameters

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.encrypt.tables.<table-name>.query-with-cipher-column= # Whether the table uses cipher columns for query
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.plain-column= # Plain column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.cipher-column= # Cipher column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.encryptor-name= # Encrypt algorithm name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-column= # Assisted query column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-encryptor-name# Assisted query encrypt algorithm name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.like-query-column= # Like query column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.like-query-encryptor-name# Like query encrypt algorithm name

# Encrypt algorithm configuration
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type= # Encrypt algorithm type
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx= # Encrypt algorithm properties

spring.shardingsphere.rules.encrypt.queryWithCipherColumn= # Whether query with cipher column for data encrypt. User you can use plaintext to query if have
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/common-config/builtin-algorithm/encrypt) for more details about type of algorithm.

## Procedure

1. Configure the data encryption rules in the SpringBoot file, including the data source, encryption rules, global properties and other items.
2. Start the SpringBoot program, which will automatically load the configuration and initialize the ShardingSphereDataSource.

## Sample

```properties
spring.shardingsphere.datasource.names=ds

spring.shardingsphere.datasource.ds.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.jdbc-url=jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=

spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.name-assisted-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-assisted-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.name-like-encryptor.type=CHAR_DIGEST_LIKE
spring.shardingsphere.rules.encrypt.encryptors.name-like-encryptor.props.delta=2
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.props.aes-key-value=123456abc

spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.cipher-column=username
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.encryptor-name=name-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.assisted-query-column=username_assisted
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.assisted-query-encryptor-name=name-assisted-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.like-query-column=username_like
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.like-query-encryptor-name=name-like-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.cipher-column=pwd
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.encryptor-name=pwd-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.query-with-cipher-column=true
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.query-with-cipher-column=false

spring.shardingsphere.props.sql-show=true
```

## Related References

- [Core Feature: Data Encryption](/en/features/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
