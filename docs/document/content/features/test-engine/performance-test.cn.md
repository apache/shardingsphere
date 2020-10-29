+++
pre = "<b>3.10.5. </b>"
title = "性能测试"
weight = 5
+++

## 目标

对ShardingSphere-JDBC，ShardingSphere-Proxy及MySQL进行性能对比。从业务角度考虑，在基本应用场景（单路由，主从+加密+分库分表，全路由）下，INSERT+UPDATE+DELETE通常用作一个完整的关联操作，用于性能评估，而SELECT关注分片优化可用作性能评估的另一个操作；而主从模式下，可将INSERT+SELECT+DELETE作为一组评估性能的关联操作。
为了更好的观察效果，设计在一定数据量的基础上，使用jmeter 20并发线程持续压测半小时，进行增删改查性能测试，且每台机器部署一个MySQL实例，而对比MySQL场景为单机单实例部署。

## 测试场景

### 单路由

在1000数据量的基础上分库分表，根据`id`分为4个库，部署在同一台机器上，根据`k`分为1024个表，查询操作路由到单库单表；
作为对比，MySQL运行在1000数据量的基础上，使用INSERT+UPDATE+DELETE和单路由查询语句。

### 主从

基本主从场景，设置一主库一从库，部署在两台不同的机器上，在10000数据量的基础上，观察读写性能；
作为对比，MySQL运行在10000数据量的基础上，使用INSERT+SELECT+DELETE语句。

### 主从+加密+分库分表

在1000数据量的基础上，根据`id`分为4个库，部署在四台不同的机器上，根据`k`分为1024个表，`c`使用aes加密，`pad`使用md5加密，查询操作路由到单库单表；
作为对比，MySQL运行在1000数据量的基础上，使用INSERT+UPDATE+DELETE和单路由查询语句。

### 全路由

在1000数据量的基础上，分库分表，根据`id`分为4个库，部署在四台不同的机器上，根据`k`分为1个表，查询操作使用全路由。
作为对比，MySQL运行在1000数据量的基础上，使用INSERT+UPDATE+DELETE和全路由查询语句。

## 测试环境搭建

### 数据库表结构

此处表结构参考sysbench的sbtest表

```shell
CREATE TABLE `tbl` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `k` int(11) NOT NULL DEFAULT 0,
  `c` char(120) NOT NULL DEFAULT '',
  `pad` char(60) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
);
```

### 测试场景配置

ShardingSphere-JDBC使用与ShardingSphere-Proxy一致的配置，MySQL直连一个库用作性能对比，下面为四个场景的具体配置：

#### 单路由配置

```yaml
schemaName: sharding_db

dataSources:
  ds_0:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_1:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_2:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test 
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_3:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
    tables:
      tbl:
        actualDataNodes: ds_${0..3}.tbl${0..1023}
        tableStrategy:
          inline:
            shardingColumn: k
            algorithmExpression: tbl${k % 1024}
        keyGenerateStrategy:
            type: SNOWFLAKE
            column: id
    defaultDatabaseStrategy:
      inline:
        shardingColumn: id
        algorithmExpression: ds_${id % 4}
    defaultTableStrategy:
      none:
```

#### 主从配置

```yaml
schemaName: sharding_db

dataSources:
  primary_ds:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  replica_ds_0:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
replicaQueryRule:
  name: pr_ds
  primaryDataSourceName: primary_ds
  replicaDataSourceNames:
    - replica_ds_0
```

#### 主从+加密+分库分表配置

```yaml
schemaName: sharding_db

dataSources:
  primary_ds_0:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  replica_ds_0:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  primary_ds_1:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  replica_ds_1:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  primary_ds_2:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  replica_ds_2:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  primary_ds_3:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  replica_ds_3:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
  tables:
    tbl:
      actualDataNodes: pr_ds_${0..3}.tbl${0..1023}
      databaseStrategy:
        inline:
          shardingColumn: id
          algorithmExpression: pr_ds_${id % 4}
      tableStrategy:
        inline:
          shardingColumn: k
          algorithmExpression: tbl${k % 1024}
      keyGenerateStrategy:
        type: SNOWFLAKE
        column: id
  bindingTables:
    - tbl
  defaultDataSourceName: primary_ds_1
  defaultTableStrategy:
    none:
  replicaQueryRules:
    pr_ds_0:
      primaryDataSourceName: primary_ds_0
      replicaDataSourceNames:
        - replica_ds_0
      loadBalanceAlgorithmType: ROUND_ROBIN
    pr_ds_1:
      primaryDataSourceName: primary_ds_1
      replicaDataSourceNames:
        - replica_ds_1
      loadBalanceAlgorithmType: ROUND_ROBIN
    pr_ds_2:
      primaryDataSourceName: primary_ds_2
      replicaDataSourceNames:
        - replica_ds_2
      loadBalanceAlgorithmType: ROUND_ROBIN
    pr_ds_3:
      primaryDataSourceName: primary_ds_3
      replicaDataSourceNames:
        - replica_ds_3
      loadBalanceAlgorithmType: ROUND_ROBIN
encryptRule:
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    md5_encryptor:
      type: MD5
  tables:
    sbtest:
      columns:
        c:
          plainColumn: c_plain
          cipherColumn: c_cipher
          encryptorName: aes_encryptor
        pad:
          cipherColumn: pad_cipher
          encryptorName: md5_encryptor
```

#### 全路由

```yaml
schemaName: sharding_db

dataSources:
  ds_0:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_1:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_2:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  ds_3:
    url: jdbc:mysql://***.***.***.***:****/ds?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
  tables:
    tbl:
      actualDataNodes: ds_${0..3}.tbl1
      tableStrategy:
        inline:
          shardingColumn: k
          algorithmExpression: tbl1
      keyGenerateStrategy:
          type: SNOWFLAKE
          column: id
  defaultDatabaseStrategy:
    inline:
      shardingColumn: id
      algorithmExpression: ds_${id % 4}
  defaultTableStrategy:
    none:  
```

## 测试结果验证

### 压测语句

```shell
INSERT+UPDATE+DELETE语句：
INSERT INTO tbl(k, c, pad) VALUES(1, '###-###-###', '###-###');
UPDATE tbl SET c='####-####-####', pad='####-####' WHERE id=?;
DELETE FROM tbl WHERE id=?

全路由查询语句：
SELECT max(id) FROM tbl WHERE id%4=1

单路由查询语句：
SELECT id, k FROM tbl ignore index(`PRIMARY`) WHERE id=1 AND k=1

INSERT+SELECT+DELETE语句：
INSERT INTO tbl1(k, c, pad) VALUES(1, '###-###-###', '###-###');
SELECT count(id) FROM tbl1;
SELECT max(id) FROM tbl1 ignore index(`PRIMARY`);
DELETE FROM tbl1 WHERE id=?
```

### 压测类

参考[shardingsphere-benchmark](https://github.com/apache/shardingsphere-benchmark/tree/master/shardingsphere-benchmark)实现，注意阅读其中的注释

### 编译

```shell
git clone https://github.com/apache/shardingsphere-benchmark.git
cd shardingsphere-benchmark/shardingsphere-benchmark
mvn clean install
```

### 压测执行

```shell
cp target/shardingsphere-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar apache-jmeter-4.0/lib/ext
jmeter –n –t test_plan/test.jmx
test.jmx参考https://github.com/apache/shardingsphere-benchmark/tree/master/report/script/test_plan/test.jmx
```

### 压测结果处理

注意修改为上一步生成的result.jtl的位置。
```shell
sh shardingsphere-benchmark/report/script/gen_report.sh
```

### 历史压测数据展示

正在进行中，请等待。
<!--
[Benchmark性能平台](https://shardingsphere.apache.org/benchmark/#/overview)是数据以天粒度展示
-->