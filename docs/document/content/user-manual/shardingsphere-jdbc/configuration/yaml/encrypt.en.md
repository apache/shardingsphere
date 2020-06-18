+++
title = "Encryption"
weight = 3
+++

## Configuration Example

```yaml
dataSource: !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/ds_name
  username: root
  password: root

rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        pwd:
          cipherColumn: pwd_cipher
          plainColumn: pwd_plain
          encryptorName: aes_encryptor
        phone:
          cipherColumn: phone_cipher
          encryptorName: md5_encryptor
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes.key.value: 123456
    md5_encryptor:
      type: MD5

props:
  query.with.cipher.column: true
```

## Configuration Item Explanation

```yaml
dataSource: # Ignore data source configuration

rules:
- !ENCRYPT
  tables:
    <table_name> (+): # Encrypt table name
      columns:
        <column_name> (+): # Encrypt logic column name
          cipherColumn: # Cipher column name
          assistedQueryColumn (?):  # Assisted query column name
          plainColumn (?): # Plain column name
          encryptorName: # Encrypt algorithm name
  encryptors:
    <encryptor_name> (+): # Encrypt algorithm name
      type: # Encrypt algorithm type
      props: # Encrypt algorithm properties
        # ...

props:
  # ...
```

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.
