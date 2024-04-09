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
          assistedQuery:
            name: assisted_username
            encryptorName: assisted_encryptor
  encryptors:
    name_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    assisted_encryptor:
      type: MD5
      props:
        salt: 123456
```

## 相关参考
- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
