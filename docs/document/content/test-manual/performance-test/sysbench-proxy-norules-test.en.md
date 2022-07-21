+++
title = "SysBench ShardingSphere-Proxy Empty Rule Performance Test"
weight = 1
+++

## Objectives

Compare the performance of ShardingSphere-Proxy and MySQL
1. Sysbench directly carries out stress testing on the performance of MySQL.
1. Sysbench directly carries out stress testing on ShardingSphere-Proxy (directly connect MySQL). 

Based on the above two groups of experiments, we can figure out the loss of MySQL when using ShardingSphere-Proxy.

## Set up the test environment

### Server information

1. Db-related configuration: it is recommended that the memory is larger than the amount of data to be tested, so that the data is stored in the memory hot block, and the rest can be adjusted.
1. ShardingSphere-Proxy-related configuration: it is recommended to use a high-performance, multi-core CPU, and other configurations can be customized.
1. Disable swap partitions on all servers involved in the stress testing.

### Database

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

# Appropriate tuning can be considered to magnify the underlying DB performance, so that the experiment doesn't subject to DB performance bottleneck.

```

### Stress testing tool

Refer to [ sysbench's GitHub ](https://github.com/akopytov/sysbench)

### ShardingSphere-Proxy

#### bin/start.sh

```shell
 -Xmx16g -Xms16g -Xmn8g  # Adjust JVM parameters
```

#### config.yaml

```yaml
databaseName: sharding_db

dataSources:
  ds_0:
    url: jdbc:mysql://***.***.***.***:****/test?serverTimezone=UTC&useSSL=false # Parameters can be adjusted appropriately
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200 # The maximum ConnPool is set to ${the number of concurrencies in stress testing}, which is consistent with the number of concurrencies in stress testing to shield the impact of additional connections in the process of stress testing.
    minPoolSize: 200 # The minimum ConnPool is set to ${the number of concurrencies in stress testing}, which is consistent with the number of concurrencies in stress testing to shield the impact of connections initialization in the process of stress testing.

rules: []
```

## Test phase

### Environment setup

```shell
sysbench oltp_read_write --mysql-host=${DB_IP} --mysql-port=${DB_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  cleanup
sysbench oltp_read_write --mysql-host=${DB_IP} --mysql-port=${DB_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  prepare
```

### Stress testing command

```shell
sysbench oltp_read_write --mysql-host=${DB/PROXY_IP} --mysql-port=${DB/PROXY_PORT} --mysql-user=${USER} --mysql-password=${PASSWD} --mysql-db=test --tables=10 --table-size=1000000 --report-interval=10 --time=100 --threads=200  run
```

### Stress testing report analysis

```shell
sysbench 1.0.20 (using bundled LuaJIT 2.1.0-beta2)
Running the test with following options:
Number of threads: 200
Report intermediate results every 10 second(s)
Initializing random number generator from current time
Initializing worker threads...
Threads started!
# Report test results every 10 seconds, and the number of tps, reads per second, writes per second, and the total response time of more than 95th percentile.
[ 10s ] thds: 200 tps: 11161.70 qps: 223453.06 (r/w/o: 156451.76/44658.51/22342.80) lat (ms,95%): 27.17 err/s: 0.00 reconn/s: 0.00
...
[ 120s ] thds: 200 tps: 11731.00 qps: 234638.36 (r/w/o: 164251.67/46924.69/23462.00) lat (ms,95%): 24.38 err/s: 0.00 reconn/s: 0.00
SQL statistics:
    queries performed:
        read:                            19560590                       # number of reads     
        write:                           5588740                        # number of writes
        other:                           27943700                       # number of other operations (COMMIT etc.)
        total:                           27943700                       # the total number
    transactions:                        1397185 (11638.59 per sec.)    # number of transactions (per second)
    queries:                             27943700 (232771.76 per sec.)  # number of statements executed (per second)
    ignored errors:                      0      (0.00 per sec.)         # number of ignored errors (per second)
    reconnects:                          0      (0.00 per sec.)         # number of reconnections (per second)

General statistics:
    total time:                          120.0463s                      # total time
    total number of events:              1397185                        # toal number of transactions

Latency (ms):
         min:                                    5.37                   # minimum latency
         avg:                                   17.13                   # average latency
         max:                                  109.75                   # maximum latency
         95th percentile:                       24.83                   # average response time of over 95th percentile.
         sum:                             23999546.19

Threads fairness:
    events (avg/stddev):           6985.9250/34.74                      # On average, 6985.9250 events were completed per thread, and the standard deviation is 34.74
    execution time (avg/stddev):   119.9977/0.01                        # The average time of each thread is 119.9977 seconds, and the standard deviation is 0.01
```

### Noticeable features

1. CPU utilization ratio of the server where ShardingSphere-Proxy resides. It is better to make full use of CPU.
1. I/O of the server disk where the DB resides. The lower the physical read value is, the better.
1. Network IO of the server involved in the stress testing. 
