+++
toc = true
title = "最大努力送达"
weight = 1
+++

## 概念

在分布式数据库的场景下，相信对于该数据库的操作最终一定可以成功，所以通过最大努力反复尝试送达操作。

![最大努力送达型事务](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture-soft-transaction-bed.png)

## 适用场景

* 根据主键删除数据。
* 更新记录永久状态，如更新通知送达状态。

## 使用规范

使用最大努力送达型柔性事务的SQL需要满足幂等性。

* INSERT语句要求必须包含主键，且不能是自增主键。
* UPDATE语句要求幂等，不能是UPDATE xxx SET x=x+1
* DELETE语句无要求。

## 开发指南

* sharding-jdbc-transaction完全基于java开发，直接提供jar包，可直接使用maven导入坐标即可使用。
* 为了保证事务不丢失，sharding-jdbc-transaction需要提供数据库存储事务日志，配置方法可参见事务管理器配置项。
* 由于柔性事务采用异步尝试，需要部署独立的作业和Zookeeper。sharding-jdbc-transaction采用elastic-job实现的sharding-jdbc-transaction-async-job，通过简单配置即可启动高可用作业异步送达柔性事务，启动脚本为start.sh。
* 为了便于开发，sharding-jdbc-transaction提供了基于内存的事务日志存储器和内嵌异步作业。

## 独立部署作业指南

* 部署用于存储事务日志的数据库。
* 部署用于异步作业使用的Zookeeper。
* 配置YAML文件,参照示例。
* 下载并解压文件sharding-jdbc-transaction-async-job-$VERSION.tar，通过start.sh脚本启动异步作业。
