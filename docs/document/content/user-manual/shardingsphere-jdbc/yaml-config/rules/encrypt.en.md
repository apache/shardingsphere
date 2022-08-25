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
    <table-name> (+): # Encrypt table name
      columns:
        <column-name> (+): # Encrypt logic column name
          cipherColumn: # Cipher column name
          assistedQueryColumn (?):  # Assisted query column name
          plainColumn (?): # Plain column name
          encryptorName: # Encrypt algorithm name
      queryWithCipherColumn(?): # The current table whether query with cipher column for data encrypt. 
    
  # Encrypt algorithm configuration
  encryptors:
    <encrypt-algorithm-name> (+): # Encrypt algorithm name
      type: # Encrypt algorithm type
      props: # Encrypt algorithm properties
        # ...

  queryWithCipherColumn: # Whether query with cipher column for data encrypt. User you can use plaintext to query if have
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
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          plainColumn: username_plain
          cipherColumn: username
          encryptorName: name-encryptor
        pwd:
          cipherColumn: pwd
          assistedQueryColumn: assisted_query_pwd
          encryptorName: pwd_encryptor
  encryptors:
    name-encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    pwd_encryptor:
      type: assistedTest
```

Read the YAML configuration to create a data source according to the createDataSource method of YamlShardingSphereDataSourceFactory.

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile());
```

## Related References

- [Core Feature: Data Encryption](/en/features/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
