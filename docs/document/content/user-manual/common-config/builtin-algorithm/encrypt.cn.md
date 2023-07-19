+++
title = "加密算法"
weight = 5
+++

## 背景信息

加密算法是 Apache ShardingSphere 的加密功能使用的算法，ShardingSphere 内置了多种算法，可以让用户方便使用。

## 参数解释

### 标准加密算法

#### AES 加密算法

类型：AES

可配置属性：

| *名称*                  | *数据类型* | *说明*                         |
|-----------------------|--------|------------------------------|
| aes-key-value         | String | AES 使用的 KEY                  |
| digest-algorithm-name | String | AES KEY 的摘要算法 (可选，默认值：SHA-1) |

#### RC4 加密算法

类型：RC4

可配置属性：

| *名称*          | *数据类型* | *说明*        |
|---------------|--------|-------------|
| rc4-key-value | String | RC4 使用的 KEY |

### 模糊加密算法

#### 单字符摘要模糊加密算法

类型：CHAR_DIGEST_LIKE

可配置属性：

| *名称*  | *数据类型* | *说明*               |
|-------|--------|--------------------|
| delta | int    | 字符Unicode码偏移量（十进制） |
| mask  | int    | 字符加密掩码（十进制）        |
| start | int    | 密文Unicode初始码（十进制）  |
| dict  | String | 常见字                |

### 辅助查询加密算法

#### MD5 辅助查询加密算法

类型：MD5

可配置属性：

| *名称* | *数据类型* | *说明*   |
|------|--------|--------|
| salt | String | 盐值（可选） |

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
          cipher:
            name: username
            encryptorName: name_encryptor
          likeQuery:
            name: name_like
            encryptorName: like_encryptor
  encryptors:
    like_encryptor:
      type: CHAR_DIGEST_LIKE
    name_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
```

## 相关参考
- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
