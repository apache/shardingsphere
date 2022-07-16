+++
pre = "<b>6.12. </b>"
title = "Encryption"
weight = 12
chapter = true
+++

## EncryptAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.encrypt.algorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-api/src/main/java/org/apache/shardingsphere/encrypt/spi/EncryptAlgorithm.java)

### Definition

Data encryption algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| MD5EncryptAlgorithm           | MD5 data encrypt algorithm | [`org.apache.shardingsphere.encryption.algorithm.MD5Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/MD5EncryptAlgorithm.java) |
| AESEncryptAlgorithm           | AES data encrypt algorithm | [`org.apache.shardingsphere.encryption.algorithm.AESEncrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/AESEncryptAlgorithm.java) |
| RC4EncryptAlgorithm           | RC4 data encrypt algorithm | [`org.apache.shardingsphere.encryption.algorithm.RC4Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/RC4EncryptAlgorithm.java) |
| SM3EncryptAlgorithm           | SM3 data encrypt algorithm | [`org.apache.shardingsphere.encryption.algorithm.SM3Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-plugin/shardingsphere-encrypt-sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM3EncryptAlgorithm.java) |
| SM4EncryptAlgorithm           | SM4 data encrypt algorithm | [`org.apache.shardingsphere.encryption.algorithm.SM4Encrypt`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-plugin/shardingsphere-encrypt-sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM4EncryptAlgorithm.java) |
