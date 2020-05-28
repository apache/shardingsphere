+++
pre = "<b>3.4.2.2 </b>"
title = "两阶段事务-XA"
weight = 2
+++

## 功能

* 支持数据分片后的跨库XA事务
* 两阶段提交保证操作的原子性和数据的强一致性
* 服务宕机重启后，提交/回滚中的事务可自动恢复
* SPI 机制整合主流的XA事务管理器，默认Atomikos，可以选择使用 Narayana 和 Bitronix
* 同时支持 XA 和非 XA 的连接池
* 提供 spring boot starter 和 spring namespace 的接入端

## 不支持项

* 服务宕机后，在其它机器上恢复提交/回滚中的数据
