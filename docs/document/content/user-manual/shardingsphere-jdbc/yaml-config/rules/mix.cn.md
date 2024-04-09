+++
title = "混合规则"
weight = 10
+++

## 背景信息

ShardingSphere 涵盖了很多功能，例如，分库分片、读写分离、数据加密等。这些功能用户可以单独进行使用，也可以配合一起使用，下面是基于 YAML 的参数解释和配置示例。

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
          cipher:
            name: # 密文列名称
            encryptorName: # 密文列加密算法名称
          assistedQuery (?):
            name: # 查询辅助列名称
            encryptorName:  # 查询辅助列加密算法名称
          likeQuery (?):
            name: # 模糊查询列名称
            encryptorName:  # 模糊查询列加密算法名称
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
- !ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    assisted_encryptor:
      type: MD5
    like_encryptor:
      type: CHAR_DIGEST_LIKE
  tables:
    t_encrypt:
      columns:
        user_id:
          cipher:
            name: user_cipher
            encryptorName: aes_encryptor
          assistedQuery:
            name: assisted_query_user
            encryptorName: assisted_encryptor
          likeQuery:
            name: like_query_user
            encryptorName: like_encryptor
        order_id:
          cipher:
            name: order_cipher
            encryptorName: aes_encryptor
```
