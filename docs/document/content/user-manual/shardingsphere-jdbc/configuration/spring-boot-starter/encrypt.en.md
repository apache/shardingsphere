+++
title = "Encryption"
weight = 3
+++

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit data source configuration

spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.cipher-column= # Cipher column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-column= # Assisted query column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.plain-column= # Plain column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.encryptor-name= # Encrypt algorithm name

# Encrypt algorithm configuration
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type= # Encrypt algorithm type
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx= # Encrypt algorithm properties
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.
