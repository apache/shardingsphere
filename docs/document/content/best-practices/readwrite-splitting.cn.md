+++
pre = "<b>7.3. </b>"
title = "读写分离"
weight = 3
chapter = true
+++

## 适用场景
适用于一主多从的数据库架构，主库负责数据的写入、修改、删除等事务性操作，从库负责查询操作。
另外 Apache ShardingSphere 的读写分离功能提供了多种负载均衡策略。
## 前提条件
假设用户有一主二从的数据库架构，另外用户期望两个从库能够承担不同比重的负载。
## 数据规划
我们将采用读写分离配置，并且针对两个从库采用 `WEIGHT` 的负载均衡策略，让两个从库承担不同的负载。
## 操作步骤
1. 下载 ShardingSphere-proxy
2. 采用如配置示例所示的读写分离配置
3. 连接 proxy 后，创建 `t_order` 表
``` sql
## 可以通过 PREVIEW 语法查看真实创建表语法，可以看到路由结果都是指向了 write_ds
readwrite_splitting_db=> PREVIEW CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
 data_source_name |                                                     actual_sql
------------------+---------------------------------------------------------------------------------------------------------------------
 write_ds         | CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
(1 row)
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
``` 
4. 向 `t_order` 表中插入数据
``` sql
readwrite_splitting_db=> PREVIEW INSERT INTO t_order values (1, 1,'OK');
 data_source_name |               actual_sql
------------------+----------------------------------------
 write_ds         | INSERT INTO t_order values (1, 1,'OK')
(1 row)
INSERT INTO t_order values (1, 1,'OK')
```
5. 查询 `t_order` 表
``` sql
## 多次查询路由到不同从库
readwrite_splitting_db=> PREVIEW SELECT * FROM t_order;
 data_source_name |      actual_sql
------------------+-----------------------
 read_ds_1        | SELECT * FROM t_order
(1 row)

readwrite_splitting_db=> PREVIEW SELECT * FROM t_order;
 data_source_name |      actual_sql
------------------+-----------------------
 read_ds_0        | SELECT * FROM t_order
(1 row)
```

## 配置示例
config-encrypt.yaml
```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    readwrite_ds:
      staticStrategy:
        writeDataSourceName: write_ds
        readDataSourceNames:
          - read_ds_0
          - read_ds_1
      loadBalancerName: weight_lb
  loadBalancers:
    weight_lb:
      type: WEIGHT
      props:
        read_ds_0: 2
        read_ds_1: 1
```
## 相关参考
[YAML 配置：读写分离](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)