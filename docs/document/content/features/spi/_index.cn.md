+++
pre = "<b>3.9. </b>"
title = "可插拔架构"
weight = 9
chapter = true
+++

## 背景

在 Apache ShardingSphere 中，很多功能实现类的加载方式是通过 [SPI（Service Provider Interface）](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) 注入的方式完成的。
SPI 是一种为了被第三方实现或扩展的 API，它可以用于实现框架扩展或组件替换。

本章节汇总了 Apache ShardingSphere 所有通过 SPI 方式载入的功能模块。
如无特殊需求，用户可以使用 Apache ShardingSphere 提供的内置实现，并通过简单配置即可实现相应功能；高级用户则可以参考各个功能模块的接口进行自定义实现。
我们非常欢迎大家将您的实现类反馈至[开源社区](https://github.com/apache/shardingsphere/pulls)，让更多用户从中收益。

## 挑战

可插拔架构对程序架构设计的要求非常高，需要将各个模块相互独立，互不感知，并且通过一个可插拔内核，以叠加的方式将各种功能组合使用。
设计一套将功能开发完全隔离的架构体系，可以最大限度的将开源社区的活力激发出来。

Apache ShardingSphere 5.x 版本开始致力于可插拔架构，项目的功能组件能够灵活的以可插拔的方式进行扩展。
目前，数据分片、读写分离、多数据副本、数据加密、影子库压测等功能，以及 MySQL、PostgreSQL、SQLServer、Oracle 等 SQL 与协议的支持，均通过插件的方式织入项目。
Apache ShardingSphere 目前已提供数十个 SPI 作为系统的扩展点，仍在不断增加中。

## 目标

**让开发者能够像使用积木一样定制属于自己的独特系统，是 Apache ShardingSphere 可插拔架构的设计目标。**
