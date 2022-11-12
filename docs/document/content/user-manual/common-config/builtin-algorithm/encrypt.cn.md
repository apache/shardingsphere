+++
title = "加密算法"
weight = 5
+++

## 背景信息
加密算法是 Apache ShardingSphere 的加密功能使用的算法，ShardingSphere 内置了多种算法，可以让用户方便使用。

## 参数解释

### MD5 加密算法

类型：MD5

可配置属性：无

### AES 加密算法

类型：AES

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| aes-key-value | String    | AES 使用的 KEY |

### RC4 加密算法

类型：RC4

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| rc4-key-value | String    | RC4 使用的 KEY |

### SM3 加密算法

类型：SM3

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| sm3-salt      | String    | SM3 使用的 SALT（空或 8 Bytes） |

### SM4 加密算法

类型：SM4

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| sm4-key       | String    | SM4 使用的 KEY （16 Bytes） |
| sm4-mode      | String    | SM4 使用的 MODE （CBC 或 ECB） |
| sm4-iv        | String    | SM4 使用的 IV （MODE 为 CBC 时需指定，16 Bytes）|
| sm4-padding   | String    | SM4 使用的 PADDING （PKCS5Padding 或 PKCS7Padding，暂不支持 NoPadding）|

### 单字符摘要模糊算法

类型：CHAR_DIGEST_LIKE

可配置属性：

| *名称* | *数据类型* | *说明*                      |
| -------- | ------------ | ----------------------------- |
| delta    | int          | 字符Unicode码偏移量（十进制） |
| mask     | int          | 字符加密掩码（十进制）        |
| start    | int          | 密文Unicode初始码（十进制）   |
| dict     | String       | 常见字                        |

## 操作步骤
1. 在加密规则中配置加密器
2. 为加密器指定加密算法类型

## 配置示例
```yaml
rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          plainColumn: username_plain
          cipherColumn: username
          encryptorName: name-encryptor
          likeQueryColumn: name_like
          likeQueryEncryptorName: like-encryptor
  encryptors:
    like-encryptor:
      type: CHAR_DIGEST_LIKE
    name-encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
```

## 相关参考
- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
