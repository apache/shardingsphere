+++
toc = true
title = "强制分片路由"
weight = 3
+++

## 实现动机

通过解析SQL语句提取分片键列与值并进行分片是ShardingSphere对SQL零侵入的实现方式。若SQL语句中没有分片条件，则无法进行分片，需要全路由。

在一些应用场景中，分片条件并不存在于SQL，而存在于外部业务逻辑。因此需要提供一种通过外部指定分片结果的方式，在ShardingSphere中叫做Hint。

## 实现机制

ShardingSphere使用ThreadLocal管理分片键值。可以通过编程的方式向HintManager中添加分片条件，该分片条件仅在当前线程内生效。

除了通过编程的方式使用强制分片路由，ShardingSphere还计划通过SQL中的特殊注释的方式引用Hint，使开发者可以采用更加透明的方式使用该功能。

指定了强制分片路由的SQL将会无视原有的分片逻辑，直接路由至指定的真实数据节点。
