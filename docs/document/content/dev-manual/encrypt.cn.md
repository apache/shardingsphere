+++
pre = "<b>5.6. </b>"
title = "数据加密"
weight = 6
chapter = true
+++

## EncryptAlgorithm

### 全限定类名

[`org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/api/src/main/java/org/apache/shardingsphere/encrypt/spi/EncryptAlgorithm.java)

### 定义

数据加密算法

### 已知实现

| *配置标识*           | *详细说明*           | *全限定类名*                                                                                                                                                                                                                                                            |
|------------------|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AES              | 基于 AES 的数据加密算法   | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.AESEncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/standard/AESEncryptAlgorithm.java)                |
| MD5              | 基于 MD5 的辅助查询加密算法 | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.MD5EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/assisted/MD5AssistedEncryptAlgorithm.java)        |
