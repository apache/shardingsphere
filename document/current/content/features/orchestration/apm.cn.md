+++
pre = "<b>3.3.4. </b>"
toc = true
title = "应用性能监控"
weight = 4
+++

## 背景

`APM`是应用性能监控的缩写。目前`APM`的主要功能着眼于分布式系统的性能诊断，其主要功能包括调用链展示，应用拓扑分析等。

[Sharding-Sphere](http://shardingsphere.io)团队与[SkyWalking](http://skywalking.io)团队共同合作，推出了`Sharding-Sphere`自动探针，可以将`Sharding-Sphere`的性能数据发送到`SkyWalking`中。

## 使用方法

### 使用SkyWalking插件

请参考[SkyWalking部署手册](https://github.com/apache/incubator-skywalking/blob/5.x/docs/cn/Quick-start-CN.md)。

### 使用OpenTracing插件

如果想使用其他的APM系统，且该系统支持[OpenTracing](http://opentracing.io)。可以使用Sharding-Sphere提供的API配合该APM系统使用。

* 通过读取系统参数注入APM系统提供的Tracer实现类
```
    启动时添加参数：-Dio.shardingsphere.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
    调用初始化方法：ShardingTracer.init()                          
```

* 通过参数注入APM系统提供的Tracer实现类 
```
    shardingTracer.init(new SkywalkingTracer())   
```

*注意:使用SkyWalking的OpenTracing探针时，应将原Sharding-Sphere探针插件禁用，以防止两种插件互相冲突*


## 效果展示

### 应用架构

使用`Sharding-Proxy`访问两个数据库`192.168.0.1:3306`和`192.168.0.2:3306`，且每个数据库中有两个分表。

### 拓扑图展示

![拓扑图](http://ovfotjrsi.bkt.clouddn.com/apm/5x_topology.png)

从图中看，用户访问18次Sharding-Proxy应用，每次每个数据库访问了两次。这是由于每次访问涉及到每个库中的两个分表，所以每次访问了四张表。

### 跟踪数据展示

![跟踪图](http://ovfotjrsi.bkt.clouddn.com/apm/5x_trace.png)

从跟踪图中可以能够看到SQL解析和执行的情况。

`/Sharding-Sphere/parseSQL/` : 表示本次SQL的解析性能。

![解析节点](http://ovfotjrsi.bkt.clouddn.com/apm/5x_parse.png)

`/Sharding-Sphere/executeSQL/` : 表示具体执行的实际SQL的性能。

![实际访问节点](http://ovfotjrsi.bkt.clouddn.com/apm/5x_executeSQL.png)
