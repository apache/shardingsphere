+++
title = "Shadow DB"
weight = 4
+++

## Root Configuration

Class name: org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

Attributes:

| *Name* | *DataType* | *Description* | *Default Value* |
| ------ | ---------- | ------------- | --------------- |
| enable | boolean    | Shadow DB switch. Optional values: true/false |false|
| dataSources | Map\<String, ShadowDataSourceConfiguration\> | Shadow data source mapping name and configuration | None |
| tables | Map\<String, ShadowTableConfiguration\> | Shadow table name and configuration | None |
| defaultShadowAlgorithmName | String | default shadow algorithm name | Option item |
| shadowAlgorithms | Map\<String, ShardingSphereAlgorithmConfiguration\> | Shadow algorithm name and configuration | None |

## Shadow Data Source Configuration

Class name: org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration

Attributes:

| *Name* | *DataType* | *Description* | *Default Value* |
| ------ | ---------- | ------------- | --------------- |
| sourceDataSourceName | String | Production data source name | None |
| shadowDataSourceName | String | Shadow data source name | None |

## Shadow Table Configuration

Class name: org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration

Attributes:

| *Name* | *DataType* | *Description* | *Default Value* |
| ------ | ---------- | ------------- | --------------- |
| dataSourceNames | Collection\<String\> | Shadow table location shadow data source names | None |
| shadowAlgorithmNames | Collection\<String\> | Shadow table location shadow algorithm names | None |

## Shadow Algorithm Configuration

Please refer to [Built-in Shadow Algorithm List](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/shadow).
