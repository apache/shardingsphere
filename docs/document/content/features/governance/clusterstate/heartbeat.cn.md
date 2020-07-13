+++
title = "心跳检测"
weight = 1
+++

## 背景

ShardingSphere 治理模块旨在提供更高效，更自动化的集群管理功能，实现此功能依赖于集群中各个节点的状态信息，而各个节点之间的实时连接状态也是实现自动化集群管理必不可少的。

心跳检测功能负责采集应用程序和数据库之间的实时连接状态，为后续自动化治理和调度提供支持。

## 方案

![设计方案](https://shardingsphere.apache.org/document/current/img/control-panel/cluster/heartbeat.png)

- 应用程序启动时根据配置初始化心跳检测模块
- 心跳检测模块启动心跳检测任务，定时获取与实例关联的数据库连接并执行心跳检测 `SQL`
- 处理心跳检测结果并保存至注册中心

## 数据结构

心跳检测结果保存在注册中心 `instances` 节点应用实例下:
```
state: ONLINE # 应用实例状态
sharding_db.ds_0: # 逻辑数据库名称.数据源名称
	state: ONLINE # 数据源状态
	lastConnect: #最后连接时间戳
sharding_db.ds_1:
	state: DISABLED
	lastConnect:	
master_slave_db.master_ds:
	state: ONLINE
	lastConnect:	
master_slave_db.slave_ds_0:
	state: ONLINE
	lastConnect:	
master_slave_db.slave_ds_1:
	state: ONLINE
	lastConnect:	
```

## 使用

### Sharding-Proxy

在 ShardingSphere-Proxy 的 `server.yaml` 文件中新增以下配置：
```
cluster:
  heartbeat:
    sql: select 1  # 心跳检测 SQL
    threadCount: 1 # 心跳检测线程池大小
    interval: 60   # 心跳检测任务间隔(s)
    retryEnable: false # 是否开启重试，开启后如果检测失败则进行重试直到达到最大重试次数
    retryMaximum: 3  # 最大重试次数，开启重试时生效
    retryInterval: 3 # 重试间隔(s)，开启重试时生效
proxy.cluster.enabled: false # 设置 true 开始心跳检测，false 关闭心跳检测
```

由于心跳检测结果需存储在注册中心，所以使用心跳检测功能需同时开启 ShardingSphere [分布式治理](/cn/features/governance/management/)功能。
