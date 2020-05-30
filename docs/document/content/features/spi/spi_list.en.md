+++
pre = "<b>3.9.2. </b>"
title = "SPI List"
weight = 2
chapter = true
+++

## SQL Passing

### SQLParserConfiguration

| *SPI Name*                    | *Descript ion*                                        |
| ----------------------------- | ----------------------------------------------------- |
| SQLParserConfiguration        | Regulate for SQL parser ANTLR G4 file and AST visitor |

| *Implementation Class*        | *Description*                                         |
| ----------------------------- | ----------------------------------------------------- |
| MySQLParserConfiguration      | Based on MySQL's SQL parser                           |
| PostgreSQLParserConfiguration | Based on PostgreSQL's SQL parser                      |
| SQLServerParserConfiguration  | Based on SQLServer's SQL parser                       |
| OracleParserConfiguration     | Based on Oracle's SQL parser                          |
| SQL92ParserConfiguration      | Based on SQL92's SQL parser                           |

Please refer to [SQL Parsing](/en/features/sharding/principle/parse/) for the introduction.

## Database Protocol

### DatabaseProtocolFrontendEngine

| *SPI Name*                       | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| DatabaseProtocolFrontendEngine   | Regulate parse and adapter protocol of database access for ShardingSphere-Proxy |

| *Implementation Class*           | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| MySQLProtocolFrontendEngine      | Base on MySQL database protocol                                                 |
| PostgreSQLProtocolFrontendEngine | Base on postgreSQL database protocol                                            |

## Configuration

### ShardingSphereRuleBuilder

### YamlRuleConfigurationSwapper

## Kernel

### RuleMetaDataLoader

| *SPI Name*             | *Description*                           |
| ---------------------- | --------------------------------------- |
| RuleMetaDataLoader     | Used to initialize meta data            |

| *Implementation Class* | *Description*                           |
| ---------------------- | --------------------------------------- |
| ShardingMetaDataLoader | Used to initialize sharding meta data   |
| EncryptMetaDataLoader  | Used to initialize encryption meta data |

### RuleMetaDataDecorator

| *SPI Name*                | *Description*                        |
| ------------------------- | ------------------------------------ |
| RuleMetaDataDecorator     | Used to update meta data             |

| *Implementation Class*    | *Description*                        |
| ------------------------- | ------------------------------------ |
| ShardingMetaDataDecorator | Used to update sharding meta data    |
| EncryptMetaDataDecorator  | Used to update encryption meta data  |

### RouteDecorator

| *SPI Name*                | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| RouteDecorator            | Used to process routing results                 |

| *Implementation Class*    | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| ShardingRouteDecorator    | Used to process sharding routing results        |
| MasterSlaveRouteDecorator | Used to process master-slave routing results    |
| ReplicaRouteDecorator     | Used to process multi replica routing results   |
| ShadowRouteDecorator      | Used to process shadow database routing results |

### SQLRewriteContextDecorator

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| SQLRewriteContextDecorator         | Used to process SQL rewrite results            |

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| ShardingSQLRewriteContextDecorator | Used to process sharding SQL rewrite results   |
| EncryptSQLRewriteContextDecorator  | Used to process encryption SQL rewrite results |
| ShadowSQLRewriteContextDecorator   | Used to process shadow SQL rewrite results     |

### ExecuteGroupDecorator

| *SPI Name*                   | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ExecuteGroupDecorator        | Used by update data nodes group result |

| *Implementation Class*       | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ReplicaExecuteGroupDecorator | Used by multi replica data nodes group |

### ResultProcessEngine

| *SPI Name*                   | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ResultProcessEngine          | Used by merge engine to process result set            |

| *Implementation Class*       | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ShardingResultMergerEngine   | Used by merge engine to process sharding result set   |
| EncryptResultDecoratorEngine | Used by merge engine to process encryption result set |


## Data encryption

The data encryption interface is used to regulate the encryption, decryption, access type, property configuration and other methods of the encryptor.

There are mainly two interfaces, `ShardingEncryptor` and `ShardingQueryAssistedEncryptor` and built-in implementation types are `AESShardingEncryptor` and `MD5ShardingEncryptor`. 

Please refer to [data encryption](/en/features/orchestration/encrypt/) for the introduction.

## Distributed Primary Key

The distributed primary key interface is to regulate how to generate overall auto-increment, type access and property configurations.

Its main interface is `ShardingKeyGenerator` and built-in implementation types are `UUIDShardingKeyGenerator` and `SnowflakeShardingKeyGenerator`.

Please refer to [Distributed Primary Key](/en/features/sharding/other-features/key-generator/) for the introduction.

## Distributed Transaction

The distributed transaction interface is to regulate how to adapter distributed transaction to local transaction API.

Its main interface is `ShardingTransactionManager` and built-in implementation types are `XAShardingTransactionManager` and `SeataATShardingTransactionManager`.

Please refer to [Distributed Transaction](/en/features/transaction/) for the introduction.

## XA Transaction Manager

The XA transaction manager interface is to regulate how to adapter XA transaction manager's implementors to unified XA transaction manager API.

Its main interface is `XATransactionManager` and built-in implementation types are `AtomikosTransactionManager`, `NarayanaXATransactionManager` and `BitronixXATransactionManager`.

Please refer to [XA Transaction Manager](/en/features/concept/2pc-xa-transaction/) for the introduction.

## Registry Center

The registry center interface is used to regulate its initialization, data storage, data upgrade, monitoring and others.

Its main interface is `RegistryCenter` and built-in implementation types are Zookeeper.

Please refer to [Available Registry Center](/en/features/orchestration/supported-registry-repo/) for the introduction.
