+++
toc = true
title = "配置域模型"
weight = 4
prev = "/02-guide"
next = "/02-guide/sharding/"

+++

本文介绍Sharding-JDBC和配置相关的领域模型。以下是Sharding-JDBC与配置相关的领域模型类图。

![配置领域模型类图](http://ovfotjrsi.bkt.clouddn.com/docs/img/config_domain.png)

## 工厂方法API

图中黄色部分表示的是Sharding-JDBC的入口API，采用工厂方法的形式提供。
目前有ShardingDataSourceFactory和MasterSlaveDataSourceFactory两个工厂类。ShardingDataSourceFactory用于创建分库分表或分库分表+读写分离的JDBC驱动，MasterSlaveDataSourceFactory用于创建独立使用读写分离的JDBC驱动。

## 配置对象

图中蓝色部分表示的是Sharding-JDBC的配置对象，提供灵活多变的配置方式。
ShardingRuleConfiguration是分库分表配置的核心和入口，它可以包含多个TableRuleConfiguration和MasterSlaveRuleConfiguration。每一组相同规则分片的表配置一个TableRuleConfiguration。如果需要分库分表和读写分离共同使用，每一个读写分离的逻辑库配置一个MasterSlaveRuleConfiguration。
每个TableRuleConfiguration对应一个ShardingStrategyConfiguration，它有5中实现类可供选择。关于分片策略使用的细节，请阅读[分库分表](/02-guide/sharding/)。

仅读写分离使用MasterSlaveRuleConfiguration即可。

## 内部对象

图中红色部分表示的是内部对象，由Sharding-JDBC内部使用，应用开发者无需关注。Sharding-JDBC通过ShardingRuleConfiguration和MasterSlaveRuleConfiguration生成真正供ShardingDataSource和MasterSlaveDataSource使用的规则对象。ShardingDataSource和MasterSlaveDataSource实现了DataSource接口，是JDBC的完整实现方案。

## 初始化流程

1. 配置Configuration对象。
2. 通过Factory对象将Configuration对象转化为Rule对象。
3. 通过Factory对象将Rule对象与DataSource对象装配。
4. Sharding-JDBC使用DataSource对象进行分库分表和读写分离。
