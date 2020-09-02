+++
title = "元数据中心"
weight = 3
+++

## 实现动机

- 元数据是 ShardingSphere 所使用的数据源的表、列和索引等核心数据，元数据保障 ShardingSphere 各个组件能够正确运行。

- 元数据中心对元数据进行统一组织和管理，实现元数据的统一加载、变更通知和数据同步。

## 元数据中心数据结构

元数据中心在定义的命名空间和治理节点的 `metadata` 节点下，以 YAML 格式存储，每个逻辑数据源独立存储。

```
├─governance-name
│   ├──metadata
│   │    ├──schema_1
│   │    │    ├── [YAML text contents]
│   │    ├──schema_2
│   │    │    ├── [YAML text contents]
│   │    ├──....
```

## YAML Text Contents

在元数据中心中，元数据分为 `configuredSchemaMetaData` 和 `unconfiguredSchemaMetaDataMap` 两部分。

元数据内容目前不支持动态修改。

```
configuredSchemaMetaData:
  tables:                                       # 表
    t_order:                                    # 表名
      columns:                                  # 列
        id:                                     # 列名
          caseSensitive: false
          dataType: 0
          generated: false
          name: id
          primaryKey: trues
        order_id:
          caseSensitive: false
          dataType: 0
          generated: false
          name: order_id
          primaryKey: false
      indexs:                                   # 索引
        t_user_order_id_index:                  # 索引名
          name: t_user_order_id_index
    t_order_item:
      columns:
        order_id:
          caseSensitive: false
          dataType: 0
          generated: false
          name: order_id
          primaryKey: false
unconfiguredSchemaMetaDataMap:
  ds_0:                                         # 数据源
    tables:                                     # 表
      t_user:                                   # 表名
        columns:                                # 列
          user_id:                              # 列名
            caseSensitive: false
            dataType: 0
            generated: false
            name: user_id
            primaryKey: false
          id:
            caseSensitive: false
            dataType: 0
            generated: false
            name: id
            primaryKey: true
          order_id:
            caseSensitive: false
            dataType: 0
            generated: false
            name: order_id
            primaryKey: false
        indexes:                                # 索引
          t_user_order_id_index:                # 索引名
            name: t_user_order_id_index
          t_user_user_id_index:
            name: t_user_user_id_index
          primary:
            name: PRIMARY
```

### configuredSchemaMetaData

存储所有配置了分片规则的数据源的元数据。

### unconfiguredSchemaMetaDataMap

存储没有配置分片规则的数据源的元数据。

## 变更通知

通过某一个 ShardingSphere 实例执行 DDL 之后，
ShardingSphere 先将新的元数据存储到元数据中心，然后通过事件广播机制，通知其它 ShardingSphere 实例从元数据中心同步元数据，保证元数据一致。
