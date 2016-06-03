+++
date = "2016-01-08T16:14:21+08:00"
title = "压力测试报告"
weight = 10
chart = true
+++
# 压力测试报告
## 测试目的
对`Sharding-JDBC`进行性能测试，客观、公正评估系统的性能，目的有三:

1. 对比`Sharding-JDBC`和`JDBC`的性能。
1. `Sharding-JDBC`是否通过扩展数据库解决`JDBC`吞吐量不足的问题。
1. `Sharding-JDBC`的稳定性。

## 测试数据库配置

| 数据库实例  | DB1                        | DB2                        |
| -----------|:--------------------------:| :-------------------------:|
| 操作系统    | centOS5.4                  | centOS5.4                  |
| CPU        | 2C四核                      | 2C四核                     |
| 内存        | 32GB                       | 32GB                       |
| 硬盘        | 250G*2_RAID1+600G*4_RAID10 | 250G*2_RAID1+600G*4_RAID10 |
| Mysql版本   | 5.5.19                     | 5.5.19                     |
| 数据表字段数量   | 70                    | 70                  |
| 数据表行数   | 1000万                   | 1000万                   |
## 网络拓扑

![网络拓扑图](../../img/stress_test_arch.png)
## 单库情况下Sharding-JDBC与JDBC性能对比
测试结论：

- 查询操作，Sharding-JDBC的TPS为JDBC的TPS的99.8%
- 插入操作，Sharding-JDBC的TPS为JDBC的TPS的90.2%
- 更新操作，Sharding-JDBC的TPS为JDBC的TPS的93.1%

### 查询
- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="compareQuery" width="400" height="150"></canvas>

### 插入

- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="compareInsert" width="400" height="150"></canvas>

### 更新

- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="compareUpdate" width="400" height="150"></canvas>

## Sharding-JDBC单库与双库性能对比
对比测试：

- 单库用例中所有数据全部在DB1中
- 双库用例中所有数据均匀分布在DB1与DB2中

结论：

- 查询操作，TPS双库比单库可以增加大约94%的性能
- 插入操作，TPS双库比单库可以增加大约60%的性能
- 更新操作，TPS双库比单库可以增加大约89%的性能

### 查询
- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="singleAndDubbleQuery" width="400" height="150"></canvas>

### 插入
- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="singleAndDubbleInsert" width="400" height="150"></canvas>

### 更新
- 横坐标：并发用户数
- 纵坐标：TPS

<canvas id="singleAndDubbleUpdate" width="400" height="150"></canvas>

## Sharding-JDBC疲劳测试
经过8个小时的疲劳测试，jvm的堆大约占用不到600MB的内存，且使用量比较稳定。没有发生Full GC。
<canvas id="fatigueTest" width="400" height="150"></canvas>

