+++
pre = "<b>3.11.5. </b>"
title = "性能测试(sysbench)"
weight = 5
+++

## 目标

本文旨在测试 ShardingSphere-JDBC 及 ShardingSphere-Proxy 在分片场景下与 MySQL、PostgreSQL 的性能对比

## 环境

### 软件版本

| 名称               		     | 版本        |
| ------------------ | ------- |
| CentOS                       | 7.3.1        |
| MySQL                        | 5.7          |
| PostgreSQL                 | 10.0         |
| ShardingSphere-JDBC  | 5.0.0-RC1 |
| ShardingSphere-Proxy  | 5.0.0-RC1 |

### 硬件配置

| 		**名称**    	   | **配置**  | 				 **作用**                      |
| -----------------    | -------  | -------------------------------------------   |
| Sysbench             | 32C 64G  | 发压机，通过 sysbench 对响应数据库进行测试，单独安装 |
| ShardingSphere-Proxy | 32C 64G  | 5.0.0-RC1 版本的 ShardingSphere-Proxy，单独部署  |
| MySQL                | 32C 64G  | 要测试的 MySQL，与 PostgreSQL 安装在同一台机器     |
| PostgreSQL           | 32C 64G  | 要测试的 PostgreSQL，与 MySQL 安装在同一台机器     |

## 性能测试

准备好相关配置留作测试之用（以下配置以 MySQL 为例）

### ShardingSphere-Proxy 分片

```yaml
schemaName: sharding_db

dataSources:
  ds_0:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_1:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1

rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_item_inline
      keyGenerateStrategy:
        column: order_item_id
        keyGeneratorName: snowflake
  bindingTables:
    - t_order,t_order_item
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  defaultTableStrategy:
    none:

  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
    t_order_item_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_item_${order_id % 2}

  keyGenerators:
    snowflake:
      type: SNOWFLAKE
      props:
        worker-id: 123
```

### ShardingSphere-JDBC 分片

```yaml
mode:
  type: Standalone
  repository:
    type: File
  overwrite: true

dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !SHARDING
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item
      keyGenerateStrategy:
        column: order_item_id
        keyGeneratorName: snowflake
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_address
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  defaultTableStrategy:
    none:
  
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
    
  keyGenerators:
    snowflake:
      type: SNOWFLAKE
      props:
          worker-id: 123

props:
  sql-show: false
```

[](https://shardingsphere.apache.org/document/current/img/stress-test/sysbench_test_process.jpg)

Sysbench 是一款基于 LuaJIT 的可编写脚本的多线程基准测试工具。它最常用于数据库基准测试。
Sysbench 自带的脚本，包含了很多常见的场景，可以非常有效的对数据库的性能进行测试。

| 脚本名称                  | 执行sql                                                       |
| -----------------        | ------------------------------------------------------------ |
| oltp\_point\_select      | SELECT c FROM sbtest1 WHERE id=?                             |
| oltp\_read\_only         | COMMIT <br> SELECT c FROM sbtest1 WHERE id=?  |
| oltp\_write\_only        | COMMIT <br> UPDATE sbtest1 SET k=k+1 WHERE id=?  <br> UPDATE sbtest6 SET c=? WHERE id=?  <br> DELETE FROM sbtest1 WHERE id=?  <br> INSERT INTO sbtest1 (id, k, c, pad) VALUES (?, ?, ?, ?)  <br> BEGIN |
| oltp\_read\_write        | COMMIT <br> SELECT c FROM sbtest1 WHERE id=?  <br> UPDATE sbtest3 SET k=k+1 WHERE id=?  <br> UPDATE sbtest10 SET c=? WHERE id=?  <br> DELETE FROM sbtest8 WHERE id=?  <br> INSERT INTO sbtest8 (id, k, c, pad) VALUES (?, ?, ?, ?)  <br> BEGIN |
| oltp\_update\_index      | UPDATE sbtest1 SET k=k+1 WHERE id=? |
| oltp\_update\_non\_index | UPDATE sbtest1 SET c=? WHERE id=?   |
| oltp\_delete             | DELETE FROM sbtest1 WHERE id=?      |

通过 sysbench 分别测试 `Proxy + Database + 分片`、`直连 Database` 进行横向对比。

如下脚本为 sysbench 压测 proxy 的相应命令：

```bash
# clean and prepare the test data for sysbench. need to create a schema called sbtest before execute the following command
sysbench oltp\_read\_only --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=10 --time=3600 --threads=10 --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --rand-type=uniform --range_selects=off --auto_inc=off cleanup
sysbench oltp\_read\_only --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=10 --time=3600 --threads=10 --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --rand-type=uniform --range_selects=off --auto_inc=off prepare

# start to test by corresponding script
sysbench oltp\_read\_only --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run
sysbench oltp\_read\_only --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_read\_only.txt
sysbench oltp\_point\_select --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_point\_select.txt
sysbench oltp\_read\_write --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_read\_write.txt
sysbench oltp\_write\_only --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_write\_only.txt
sysbench oltp\_update\_index --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_update\_index.txt
sysbench oltp\_update\_non\_index --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_update\_non\_index.txt
sysbench oltp\_delete --mysql-host=${SHARDINGSPHERE_PROXY_IP} --mysql-port=3307 --mysql-user=root --mysql-password='root' --mysql-db=sbtest --tables=10 --table-size=1000000 --report-interval=5 --time=1800 --threads=${THREADS} --max-requests=0 --percentile=99 --mysql-ignore-errors="all" --range_selects=off --rand-type=uniform --auto_inc=off run | tee oltp\_delete.txt
```

## 测试结果

`point_select` 作为最基础的测试用例，这里我们以 `point_select` 为基础测试脚本，横向对比不同数据库以及 ShardingSphere 的性能。如下即为对应的数据库以及 ShardingSphere 产品的 QPS

| 线程数 | MySQL   | ShardingSphere-Proxy(分片) | ShardingSphere-JDBC(分片 by JMH) |
| :----- | :------ | :-------------------------- | :------------------------------- |
| 20     | 154,408 | 50,042                      | 101,687                          |
| 100    | 283,918 | 107,488                     | 245,676                          |
| 200    | 281,902 | 110,278                     | 252,621                          |

> Sysbench 是由 C 语言编写的，所以无法直接测试 ShardingSphere-JDBC，这里对 ShardingSphere-JDBC 的测试使用的是 OpenJDK 自带的压测工具 JMH

其他测试结果

### MySQL 的测试结果：
| MySQL     | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| --------- | --------------   | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20  | 172,640          | 154,408           | 63,520          | 33,890          | 12,779            | 14,256                | 24,318      |
| thread100 | 308,513          | 283,918           | 107,942         | 50,664          | 18,659            | 18,350                | 29,799      |
| thread200 | 309,668          | 281,902           | 125,311         | 64,977          | 21,181            | 20,587                | 34,745      |

### ShardingSphere-Proxy + MySQL + 分片的测试结果：
| ShardingSphere-Proxy\_Sharding\_MySQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ----------------------------------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20                            | 53,953         | 50,042            | 41,929          | 36,395          | 21,700            | 23,863                | 34,000      |
| thread100                           | 117,897        | 107,488           | 104,338         | 74,393          | 38,222            | 39,742                | 93,573      |
| thread200                           | 113,608        | 110,278           | 110,829         | 84,354          | 46,583            | 45,283                | 104,681     |

### ShardingSphere-JDBC + MySQL + 分片的测试结果：
| ShardingSphere-JDBC\_Sharding\_MySQL | oltp\_point\_select |
| ----------------------------------   | ----------------- |
| thread20                             | 101,687           |
| thread100                            | 245,676           |
| thread200                            | 252,621           |

### PostgreSQL 的测试结果：
| PostgreSQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ---------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread100  | 364,045        | 302,767           | 3,300           | 1,469           | 704               | 1,236                 | 1,460       |
| thread200  | 347,426        | 280,177           | 3,261           | 1,575           | 688               | 1,209                 | 1,518       |

### ShardingSphere-Proxy + PostgreSQL + 分片的测试结果：
| ShardingSphere-Proxy\_Sharding\_PostgreSQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ---------------------------------------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20                                 | 52,831         | 56,259            | 2,666           | 1,233           | 583               | 826                   | 989         |
| thread100                                | 121,476        | 126,167           | 3,187           | 1,160           | 555               | 827                   | 1,053       |
| thread200                                | 118,351        | 122,423           | 3,254           | 1,125           | 544               | 785                   | 1,016       |

### ShardingSphere-JDBC + PostgreSQL + 分片的测试结果：
| ShardingSphere-JDBC\_Sharding\_PostgreSQL | oltp\_point\_select |
| --------------------------------------- | ----------------- |
| thread20                                | 112,977           |
| thread100                               | 280,439           |
| thread200                               | 284,474           |
