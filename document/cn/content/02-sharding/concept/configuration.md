+++
toc = true
title = "配置"
weight = 3
+++

## 分片规则

分片规则配置的总入口。包含数据源配置、表配置、绑定表配置以及读写分离配置等。

## 数据源配置

真实数据源列表。

## 表配置

逻辑表名称、真实表名称与分表规则的配置。

## 分片策略配置

对于分片策略存有数据源分片策略和表分片策略两种维度。

- 数据源分片策略

对应于DatabaseShardingStrategy。用于配置数据被分配的目标数据源。

- 表分片策略

对应于TableShardingStrategy。用于配置数据被分配的目标表，该目标表存在与该数据的目标数据源内。故表分片策略是依赖与数据源分片策略的结果的。

两种策略的API完全相同。

## 自增主键生成策略

通过在客户端生成自增主键替换以数据库原生自增主键的方式，做到分布式主键无重复。

## Config Map

通过ConfigMap可以配置分库分表或读写分离数据源的元数据，可通过调用ConfigMapContext.getInstance()获取ConfigMap中的shardingConfig和masterSlaveConfig数据。例：如果机器权重不同则流量可能不同，可通过ConfigMap配置机器权重元数据。
