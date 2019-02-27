+++
pre = "<b>4.1.2. </b>"
toc = true
title = "Configuration manual"
weight = 2
chapter = true
+++

## Introduction

As the core of Sharding-JDBC, configuration is the only module in Sharding-JDBC that has something to do with application developers. 
Configuration module is also the portal of Sharding-JDBC, through which users can fast and clearly understand the functions provided by Sharding-JDBC.

This part is a configuration manual for Sharding-JDBC, which can also be referred to as a dictionary if necessary.

Sharding-JDBC has provided 4 kinds of configuration methods for different situations. 
By configuration, application developers can flexibly use sharding databases with sharding tables, read-write split or the combination of both.

![The class diagrams for Domain Model](https://shardingsphere.apache.org/document/current/img/config_domain.png)

## Factory API

The yellow part in the diagram indicates the API entrance of Sharding-JDBC, provided in factory method. 
There are two kinds of factories, `ShardingDataSourceFactory ` and `MasterSlaveDataSourceFactory` for now. 
`ShardingDataSourceFactory` is used to create JDBC drivers of sharding databases with sharding tables or sharding databases with sharding tables+read-write split; `MasterSlaveDataSourceFactory` is used to create JDBC drivers of read-write split.

## Configuration Object

The yellow part in the diagram indicates configuration objects of Sharding-JDBC, which provides flexible configuration methods. 
`ShardingRuleConfiguration` is the core and entrance of sharding database and sharding table configuration, which can include many `TableRuleConfiguration` and `MasterSlaveRuleConfiguration`. 
Each group of sharding tables with same rules is set with a `TableRuleConfiguration`. 
If sharding databases with sharding tables and read-write split are used together, each logic database of read-write split is set with a `MasterSlaveRuleConfiguration`. 
Each `TableRuleConfiguration` is corresponding to a `ShardingStrategyConfiguration` and it has 5 types to choose.

Read-write split uses `MasterSlaveRuleConfiguration`.

## Internal Object

The red part in the diagram indicates internal objects, which are used in Sharding-JDBC and needless to be focused by application developers. 
Sharding-JDBC uses `ShardingRuleConfiguration` and `MasterSlaveRuleConfiguration` to generate rule objects used by `ShardingDataSource` and `MasterSlaveDataSource`. 
Implemented with `DataSource` interface, `ShardingDataSource` and `MasterSlaveDataSource` are complete implementation schemes of JDBC.

## Initialization Process

1. Configure `Configuration` objects.
2. Transfer `Configuration` objects to `Rule` objects through `Factory` objects.
3. Equip `Rule` objects and `DataSource` objects through `Factory` objects.
4. Sharding-JDBC uses `DataSource` objects to split databases.

## Use Convention

Classes in `io.shardingsphere.api` and `io.shardingsphere.shardingjdbc.api` packages are API open to users, the modifications of which will be declared in release notes. 
As internal implementations, classes in other packages can adjust any time, `please not use them directly`.