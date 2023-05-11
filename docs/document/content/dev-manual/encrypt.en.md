+++
pre = "<b>5.11. </b>"
title = "Encryption"
weight = 11
chapter = true
+++

## EncryptAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/api/src/main/java/org/apache/shardingsphere/encrypt/spi/EncryptAlgorithm.java)

### Definition

Data encrypt algorithm definition

### Implementation classes

| *Configuration Type* | *Description*                               | *Fully-qualified class name*                                                                                                                                                                                                                                       |
|----------------------|---------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AES                  | AES data encrypt algorithm                  | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.AESEncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/standard/AESEncryptAlgorithm.java)                 |
| RC4                  | RC4 data encrypt algorithm                  | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.RC4EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/standard/RC4EncryptAlgorithm.java)                 |
| SM3                  | SM3 data encrypt algorithm                  | [`org.apache.shardingsphere.encrypt.sm.algorithm.SM3EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/plugin/sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM3EncryptAlgorithm.java)                      |
| SM4                  | SM4 data encrypt algorithm                  | [`org.apache.shardingsphere.encrypt.sm.algorithm.SM4EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/plugin/sm/src/main/java/org/apache/shardingsphere/encrypt/sm/algorithm/SM4EncryptAlgorithm.java)                      |
| CHAR_DIGEST_LIKE     | Data encryption algorithms for like queries | [`org.apache.shardingsphere.encrypt.algorithm.like.CharDigestLikeEncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/like/CharDigestLikeEncryptAlgorithm.java) |
| MD5                  | MD5 assisted query encrypt algorithm        | [`org.apache.shardingsphere.encrypt.algorithm.encrypt.MD5EncryptAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/algorithm/assisted/MD5AssistedEncryptAlgorithm.java)                 |
