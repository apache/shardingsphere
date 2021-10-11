+++
pre = "<b>3.11.5. </b>"
title = "Performance Test(sysbench)"
weight = 5
+++

## Target

This pressure test is for the performance compare between ShardingSphere-JDBC,ShardingSphere-Proxy in Sharding Rule to MySQL,PostgreSQL

## Environment

### Software

| **Name**               | **Version** |
| ---------------------- | ---------   |
| CentOS                 | 7.3.1       |
| MySQL                  | 5.7         |
| PostgreSQL             | 10.0        |
| ShardingSphere-JDBC    | 5.0.0-RC1   |
| ShardingSphere-Proxy   | 5.0.0-RC1   |

### Hardware

| 		**Name**       | **Hardware** |    **Comment**                                             |
| -------------------  | ------------ | -------------------------------------------------------    |
| Sysbench             | 32C 64G      | the machine send request by sysbench,deployed separately   |
| ShardingSphere-Proxy | 32C 64G      | 5.0.0-RC1 版本的 ShardingSphere-Proxy,deployed separately   |
| MySQL                | 32C 64G      | MySQL, installed with PostgreSQL on the same machine       |
| PostgreSQL           | 32C 64G      | MySQL, installed with PostgreSQL on the same machine       |

## Performance Test

Prepare the config for ShardingSphere-Proxy and ShardingSphere-JDBC for testing(following configs are for MySQL)

### ShardingSphere-Proxy Sharding

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

### ShardingSphere-JDBC Sharding

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

Sysbench is a scriptable multi-threaded benchmark tool based on LuaJIT. It is most frequently used for database benchmarks
The scripts contained in sysbench, covered a lot of database test situation, it's very easy to test database performance.

| Script                   | SQL                                                      |
| -----------------------  | ------------------------------------------------------------ |
| oltp\_point\_select      | SELECT c FROM sbtest1 WHERE id=?                             |
| oltp\_read\_only         | COMMIT <br> SELECT c FROM sbtest1 WHERE id=?  |
| oltp\_write\_only        | COMMIT <br> UPDATE sbtest1 SET k=k+1 WHERE id=?  <br> UPDATE sbtest6 SET c=? WHERE id=?  <br> DELETE FROM sbtest1 WHERE id=?  <br> INSERT INTO sbtest1 (id, k, c, pad) VALUES (?, ?, ?, ?)  <br> BEGIN |
| oltp\_read\_write        | COMMIT <br> SELECT c FROM sbtest1 WHERE id=?  <br> UPDATE sbtest3 SET k=k+1 WHERE id=?  <br> UPDATE sbtest10 SET c=? WHERE id=?  <br> DELETE FROM sbtest8 WHERE id=?  <br> INSERT INTO sbtest8 (id, k, c, pad) VALUES (?, ?, ?, ?)  <br> BEGIN |
| oltp\_update\_index      | UPDATE sbtest1 SET k=k+1 WHERE id=? |
| oltp\_update\_non\_index | UPDATE sbtest1 SET c=? WHERE id=?   |
| oltp\_delete             | DELETE FROM sbtest1 WHERE id=?      |

By sysbench, test `Proxy + Database +Sharding`、`Direct to Database` and compare these result
Following is the test script for proxy by sysbench:

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

## Test Result

`point_select` as most base test case, it's very obvious to test the performance between different databases and product in ShardingSphere.Following is the QPS result for MySQL and ShardingSphere 

| Thread | MySQL   | ShardingSphere-Proxy(Sharding) | ShardingSphere-JDBC(Sharding by JMH) |
| :----- | :------ | :----------------------------- | :----------------------------------- |
| 20     | 154,408 | 50,042                         | 101,687                              |
| 100    | 283,918 | 107,488                        | 245,676                              |
| 200    | 281,902 | 110,278                        | 252,621                              |

> Sysbench is written by C language, so it could not test ShardingSphere-JDBC.Here by, the tool for testing ShardingSphere-JDBC, is a test tool from OpenJDK, called JMH
Other Test Result

### MySQL Test Result :
| MySQL     | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| --------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20  | 172,640        | 154,408           | 63,520          | 33,890          | 12,779            | 14,256                | 24,318     |
| thread100 | 308,513        | 283,918           | 107,942         | 50,664          | 18,659            | 18,350                | 29,799    |
| thread200 | 309,668        | 281,902           | 125,311         | 64,977          | 21,181            | 20,587                | 34,745    |

### ShardingSphere-Proxy + MySQL + Sharding Test Result :
| ShardingSphere-Proxy\_Sharding\_MySQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ----------------------------------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20                            | 53,953         | 50,042            | 41,929          | 36,395          | 21,700            | 23,863                | 34,000      |
| thread100                           | 117,897        | 107,488           | 104,338         | 74,393          | 38,222            | 39,742                | 93,573      |
| thread200                           | 113,608        | 110,278           | 110,829         | 84,354          | 46,583            | 45,283                | 104,681     |

### ShardingSphere-JDBC + MySQL + Sharding Test Result :  
| ShardingSphere-JDBC\_Sharding\_MySQL | oltp\_point\_select |
| ----------------------------------   | ----------------- |
| thread20                             | 101,687           |
| thread100                            | 245,676           |
| thread200                            | 252,621           |

### PostgreSQL Test Result :
| PostgreSQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ---------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20   | 198,943        | 179,174           | 3,594           | 1,504           | 669               | 1,240                 | 1,502       |
| thread100  | 364,045        | 302,767           | 3,300           | 1,469           | 704               | 1,236                 | 1,460       |
| thread200  | 347,426        | 280,177           | 3,261           | 1,575           | 688               | 1,209                 | 1,518       |

### ShardingSphere-Proxy + PostgreSQL + Sharding Test Result :
| ShardingSphere-Proxy\_Sharding\_PostgreSQL | oltp\_read\_only | oltp\_point\_select | oltp\_read\_write | oltp\_write\_only | oltp\_update\_index | oltp\_update\_non\_index | oltp\_delete |
| ---------------------------------------- | -------------- | ----------------- | --------------- | --------------- | ----------------- | --------------------- | ----------- |
| thread20                                 | 52,831         | 56,259            | 2,666           | 1,233           | 583               | 826                   | 989         |
| thread100                                | 121,476        | 126,167           | 3,187           | 1,160           | 555               | 827                   | 1,053       |
| thread200                                | 118,351        | 122,423           | 3,254           | 1,125           | 544               | 785                   | 1,016       |

### ShardingSphere-JDBC + PostgreSQL + Sharding Test Result :
| ShardingSphere-JDBC\_Sharding\_PostgreSQL | oltp\_point\_select |
| --------------------------------------- | ----------------- |
| thread20                                | 112,977           |
| thread100                               | 280,439           |
| thread200                               | 284,474           |

