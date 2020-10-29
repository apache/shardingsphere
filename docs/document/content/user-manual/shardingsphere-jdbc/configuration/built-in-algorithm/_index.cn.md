+++
title = "内置算法"
weight = 5
chapter = true
+++

## 简介

Apache ShardingSphere 通过 SPI 方式允许开发者扩展算法；
与此同时，Apache ShardingSphere 也提供了大量的内置算法以便于开发者使用。

## 使用方式

内置算法均通过 type 和 props 进行配置，其中 type 由算法定义在 SPI 中，props 用于传递算法的个性化参数配置。

无论使用哪种配置方式，均是将配置完毕的算法命名，并传递至相应的规则配置中。
本章节根据功能区分并罗列 Apache ShardingSphere 全部的内置算法，供开发者参考。
