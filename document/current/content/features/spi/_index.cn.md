+++
pre = "<b>3.5. </b>"
title = "SPI"
weight = 5
chapter = true
+++

## 背景

在Apache ShardingSphere中，很多功能实现类的加载方式是通过SPI注入的方式完成的。
[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)是一种为了被第三方实现或扩展的API，它可以用于实现框架扩展或组件替换。

Apache ShardingSphere之所以采用SPI方式进行扩展，是出于整体架构最优设计考虑。
为了让高级用户通过实现Apache ShardingSphere提供的相应接口，动态将用户自定义的实现类加载其中，从而在保持Apache ShardingSphere架构完整性与功能稳定性的情况下，满足用户不同场景的实际需求。

本章节汇总了Apache ShardingSphere所有通过SPI方式载入的功能模块。
如无特殊需求，用户可以使用Apache ShardingSphere提供的内置实现，并通过简单配置即可实现相应功能；高级用户则可以参考各个功能模块的接口进行自定义实现。
我们非常欢迎大家将您的实现类反馈至[开源社区](https://github.com/apache/incubator-shardingsphere/pulls)，让更多用户从中收益。

### 数据脱敏

数据脱敏的接口用于规定加解密器的加密、解密、类型获取、属性设置等方式。
主要接口有两个：`ShardingEncryptor`和`ShardingQueryAssistedEncryptor`，其中`ShardingEncryptor`的内置实现类有`AESShardingEncryptor`和`MD5ShardingEncryptor`。
有关加解密介绍，请参考[数据脱敏](/cn/features/orchestration/encrypt/)。

### 分布式主键

分布式主键的接口主要用于规定如何生成全局性的自增、类型获取、属性设置等。
主要接口为`ShardingKeyGenerator`，其内置实现类有`UUIDShardingKeyGenerator`和`SnowflakeShardingKeyGenerator`。
有关自增主键的介绍，请参考[分布式主键](/cn/features/sharding/other-features/key-generator/)。

### 注册中心

注册中心的接口主要用于规定注册中心初始化、存取数据、更新数据、监控等行为。
主要接口为`RegistryCenter`，其内置实现类有Zookeeper, ETCD。相关介绍请参考[注册中心](/cn/features/orchestration/supported-registry-repo/)。