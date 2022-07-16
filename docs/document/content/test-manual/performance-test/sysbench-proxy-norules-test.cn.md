+++
title = "Sysbench ShardingSphere Proxy 空 Rules 性能测试"
weight = 1
+++

## 测试目的

对 ShardingSphere-Proxy 及 MySQL 进行性能对比
1. sysbench 直接压测 MySQL 性能
2. sysbench 压测 ShardingSphere-Proxy(底层透传 MySQL)

基于以上两组实验，得到使用 ShardingSphere-Proxy 对于 MySQL 的损耗。

## 测试环境搭建

### 服务器信息

1. DB 相关配置：推荐内存大于压测的数据量，使得数据均在内存热块中，其余可自行调整；
2. ShardingSphere-Proxy 相关配置：推荐使用高性能多核 CPU，其余可自行调整；
3. 压测涉及服务器均关闭 swap 分区。

### 数据库

```shell
[mysqld]
innodb_buffer_pool_size=${MORE_THAN_DATA_SIZE}
innodb-log-file-size=3000000000
innodb-log-files-in-group=5
innodb-flush-log-at-trx-commit=0
innodb-change-buffer-max-size=40
back_log=900
innodb_max_dirty_pages_pct=75
innodb_open_files=20480
innodb_buffer_pool_instances=8
innodb_page_cleaners=8
innodb_purge_threads=2
innodb_read_io_threads=8
innodb_write_io_threads=8
table_open_cache=102400
log_timestamps=system
thread_cache_size=16384
transaction_isolation=READ-COMMITTED

# 可参考进行适当调优，旨在放大底层 DB 性能，不让实验受制于 DB 性能瓶颈。

```

### 压测工具

可通过 [ sysbench ](https://github.com/akopytov/sysbench) 官网自行获取

### ShardingSphere-Proxy

#### bin/start.sh

```shell
 -Xmx16g -Xms16g -Xmn8g  # 调整 JVM 相关参数
```

#### config.yaml

```yaml
databaseName: sharding_db

dataSources:
  ds_0:
    url: jdbc:mysql://***.***.***.***:****/test?serverTimezone=UTC&useSSL=false # 参数可适当调整
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200 # 最大链接池设为 ${压测并发数} 与压测并发数保持一致，屏蔽压测过程中额外的链接带来的影响
    minPoolSize: 200 # 最小链接池设为 ${压测并发数} 与压测并发数保持一致，屏蔽压测过程中初始化链接带来的影响

rules: []

```

## 测试阶段

### 环境准备

```shell
sysbench oltp_read_write --mysql-host=${DB_IP} --mysql-port=${DB_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  cleanup
sysbench oltp_read_write --mysql-host=${DB_IP} --mysql-port=${DB_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  prepare
```

### 压测命令

```shell
sysbench oltp_read_write --mysql-host=${DB/PROXY_IP} --mysql-port=${DB/PROXY_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  run
```

### 压测报告分析

```shell
sysbench 1.0.20 (using bundled LuaJIT 2.1.0-beta2)
Running the test with following options:
Number of threads: 200
Report intermediate results every 10 second(s)
Initializing random number generator from current time
Initializing worker threads...
Threads started!
# 每 10 秒钟报告一次测试结果，tps、每秒读、每秒写、95% 以上的响应时长统计
[ 10s ] thds: 200 tps: 11161.70 qps: 223453.06 (r/w/o: 156451.76/44658.51/22342.80) lat (ms,95%): 27.17 err/s: 0.00 reconn/s: 0.00
...
[ 120s ] thds: 200 tps: 11731.00 qps: 234638.36 (r/w/o: 164251.67/46924.69/23462.00) lat (ms,95%): 24.38 err/s: 0.00 reconn/s: 0.00
SQL statistics:
    queries performed:
        read:                            19560590                       # 读总数     
        write:                           5588740                        # 写总数
        other:                           27943700                       # 其他操作总数 (COMMIT 等)
        total:                           27943700                       # 全部总数
    transactions:                        1397185 (11638.59 per sec.)    # 总事务数 ( 每秒事务数 )
    queries:                             27943700 (232771.76 per sec.)  # 执行语句总数 ( 每秒执行语句次数 )
    ignored errors:                      0      (0.00 per sec.)         # 忽略错误数 ( 每秒忽略错误数 )
    reconnects:                          0      (0.00 per sec.)         # 重连次数 ( 每秒重连次数 )

General statistics:
    total time:                          120.0463s                      # 总共耗时
    total number of events:              1397185                        # 总共发生多少事务数

Latency (ms):
         min:                                    5.37                   # 最小延时
         avg:                                   17.13                   # 平均延时
         max:                                  109.75                   # 最大延时
         95th percentile:                       24.83                   # 超过 95% 平均耗时
         sum:                             23999546.19

Threads fairness:
    events (avg/stddev):           6985.9250/34.74                      # 平均每线程完成 6985.9250 次 event，标准差为 34.74
    execution time (avg/stddev):   119.9977/0.01                        # 每个线程平均耗时 119.9977 秒，标准差为 0.01

```

### 压测过程中值得关注的点

1. ShardingSphere-Proxy 所在服务器 CPU 利用率，充分利用 CPU 为佳；
2. DB 所在服务器磁盘 IO，物理读越低越好；
3. 压测中涉及服务器的网络 IO。
