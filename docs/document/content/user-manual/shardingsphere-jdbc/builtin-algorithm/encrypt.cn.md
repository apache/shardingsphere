+++
title = "加密算法"
weight = 5
+++

## MD5 加密算法

类型：MD5

可配置属性：无

## AES 加密算法

类型：AES

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| aes-key-value | String    | AES 使用的 KEY |

## RC4 加密算法

类型：RC4

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| rc4-key-value | String    | RC4 使用的 KEY |

## SM3 加密算法

类型：SM3

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| sm3-salt      | String    | SM3 使用的 SALT（空 或 8 Bytes） |

## SM4 加密算法

类型：SM4

可配置属性：

| *名称*         | *数据类型* | *说明*         |
| ------------- | --------- | ------------- |
| sm4-key       | String    | SM4 使用的 KEY （16 Bytes） |
| sm4-mode      | String    | SM4 使用的 MODE （CBC 或 ECB） |
| sm4-iv        | String    | SM4 使用的 IV （MODE为CBC时需指定，16 Bytes）|
| sm4-padding   | String    | SM4 使用的 PADDING （PKCS5Padding 或 PKCS7Padding，暂不支持NoPadding）|