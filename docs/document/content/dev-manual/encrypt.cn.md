+++
pre = "<b>5.12. </b>"
title = "数据加密"
weight = 12
chapter = true
+++

## EncryptAlgorithm

### 全限定类名

[`org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-api/src/main/java/org/apache/shardingsphere/encrypt/spi/EncryptAlgorithm.java)

### 定义

数据加密算法

### 已知实现

| *配置标识* | *详细说明*              | *全限定类名* |
| -------------------- | -------------------------- | ---------------------------- |
| MD5                  | 基于 MD5 的数据加密算法 | [`org.apache.shardingsphere.encryption.algorithm.MD5Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/MD5EncryptAlgorithm.java) |
| AES                  | 基于 AES 的数据加密算法 | [`org.apache.shardingsphere.encryption.algorithm.AESEncrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/AESEncryptAlgorithm.java) |
| RC4                  | 基于 RC4 的数据加密算法 | [`org.apache.shardingsphere.encryption.algorithm.RC4Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/RC4EncryptAlgorithm.java) |
| SM3                  | 基于 SM3 的数据加密算法 | [`org.apache.shardingsphere.encryption.algorithm.SM3Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-plugin/shardingsphere-encrypt-sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM3EncryptAlgorithm.java) |
| SM4                  | 基于 SM4 的数据加密算法 | [`org.apache.shardingsphere.encryption.algorithm.SM4Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-plugin/shardingsphere-encrypt-sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM4EncryptAlgorithm.java) |
