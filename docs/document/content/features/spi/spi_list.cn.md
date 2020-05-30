+++
pre = "<b>3.9.2. </b>"
title = "SPI 列表"
weight = 2
chapter = true
+++

## SQL 解析

| *SPI 名称*                    | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| SQLParserConfiguration        | 用于规定用于解析 SQL 的 ANTLR 语法文件及其语法树访问器 |

| *已知实现类*                   | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| MySQLParserConfiguration      | 基于 MySQL 的SQL 解析器实现                        |
| PostgreSQLParserConfiguration | 基于 PostgreSQL 的SQL 解析器实现                   |
| SQLServerParserConfiguration  | 基于 SQLServer 的SQL 解析器实现                    |
| OracleParserConfiguration     | 基于 Oracle 的SQL 解析器实现                       |
| SQL92ParserConfiguration      | 基于 SQL92 的SQL 解析器实现                        |

有关SQL解析介绍，请参考[SQL解析](/cn/features/sharding/principle/parse/)。

## 数据库协议

| *SPI 名称*                       | *详细说明*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | 用于ShardingSphere-Proxy解析与适配访问数据库的协议 |

| *已知实现类*                      | *详细说明*                                      |
| -------------------------------- | ---------------------------------------------- |
| MySQLProtocolFrontendEngine      | 基于 MySQL 的数据库协议实现                      |
| PostgreSQLProtocolFrontendEngine | 基于 PostgreSQL 的SQL 解析器实现                 |

## 内核

| *SPI 名称*             | *详细说明*         |
| ---------------------- | ----------------- |
| RuleMetaDataLoader     | 用于元数据初始化    |

| *已知实现类*            | *详细说明*         |
| ---------------------- | ----------------- |
| ShardingMetaDataLoader | 用于分片元数据初始化 |
| EncryptMetaDataLoader  | 用于加密元数据初始化 |

***

| *SPI 名称*                | *详细说明*        |
| ------------------------ | ---------------- |
| RuleMetaDataDecorator    | 用于元数据更新     |

| *已知实现类*               | *详细说明*        |
| ------------------------- | ---------------- |
| ShardingMetaDataDecorator | 用于分片元数据更新 |
| EncryptMetaDataDecorator  | 用于加密元数据更新 |

***

| *SPI 名称*                | *详细说明*              |
| ------------------------- | --------------------- |
| RouteDecorator            | 用于处理路由结果        |

| *已知实现类*               | *详细说明*             |
| ------------------------- | --------------------- |
| ShardingRouteDecorator    | 用于处理分片路由结果     |
| MasterSlaveRouteDecorator | 用于处理读写分离路由结果 |
| ReplicaRouteDecorator     | 用于处理多副本路由结果   |
| ShadowRouteDecorator      | 用于处理影子库路由结果   |

***

| *SPI 名称*                         | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| SQLRewriteContextDecorator         | 用于处理 SQL 改写结果       |

| *已知实现类*                        | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| ShardingSQLRewriteContextDecorator | 用于处理分片 SQL 改写结果   |
| EncryptSQLRewriteContextDecorator  | 用于处理加密 SQL 改写结果   |
| ShadowSQLRewriteContextDecorator   | 用于处理影子库 SQL 改写结果 |

***

| *SPI 名称*                   | *详细说明*             |
| ---------------------------- | --------------------- |
| ExecuteGroupDecorator        | 用于修改数据节点分组结果 |

| *已知实现类*                  | *详细说明*             |
| ---------------------------- | --------------------- |
| ReplicaExecuteGroupDecorator | 用于多副本数据节点分组   |

***

| *SPI 名称*                   | *详细说明*           |
| ---------------------------- | ------------------- |
| ResultProcessEngine          | 用于处理结果集        |

| *已知实现类*                  | *详细说明*           |
| ---------------------------- | ------------------- |
| ShardingResultMergerEngine   | 用于处理分片结果集归并 |
| EncryptResultDecoratorEngine | 用于处理加密结果集改写 |

## 数据加密

数据加密的接口用于规定加解密器的加密、解密、类型获取、属性设置等方式。

主要接口有两个：`ShardingEncryptor`和`ShardingQueryAssistedEncryptor`，其中`ShardingEncryptor`的内置实现类有`AESShardingEncryptor`和`MD5ShardingEncryptor`。

有关加解密介绍，请参考[数据加密](/cn/features/orchestration/encrypt/)。

## 分布式主键

分布式主键的接口主要用于规定如何生成全局性的自增、类型获取、属性设置等。

主要接口为`ShardingKeyGenerator`，其内置实现类有`UUIDShardingKeyGenerator`和`SnowflakeShardingKeyGenerator`。

有分布式主键的介绍，请参考[分布式主键](/cn/features/sharding/other-features/key-generator/)。

## 分布式事务

分布式事务的接口主要用于规定如何将分布式事务适配为本地事务接口。

主要接口为`ShardingTransactionManager`，其内置实现类有`XAShardingTransactionManager`和`SeataATShardingTransactionManager`。

有关分布式事务的介绍，请参考[分布式事务](/cn/features/transaction/)。

## XA事务管理器

XA事务管理器的接口主要用于规定如何将XA事务的实现者适配为统一的XA事务接口。

主要接口为`XATransactionManager`，其内置实现类有`AtomikosTransactionManager`, `NarayanaXATransactionManager`和`BitronixXATransactionManager`。

有关XA事务管理器的介绍，请参考[XA事务管理器](/cn/features/concept/2pc-xa-transaction/)。

## 注册中心

注册中心的接口主要用于规定注册中心初始化、存取数据、更新数据、监控等行为。

主要接口为`RegistryCenter`，其内置实现类有Zookeeper。

相关介绍请参考[注册中心](/cn/features/orchestration/supported-registry-repo/)。
