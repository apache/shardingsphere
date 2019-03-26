+++
pre = "<b>3.5. </b>"
title = "SPI"
weight = 5
chapter = true
+++

## Background

In Apache ShardingSphere, many functionality implementation uploads are finished through SPI. [Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is a kind of SPI for the third party to implement or expand, which can be applied in framework expansion or component replacement.

Apache ShardingSphere uses SPI to expand in order to optimize overall architecture design at most. It is to enable premier users, who have implemented interfaces provided by Apache ShardingSphere, dynamically load user's self-defined implementation types. Therefore, it can satisfy users' practical requirements for different scenarios, while keeping architecture integrity and functional stabilization.

This chapter has provided all the Apache ShardingSphere functional modules that are loaded through SPI. Users with no special requirements can use them to implement corresponding functions after simple configurations. Premier users can develop self-defined implementations, referring to interfaces of existing functional modules. Welcome to feed your self-defined implementations to the [open-source community](https://github.com/apache/incubator-shardingsphere/pulls), benefiting more users.

### Data Masking

Data masking interface is used to regulate the encryption, decryption, access type, property configuration and other methods of the encryptor. There are mainly two interfaces, `ShardingEncryptor` and `ShardingQueryAssistedEncryptor`, in which the built-in implementations of  `ShardingEncryptor` include `AESShardingEncryptor` and `MD5ShardingEncryptor`. Please refer to [Data Masking](/en/features/orchestration/encrypt/) for the introduction of encryption.

### Distributed Primary Key

Distributed primary key interface is used to regulate how to generate overall self-increment, type access and property configurations. Its main interface is `ShardingKeyGenerator`, built-in implementation types are `UUIDShardingKeyGenerator` and `SnowflakeShardingKeyGenerator`.  Please refer to [Distributed Primary Key](/en/features/sharding/other-features/key-generator/) for the introduction.

### Registry Center

Registry center interface is used to regulate the initialization, data storage, data upgrade, monitoring and other registry center actions. Its main interface is `RegistryCenter` and built-in implementation types are Zookeeper and ETCD. Please refer to [Available Registry Center](/en/features/orchestration/supported-registry-repo/) for the introduction.