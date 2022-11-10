+++
pre = "<b>5.12. </b>"
title = "数据加密"
weight = 12
chapter = true
+++

## EncryptAlgorithm

### 全限定类名

[`org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/api/src/main/java/org/apache/shardingsphere/encrypt/spi/EncryptAlgorithm.java)

### 定义

数据加密算法

### 已知实现

| *配置标识* | *详细说明*              | *全限定类名* |
| -------------------- | -------------------------- | ---------------------------- |
| MD5                  | 基于 MD5 的数据加密算法   | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.MD5EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/encrypt/MD5EncryptAlgorithm.java) |
| AES                  | 基于 AES 的数据加密算法   | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.AESEncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/encrypt/AESEncryptAlgorithm.java) |
| RC4                  | 基于 RC4 的数据加密算法   | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.RC4EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/encrypt/RC4EncryptAlgorithm.java) |
| SM3                  | 基于 SM3 的数据加密算法   | [`org.apache.shardingsphere.encrypt.sm.algorithm.SM3EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/plugin/sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM3EncryptAlgorithm.java) |
| SM4                  | 基于 SM4 的数据加密算法   | [`org.apache.shardingsphere.encrypt.sm.algorithm.SM4EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/plugin/sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM4EncryptAlgorithm.java) |
| CHAR_DIGEST_FUZZY    | 用于模糊查询的数据加密算法 | [`org.apache.shardingsphere.encrypt.algorithm.fuzzy.CharDigestFuzzyEncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/fuzzy/CharDigestFuzzyEncryptAlgorithm.java) |