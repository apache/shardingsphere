+++
title = "混合规则"
weight = 9
+++

## 背景信息

ShardingSphere 涵盖了很多功能，例如，分库分片、读写分离、高可用、数据加密等。这些功能用户可以单独进行使用，也可以配合一起使用，下面是基于 YAML 的参数解释和配置示例。

## 参数解释

```yaml
rules:
- !SHARDING
  tables:
    <logic_table_name>: # 逻辑表名称:
      actualDataNodes: # 由逻辑数据源名 + 表名组成（参考 Inline 语法规则）
      tableStrategy: # 分表策略，同分库策略
        standard:
          shardingColumn: # 分片列名称
          shardingAlgorithmName: # 分片算法名称
      keyGenerateStrategy:
        column: # 自增列名称，缺省表示不使用自增主键生成器
        keyGeneratorName: # 分布式序列算法名称
  defaultDatabaseStrategy:
    standard:
      shardingColumn: # 分片列名称
      shardingAlgorithmName: # 分片算法名称
  shardingAlgorithms:
    <sharding_algorithm_name>: # 分片算法名称
      type: INLINE
      props:
        algorithm-expression: # INLINE 表达式
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: # INLINE 表达式
  keyGenerators:
    <key_generate_algorithm_name> (+): # 分布式序列算法名称
      type: # 分布式序列算法类型
      props: # 分布式序列算法属性配置
- !READWRITE_SPLITTING
  dataSources:
    <data_source_name>: # 读写分离逻辑数据源名称
      dynamicStrategy: # 读写分离类型
        autoAwareDataSourceName: # 数据库发现逻辑数据源名称
    <data_source_name>: # 读写分离逻辑数据源名称
      dynamicStrategy: # 读写分离类型
        autoAwareDataSourceName: # 数据库发现逻辑数据源名称
- !DB_DISCOVERY
  dataSources:
    <data_source_name>:
      dataSourceNames: # 数据源名称列表
        - ds_0
        - ds_1
        - ds_2
      discoveryHeartbeatName: # 检测心跳名称
      discoveryTypeName: # 数据库发现类型名称
    <data_source_name>:
      dataSourceNames: # 数据源名称列表
        - ds_3
        - ds_4
        - ds_5
      discoveryHeartbeatName: # 检测心跳名称
      discoveryTypeName: # 数据库发现类型名称
  discoveryHeartbeats:
    <discovery_heartbeat_name>: # 心跳名称
      props:
        keep-alive-cron: # cron 表达式，如：'0/5 * * * * ?'
  discoveryTypes:
    <discovery_type_name>: # 数据库发现类型名称
      type: # 数据库发现类型，如：MySQL.MGR 
      props:
        group-name:  # 数据库发现类型必要参数，如 MGR 的 group-name
- !ENCRYPT
  encryptors:
    <encrypt_algorithm_name> (+): # 加解密算法名称
      type: # 加解密算法类型
      props: # 加解密算法属性配置
    <encrypt_algorithm_name> (+): # 加解密算法名称
      type: # 加解密算法类型
  tables:
    <table_name>: # 加密表名称
      columns:
        <column_name> (+): # 加密列名称
          plainColumn (?): # 原文列名称
          cipherColumn: # 密文列名称
          encryptorName: # 密文列加密算法名称
          assistedQueryColumn (?):  # 查询辅助列名称
          assistedQueryEncryptorName:  # 查询辅助列加密算法名称
          likeQueryColumn (?):  # 模糊查询列名称
          likeQueryEncryptorName:  # 模糊查询列加密算法名称
      queryWithCipherColumn(?): # 该表是否使用加密列进行查询
```

## 配置示例

```yaml
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: replica_ds_${0..1}.t_order_${0..1}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: replica_ds_${user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
    t_order_item_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_item_${order_id % 2}
  keyGenerators:
    snowflake:
      type: SNOWFLAKE
- !READWRITE_SPLITTING
  dataSources:
    replica_ds_0:
      dynamicStrategy:
        autoAwareDataSourceName: readwrite_ds_0
    replica_ds_1:
      dynamicStrategy:
        autoAwareDataSourceName: readwrite_ds_1
- !DB_DISCOVERY
  dataSources:
    readwrite_ds_0:
      dataSourceNames:
        - ds_0
        - ds_1
        - ds_2
      discoveryHeartbeatName: mgr_heartbeat
      discoveryTypeName: mgr
    readwrite_ds_1:
      dataSourceNames:
        - ds_3
        - ds_4
        - ds_5
      discoveryHeartbeatName: mgr_heartbeat
      discoveryTypeName: mgr
  discoveryHeartbeats:
    mgr_heartbeat:
      props:
        keep-alive-cron: '0/5 * * * * ?'
  discoveryTypes:
    mgr:
      type: MySQL.MGR
      props:
        group-name: 558edd3c-02ec-11ea-9bb3-080027e39bd2
- !ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    md5_encryptor:
      type: MD5
    like_encryptor:
      type: CHAR_DIGEST_LIKE
  tables:
    t_encrypt:
      columns:
        user_id:
          plainColumn: user_plain
          cipherColumn: user_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: assisted_query_user
          assistedQueryEncryptorName: aes_encryptor
          likeQueryColumn: like_query_user
          likeQueryEncryptorName: like_encryptor
        order_id:
          cipherColumn: order_cipher
          encryptorName: md5_encryptor
      queryWithCipherColumn: true
```
