+++
title = "Shadow DB"
weight = 5
+++

## Root Configuration

Class name: org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

Attributes:

| *Name*                     | *DataType*                                          | *Description*                                     | *Default Value* |
| -------------------------- | --------------------------------------------------- | ------------------------------------------------- | --------------- |
| enable                     | boolean                                             | Shadow database switch                            | false           |
| dataSources                | Map\<String, ShadowDataSourceConfiguration\>        | Shadow data source mapping name and configuration |                 |
| tables                     | Map\<String, ShadowTableConfiguration\>             | Shadow table name and configuration               |                 |
| defaultShadowAlgorithmName | String                                              | default shadow algorithm name                     |                 |
| shadowAlgorithms           | Map\<String, ShardingSphereAlgorithmConfiguration\> | Shadow algorithm name and configuration           |                 |

## Shadow Data Source Configuration

Class name: org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration

Attributes:

| *Name*               | *DataType* | *Description*               |
| -------------------- | ---------- | --------------------------- |
| sourceDataSourceName | String     | Production data source name |
| shadowDataSourceName | String     | Shadow data source name     |

## Shadow Table Configuration

Class name: org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration

Attributes:

| *Name*               | *DataType*           | *Description*                                  |
| -------------------- | -------------------- | ---------------------------------------------- |
| dataSourceNames      | Collection\<String\> | Shadow table location shadow data source names |
| shadowAlgorithmNames | Collection\<String\> | Shadow table location shadow algorithm names   |

## Shadow Algorithm Configuration

Please refer to [Built-in Shadow Algorithm List](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/shadow).
