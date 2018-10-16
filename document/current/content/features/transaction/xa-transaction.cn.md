+++
pre = "<b>3.4.2. </b>"
toc = true
title = "XA事务"
weight = 2
+++

## 概念

* 完全支持跨库事务。

* 默认使用Atomikos，支持使用SPI的方式加载其他XA事务管理器。

## 支持情况

* Sharding-JDBC可以支持由用户自行配置XA数据源

* Sharding-Proxy支持
