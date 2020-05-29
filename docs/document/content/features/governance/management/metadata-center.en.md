+++
title = "Metadata Center"
weight = 3
+++

## Motivation

- Metadata is the core data of the data source used by ShardingSphere, which contains tables, columns, and indexes, etc. Metadata ensures that each component of ShardingSphere can run correctly.

- The metadata center organizes and manages the metadata in a unified manner to realize the unified loading of metadata, change notifications and data synchronization.

## Data Structure in Metadata Center

The metadata center stores metadata in YAML under the metadata node of the defined namespace and orchestration node, and each logical data source is stored independently.

```
├─orchestration-namespace
│   ├─orchestration-name
│   │   ├──metadata
│   │   │    ├──schema1
│   │   │    │    ├── [YAML text contents]     
│   │   │    ├──schema2
│   │   │    │    ├── [YAML text contents]    
│   │   │    ├──....
```

## YAML text contents

In the metadata center, metadata is divided into two parts: `configuredSchemaMetaData` and` unconfiguredSchemaMetaDataMap`.

Dynamic modification of metadata content is not supported currently.

```
configuredSchemaMetaData:
  tables:                                       # Tables
    t_order:                                    # table_name
      columns:                                  # Columns
        id:                                     # column_name
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
      indexs:                                   # Indexes
        t_user_order_id_index:                  # index_name
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
  ds_0:                                         # DataSources
    tables:                                     # Tables
      t_user:                                   # table_name
        columns:                                # Columns
          user_id:                              # column_name
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
        indexes:                                # Indexes
          t_user_order_id_index:                # index_name
            name: t_user_order_id_index
          t_user_user_id_index:
            name: t_user_user_id_index
          primary:
            name: PRIMARY
```

### configuredSchemaMetaData

Store metadata for all data sources configured with sharding rules.

### unconfiguredSchemaMetaDataMap

Store metadata for data sources that no sharding rules configured.

## Change Notifications

After DDL is executed through a certain Proxy instance, ShardingSphere stores new metadata in the metadata center first, and then notifies other Proxy instances to synchronize metadata from the metadata center by event broadcast mechanism to ensure metadata consistency.
