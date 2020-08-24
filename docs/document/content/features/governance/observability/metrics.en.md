+++
title = "Metrics"
weight = 2
+++

## Background

Metrics are measures of used to evaluate the performance of a system at run time. 
Quantitative assessment can be used to evaluate the performance of a system and to give relevant opinions on system optimization or business strategy.
Apache ShardingSphere aims to build a distributed database solution. 
For the database,it analyzes its running status,connection data,transaction number,throughput and other relevant indicators.
It is particularly important to provide visual advice and help for database scheduling,data smooth migration,sharding-database,sharding-table and other policies.

## Plan

Apache ShardingSphere follows the Metrics standard, which defines a pluggable SPI standard that does not store, collect, or display Metrics information,
It is only responsible for embedding Metrics in the program. Currently, the default implementation scheme is: Prometheus client API burial point, through which the service side passes
HTTP protocol to periodically grab Metrics data.

![the follow image](https://shardingsphere.apache.org/document/current/img/control-panel/metrics/metrics.png)

## Metrics indicators

Four types of metrics are currently defined.

 * Counter : A counter is a cumulative metric that represents a single monotonically increasing counter whose value can only increase or be reset to zero on restart.
 
 * Gauge : A gauge is a metric that represents a single numerical value that can arbitrarily go up and down.

 * Histogram : A histogram samples observations (usually things like request durations or response sizes) and counts them in configurable buckets. It also provides a sum of all observed values.
 
 * Summary : Similar to a histogram, a summary samples observations (usually things like request durations and response sizes). While it also provides a total count of observations and a sum of all observed values, it calculates configurable quantiles over a sliding time window.
 
 |name                      | type                  |labelName       | description                  |
 |:------------------------ |:--------------------- |:-------------|:-------------------- |
 |request_total             |Counter                |            |Collect all request of ShardingSphere-Proxy |
 |sql_statement_count       |Counter                | sql_type     |Collect all the types of SQL , example (SELECT,UPDATE,INSERT...)| 
 |channel_count             |Gauge                  |            |Collect all the connection number of ShardingSphere-Proxy               | 
 |requests_latency_histogram_millis |Histogram      |            |Collect all the request latency time(ms)           | 
 |sharding_datasource       |Counter                | datasource   |Collect all the sql sharding datasource                      | 
 |sharding_table            |Counter                | table        |Collect all the sql sharding table                       | 
 |transaction               |Counter                | status       |Collect all the sql transaction    
 
 ## Use
 
 Add the following configuration to the server.yaml file of ShardingSphere-Proxy:
 
 ```yaml
 metrics:
    name: prometheus # The specified type is Prometheus.
    host:  127.0.0.1 # Specify host and, if empty, get the default of ShardingSphere-Proxy.
    port:  9190  # Specify the Prometheus server fetching metrics port.
    enable : true # true for on and false for off ,if not config this, default value is true.
 ```
 
Users set up the Prometheus service by themselves, adding the following configuration in the prometheus.yml file:
 
 ```yaml
 scrape_configs:
   # The job name is added as a label `job=<job-name>` to any time series scraped from this config.
   - job_name: 'shardingSphere-proxy'
     # metrics_path defaults to '/metrics'
     # scheme defaults to 'http'.
     static_configs:
     - targets: ['localhost:9190']
 ```

 ## Dashboard
 
It is recommended to use Granfana. Users can customize the query to personalize the panel panel. Later we will provide the default panel panel configuration.
