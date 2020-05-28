+++
pre = "<b>3.4.2.3 </b>"
title = "Seata 柔性事务"
weight = 3
+++

## 功能

* 支持RC隔离级别
* 通过undo快照进行事务回滚
* 支持服务宕机后的，自动恢复提交中的事务

## 依赖

* 需要额外部署 Seata-server 服务进行分支事务的协调

## 待优化项

* Apache ShardingSphere 和 Seata 重复 SQL 解析
