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
dataSource: #省略数据源配置

encryptRule:
  encryptors:
    <encrypt-algorithm-name>:
      type: #加解密算法类型，可自定义或选择内置类型：MD5/AES 
      props: #属性配置, 注意：使用AES加密算法，需要配置AES加密算法的KEY属性：aes.key.value
        aes.key.value: 
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: #存储明文的字段
          cipherColumn: #存储密文的字段
          assistedQueryColumn: #辅助查询字段，针对 QueryAssistedEncryptAlgorithm 类型的加解密算法进行辅助查询
          encryptorName: #加密算法名字
```

配置项：dataSource

可配置属性：

| *名称*        | *配置类型* | *说明*                      |
| ------------ | ---------- | -------------------------- |
| 数据源实现类   | 属性       | 数据源实现类名称，以 `!!` 开始 |
| 数据源属性配置 | 子配置项    | 数据源个性化属性配置          |

## 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

可配置属性：

| *名称*      | *数据类型*                                    | *说明*           |
| ----------- | -------------------------------------------- | --------------- |
| name        | String                                       | 表名称           |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | 加密列规则配置列表 |

### 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*        |
| ----------------------- | -------- | ------------- |
| logicColumn             | String   | 逻辑列名称     |
| cipherColumn            | String   | 密文列名称     |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| plainColumn (?)         | String   | 原文列名称     |
| encryptorName           | String   | 加密算法名称   |

## 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*           |
| ---------- | ---------- | ---------------- |
| name       | String     | 加解密算法名称     |
| type       | String     | 加解密算法类型     |
| properties | Properties | 加解密算法属性配置 |

Apache ShardingSphere 内置的加解密算法包括：

### MD5 加解密算法

类型：MD5

可配置属性：无

### AES 加解密算法

类型：AES

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| aes.key.value | String    | AES 使用的 KEY |

### RC4 加解密算法

类型：RC4

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| rc4.key.value | String    | RC4 使用的 KEY |
