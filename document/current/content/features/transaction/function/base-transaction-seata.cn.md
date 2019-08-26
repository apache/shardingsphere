+++
pre = "<b>3.4.2.4 </b>"
toc = true
title = "柔性事务-Seata"
weight = 4
+++

## 功能

* 完全支持跨库分布式事务
* 支持RC隔离级别
* 通过undo快照进行事务回滚
* 支持服务宕机后的，自动恢复提交中的事务

## 依赖

* 需要额外部署Seata-server服务进行分支事务的协调

## 待优化项

* ShardingSphere和Seata会对SQL进行重复解析



