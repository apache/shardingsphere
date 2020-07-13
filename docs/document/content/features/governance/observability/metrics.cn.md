+++
title = "度量指标监控"
weight = 2
+++

## 背景
`Metrics` 是一种评估系统在运行时的度量指标，通常一组度量指标可以帮助我们评估系统性能，为系统优化或业务策略给出相关的意见。
Apache ShardingSphere 旨在打造一款分布式数据库解决方案，而对于数据库来说，分析它的运行状态，连接数据，事务数，吞吐量等相关指标，
为数据库的调度，数据平滑迁移，分库分表等策略提供可视化的建议与帮助尤为重要。

## 方案

Apache ShardingSphere 遵循 `Metrics` 标准，定义了一套可插拔的 `SPI` 标准，它并不存储,收集,展现 `Metrics` 信息，
只是负责在程序中对 `Metrics` 进行埋点，目前默认的实现方案为: `Prometheus` 客户端API埋点，服务端通过 
`http` 协议来定时抓取 `Metrics` 数据。

![流程图](https://shardingsphere.apache.org/document/current/img/control-panel/metrics/metrics.png)

## 指标

目前定义了4种类型的指标

 * Counter : 计数器是一个累积度量，它表示单个单调递增的计数器，其值在重新启动时只能增加或重置为零。
 
 * Gauge : 仪表盘是一种度量标准，它表示一个可以任意上下移动的数值。

 * Histogram : 直方图对观察结果(通常是请求持续时间或响应大小等)进行采样，并按可配置的桶进行计数。它还提供了所有观测值的和。
 
 * Summary : 摘要是类似于柱状图的观察结果(通常是请求持续时间和响应大小之类的内容)。虽然它还提供了观察值的总数和所有观察值的总和，但它计算了一个滑动时间窗口上的可配置分位数。
 
 |名称                       | 类型                  |标签名称       | 说明                  |
 |:------------------------ |:--------------------- |:-------------|:-------------------- |
 |request_total             |Counter                | 无           |收集 ShardingSphere 所有的请求 |
 |sql_statement_count       |Counter                | sql_type     |收集执行的 SQL 类型,比如 (SELECT,UPDATE,INSERT...)| 
 |channel_count             |Gauge                  | 无           |收集 ShardingSphere-Proxy 的连接数               | 
 |requests_latency_histogram_millis |Histogram      | 无            |收集执行所有请求的迟延时间(单位:ms)              | 
 |sharding_datasource       |Counter                | datasource   |收集执行 SQL 语句命中的分库                       | 
 |sharding_table            |Counter                | table        |收集执行 SQL 语句命中的分表                       | 
 |transaction               |Counter                | status       |收集所有的事务数量                              | 

## 使用
在 ShardingSphere-Proxy 的 server.yaml 文件中新增以下配置:

```yaml
metrics:
   name: prometheus # 指定类型为 prometheus.
   host:  127.0.0.1 # 指定 host,如果为空，则获取默认
   port:  9190  # 指定 prometheus 服务端抓取 metrics 端口
   enable : true # 配置为 true 代表开启,设置为 false 代表关闭 ,此字段不配置时候，默认开启.
```

用户自己搭建 `Prometheus` 服务，在 prometheus.yml 文件中新增如下配置:

```yaml
scrape_configs:
  # The job name is added as a label `job=<job-name>` to any time series scraped from this config.
  - job_name: 'shardingSphere-proxy'
    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.
    static_configs:
    - targets: ['localhost:9190']
```

## 面板

推荐使用 `Granfana`，用户可以自定义查询来个性化显示面板盘，后续我们会提供默认的面板盘配置。
