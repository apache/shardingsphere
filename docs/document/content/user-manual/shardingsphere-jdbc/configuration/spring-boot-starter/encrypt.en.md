+++
title = "Encryption"
weight = 3
+++

## Configuration Example

```properties
spring.shardingsphere.datasource.names=ds

spring.shardingsphere.datasource.ds.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.url=jdbc:mysql://localhost:3306/ds
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=root

spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.cipher-column=user_encrypt
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.assisted-query-column=user_assisted
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.plain-column=user_decrypt
spring.shardingsphere.rules.encrypt.tables.t_order.columns.user_id.encryptor-name=aes_encryptor

spring.shardingsphere.rules.encrypt.encryptors.aes_encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.aes_encryptor.props.aes.key.value=123456
```

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit data source configuration

spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.cipher-column= # Cipher column name
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.assisted-query-column= # Assisted query column name
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.plain-column= # Plain column name
spring.shardingsphere.rules.encrypt.tables.<table_name>.columns.<column_name>.encryptor-name= # Encrypt algorithm name

# Encrypt algorithm configuration
spring.shardingsphere.rules.encrypt.encryptors.<encryptor-name>.type= # Encrypt algorithm type
spring.shardingsphere.rules.encrypt.encryptors.<encryptor-name>.props.xxx= # Encrypt algorithm properties
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.
