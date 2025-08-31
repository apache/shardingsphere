+++
title = "Encryption"
weight = 5
+++

## Background

The YAML configuration approach to data encryption is highly readable, with the YAML format enabling a quick understanding of dependencies between encryption rules.
Based on the YAML configuration, ShardingSphere automatically completes the creation of ShardingSphereDataSource objects, reducing unnecessary coding efforts for users.

## Parameters

```yaml
rules:
- !ENCRYPT
  tables:
    <table_name> (+): # Encrypt table name
      columns:
        <column_name> (+): # Encrypt logic column name
          cipher:
            name: # Cipher column name
            encryptorName: # Cipher encrypt algorithm name
          assistedQuery (?):
            name: # Assisted query column name
            encryptorName:  # Assisted query encrypt algorithm name
          likeQuery (?):
            name: # Like query column name
            encryptorName:  # Like query encrypt algorithm name 
    
  # Encrypt algorithm configuration
  encryptors:
    <encrypt_algorithm_name> (+): # Encrypt algorithm name
      type: # Encrypt algorithm type
      props: # Encrypt algorithm properties
        # ...
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/common-config/builtin-algorithm/encrypt) for more details about type of algorithm.

## Procedure

1. Configure data encryption rules in the YAML file, including data sources, encryption rules, global attributes, and other configuration items.
2. Using the createDataSource of calling the YamlShardingSphereDataSourceFactory object to create ShardingSphereDataSource based on the configuration information in the YAML file.

## Sample

The data encryption YAML configurations are as follows:

```yaml
dataSources:
  unique_ds:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          cipher:
            name: username
            encryptorName: aes_encryptor
          assistedQuery:
            name: assisted_query_username
            encryptorName: assisted_encryptor
          likeQuery:
            name: like_query_username
            encryptorName: like_encryptor
        pwd:
          cipher:
            name: pwd
            encryptorName: aes_encryptor
          assistedQuery:
            name: assisted_query_pwd
            encryptorName: assisted_encryptor
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
        digest-algorithm-name: SHA-1
    assisted_encryptor:
      type: MD5
    like_encryptor:
      type: CHAR_DIGEST_LIKE
```

Read the YAML configuration to create a data source according to the createDataSource method of YamlShardingSphereDataSourceFactory.

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile());
```

## Related References

- [Core Feature: Data Encryption](/en/features/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
