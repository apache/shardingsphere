+++
pre = "<b>3.5. </b>"
title = "SPI"
weight = 5
chapter = true
+++

## 背景

在ShardingSphere中，很多功能实现类的加载方式是通过SPI注入的方式完成的。[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)是一种为了被第三方实现或扩展的API。它可以用于实现框架扩展或组件替换。

之所以采用此种方式，一方面是出于对ShardingSphere整体架构设计及性能最优的考虑，另一方面是为了让高级用户通过实现ShardingSphere相应的接口，动态将用户自定义的实现类加载到ShardingSphere中，从而在保持ShardingSphere架构完整性与功能稳定性的情况下，满足用户不同场景的实际需求。

本章节汇总了ShardingSphere所有通过SPI方式载入的功能模块。初级用户可以使用ShardingSphere提供的内置实现类，并通过简单配置即可实现相应功能；高级用户还可参考各个功能模块的Interface进行自定义实现，从而满足您的现实场景需求。我们非常欢迎大家将您的实现类提交PR到我们的[gitHub上](https://github.com/apache/incubator-shardingsphere/pulls)，从而让更多用户从中收益。

### 数据脱敏
数据脱敏的Interface用于规定加解密器的加密、解密、类型获取、属性设置等方式。主要接口有两个：ShardingEncryptor, ShardingQueryAssistedEncryptor，其中ShardingEncryptor的内置实现类有AESShardingEncryptor，MD5ShardingEncryptor。有关加解密介绍，请参考[数据脱敏](/cn/features/orchestration/encrypt/)。相关接口如下所示：

```java
/**
 * Base algorithm SPI.
 *
 */
public interface BaseAlgorithm {
    
    /**
     * Get algorithm type.
     * 
     * @return type
     */
    String getType();
    
    /**
     * Get properties.
     * 
     * @return properties of algorithm
     */
    Properties getProperties();
    
    /**
     * Set properties.
     * 
     * @param properties properties of algorithm
     */
    void setProperties(Properties properties);
}


/**
 * Sharding encryptor.
 *
 */
public interface ShardingEncryptor extends BaseAlgorithm {
    
    /**
     * Encode.
     * 
     * @param plaintext plaintext
     * @return ciphertext
     */
    String encrypt(Object plaintext);
    
    /**
     * Decode.
     * 
     * @param ciphertext ciphertext
     * @return plaintext
     */
    Object decrypt(String ciphertext);
}


/**
 * Sharding query assisted encryptor.
 *
 */
public interface ShardingQueryAssistedEncryptor extends ShardingEncryptor {
    
    /**
     * Query assisted encrypt.
     * 
     * @param plaintext plaintext
     * @return ciphertext
     */
    String queryAssistedEncrypt(String plaintext);
}
```

### 分布式主键
分布式主键的Interface主要用于规定如何生成全局性的自增I。主要接口为ShardingKeyGenerator，其内置实现有UUIDShardingKeyGenerator，SnowflakeShardingKeyGenerator。有关自增主键的介绍，请参考[分布式主键](/cn/features/sharding/other-features/key-generator/)。相关接口如下所示：

```java
/**
 * Base algorithm SPI.
 *
 */
public interface BaseAlgorithm {
    
    /**
     * Get algorithm type.
     * 
     * @return type
     */
    String getType();
    
    /**
     * Get properties.
     * 
     * @return properties of algorithm
     */
    Properties getProperties();
    
    /**
     * Set properties.
     * 
     * @param properties properties of algorithm
     */
    void setProperties(Properties properties);
}

/**
 * Key generator.
 *
 */
public interface ShardingKeyGenerator extends BaseAlgorithm {
    
    /**
     * Generate key.
     * 
     * @return generated key
     */
    Comparable<?> generateKey();
}
```
