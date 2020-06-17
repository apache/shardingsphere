+++
title = "数据加密"
weight = 3
+++

## 配置示例

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

## 配置项说明

```yaml
dataSource: # 省略数据源配置

rules:
- !ENCRYPT
  tables:
    <table_name> (+): # 加密表名称
      columns:
        <column_name> (+): # 加密列名称
          cipherColumn: # 密文列名称
          assistedQueryColumn (?):  # 查询辅助列名称
          plainColumn (?): # 原文列名称
          encryptorName: # 加密算法名称
  encryptors:
    <encryptor_name> (+): # 加解密算法名称
      type: # 加解密算法类型
      props: # 加解密算法属性配置
        # ...

props:
  query.with.cipher.column: true # 是否使用密文列查询
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。
