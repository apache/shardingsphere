+++
title = "混合规则"
weight = 6
+++

混合配置的规则项之间的叠加使用是通过数据源名称和表名称关联的。

如果前一个规则是面向数据源聚合的，下一个规则在配置数据源时，则需要使用前一个规则配置的聚合后的逻辑数据源名称；
同理，如果前一个规则是面向表聚合的，下一个规则在配置表时，则需要使用前一个规则配置的聚合后的逻辑表名称。

## 配置项说明

```yml
dataSources: # 配置真实存在的数据源作为名称
  write_ds:
    # ...省略具体配置
  read_ds_0:
    # ...省略具体配置
  read_ds_1:
    # ...省略具体配置

rules:
  - !SHARDING # 配置数据分片规则
    tables:
      t_user:
        actualDataNodes: ds.t_user_${0..1} # 数据源名称 `ds` 使用读写分离配置的逻辑数据源名称
        tableStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: t_user_inline
    shardingAlgorithms:
      t_user_inline:
        type: INLINE
        props:
          algorithm-expression: t_user_${user_id % 2}
  
  - !ENCRYPT # 配置数据加密规则
    tables:
      t_user: # 表名称 `t_user` 使用数据分片配置的逻辑表名称
        columns:
          pwd:
            plainColumn: plain_pwd
            cipherColumn: cipher_pwd
            encryptorName: encryptor_aes
    encryptors:
      encryptor_aes:
        type: aes
        props:
          aes-key-value: 123456abc
  
  - !READWRITE_SPLITTING # 配置读写分离规则
    dataSources:
      ds: # 读写分离的逻辑数据源名称 `ds` 用于在数据分片中使用
        writeDataSourceName: write_ds # 使用真实存在的数据源名称 `write_ds`
        readDataSourceNames:
          - read_ds_0 # 使用真实存在的数据源名称 `read_ds_0`
          - read_ds_1 # 使用真实存在的数据源名称 `read_ds_1`
        loadBalancerName: roundRobin
    loadBalancers:
      roundRobin:
        type: ROUND_ROBIN

props:
  sql-show: true
```
