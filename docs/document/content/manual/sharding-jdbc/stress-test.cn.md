+++
pre = "<b>4.1.4. </b>"
toc = true
title = "性能测试报告"
weight = 4
+++

## 测试结果概述

1. 性能损耗测试：服务器资源充足、并发数相同，比较JDBC和Sharding-JDBC性能损耗，Sharding-JDBC相对JDBC损耗不超过7%。
1. 性能对比测试：服务器资源使用到极限，相同的场景JDBC与Sharding-JDBC的吞吐量相当。
1. 性能对比测试：服务器资源使用到极限，Sharding-JDBC采用分库分表后，Sharding-JDBC吞吐量较JDBC不分表有接近2倍的提升。
1. 性能对比测试：服务器资源使用到极限，Sharding-JDBC V1.5.2与V1.4.2对比，性能比较稳定。

### 基准测试性能对比

| 业务场景   | JDBC   | Sharding-JDBC1.5.2 | Sharding-JDBC1.5.2/JDBC损耗 |
|:----------|:-------|:-------------------|:---------------------------|
|单库单表查询| 493     | 470                | 4.7%                       |
|单库单表更新| 6682    | 6303               | 5.7%                       |
|单库单表插入| 6855    | 6375               | 7%                         |

### JDBC单库两表与Sharding-JDBC两库各两表对比

| 业务场景 | JDBC单库两表 | Sharding-JDBC两库各两表 | 性能提升至 |
|:--------|:------------|:----------------------|:----------|
| 查询    | 1736        | 3331                  | 192%       |
| 更新    | 9170        | 17997                 | 196%       |
| 插入    | 11574       | 23043                 | 199%       |

### JDBC单库单表与Sharding-JDBC两库各一表对比

| 业务场景 | JDBC单库单表 | Sharding-JDBC两库各一表 | 性能提升至 |
|:--------|:------------|:----------------------|:----------|
| 查询    | 1586        | 2944                  | 185%       |
| 更新    | 9548        | 18561                 | 194%       |
| 插入    | 11182       | 21414                 | 192%       |

### Sharding-JDBC v1.4.2与v1.5.2版本对比

| 业务场景 | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | 1.5.2 / 1.4.2 |
|:--------|:--------------------|:--------------------|:--------------|
| 查询    | 2934                | 2944                 | 100.34%       |
| 更新    | 18454               | 18561                | 100.58%       |
| 插入    | 21045               | 21414                | 101.75%       |

## 测试目的

- 对比Sharding-JDBC 1.5.2与JDBC性能是否有较大损耗;
- Sharding-JDBC 1.52与1.4.2版本对比，性能是否有损耗;
- Sharding-JDBC 1.5.2是否存在非功能问题，为优化提供依据;

## 测试场景

### JDBC业务场景

| 业务场景     | 场景缩写     |
|:------------|:-------------|
| 单库单表查询 | JSdbStSelect |
| 单库单表插入 | JSdbStInsert |
| 单库单表更新 | JSdbStUpdate |
| 单库两表查询 | JSdbMtSelect |
| 单库两表插入 | JSdbMtInsert |
| 单库两表更新 | JSdbMtUpdate |

### Sharding-JDBC业务场景

| 业务场景       | 场景缩写      |
|:--------------|:--------------|
| 单库单表查询   | SJSdbStSelect |
| 单库单表插入   | SJSdbStInsert |
| 单库单表更新   | SJSdbStUpdate |
| 单库两表查询   | SJSdbMtSelect |
| 单库两表插入   | SJSdbMtInsert |
| 单库两表更新   | SJSdbMtUpdate |
| 两库各一表查询 | SJMdbStSelect |
| 两库各一表插入 | SJMdbStInsert |
| 两库各一表更新 | SJMdbStUpdate |
| 两库各两表查询 | SJMdbMtSelect |
| 两库各两表插入 | SJMdbMtInsert |
| 两库各两表更新 | SJMdbMtUpdate |

所有测试场景共分为以下两大类，其中极限测试测试范围是全部场景，基准测试范围是以下场景：

| 业务场景     | 场景缩写       |
|:------------|:--------------|
| 单库单表查询 | JSdbStSelect  |
| 单库单表插入 | JSdbStInsert  |
| 单库单表更新 | JSdbStUpdate  |
| 单库单表查询 | SJSdbStSelect |
| 单库单表插入 | SJSdbStInsert |
| 单库单表更新 | SJSdbStUpdate |

## 测试方法

基准测试：服务器资源充足，使用同样的并发线程数量，对比同样的业务场景。

极限测试：服务器资源使用达到极限、TPS不再上升，对比JDBC和Sharding-JDBC分库分表。

## 测试环境配置

服务器配置

| 名称 | 硬件配置                                                                                                      | 软件配置      | 混合应用 |
|:----|:--------------------------------------------------------------------------------------------------------------|:-------------|:--------|
| DB0 | OS：CentOS 6.6 64bit<br/>处理器：2C四核<br/>内存：32G<br/>存储：250G\*2\_RAID1+600G\*4\_RAID10<br/>网卡：1000mbps | MySQL 5.7.13 | 否      |
| DB1 | OS：CentOS 6.6 64bit<br/>处理器：2C四核<br/>内存：32G<br/>存储：250G\*2\_RAID1+600G\*4\_RAID10<br/>网卡：1000mbps | MySQL 5.7.13 | 否      |

## 测试过程数据

## 基准测试

### 单库单表查询

| 业务场景                       | 业务平均响应时间(ms) | 业务TPS |
|:------------------------------|:-------------------|:-------|
| JDBC单库单表查询                | 7                 | 493     |
| Sharding-JDBC 1.5.2单库单表查询 | 8                 | 470     |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-rt.png)

### 单库单表更新

| 业务场景                       | 业务平均响应时间(ms) | 业务TPS |
|:------------------------------|:------------------|:--------|
| JDBC单库单表更新                | 2                 | 6682    |
| Sharding-JDBC 1.5.2单库单表更新 | 3                 | 6303    |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-rt.png)

### 单库单表插入

| 业务场景                       | 业务平均响应时间(ms) | 业务TPS |
|:------------------------------|:-------------------|:-------|
| JDBC单库单表插入                | 2                  | 6855   |
| Sharding-JDBC 1.5.2单库单表插入 | 2                  | 6375   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-rt.png)

## 极限测试

### 单库单表与两库各一表查询

| 业务场景                        | 业务平均响应时间(ms) | 业务TPS |
|:-------------------------------|:-------------------|:-------|
| JDBC单库单表查询                 | 7                  | 1586   |
| Sharding-JDBC 1.5.2单库单表查询  | 7                  | 1600   |
| Sharding-JDBC 1.5.2两库各1表查询 | 13                 | 2944   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-rt.png)

### 单库两表与两库各两表查询

| 业务场景                         | 业务平均响应时间(ms) |业务TPS|
|:--------------------------------|:-------------------|:-----|
| JDBC单库两表查询                  | 6                  | 1736 |
| Sharding-JDBC 1.5.2单库两表查询   | 7                  | 1732 |
| Sharding-JDBC 1.5.2两库各两表查询 | 10                 | 3331 |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-rt.png)

### 单库单表更新与两库各一表更新

| 业务场景                         | 业务平均响应时间（ms）| 业务TPS |
|:--------------------------------|:-------------------|:--------|
| JDBC单库单表更新                  | 7                  | 9548    |
| Sharding-JDBC 1.5.2单库单更新     | 7                  | 9263    |
| Sharding-JDBC 1.5.2两库各1表更新  | 4                  | 18561   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-rt.png)

### 单库两表与两库各2表更新

| 业务场景                         | 业务平均响应时间(ms) | 业务TPS |
|:--------------------------------|:------------------|:--------|
| JDBC单库两表更新                  | 7                 | 9170    |
| Sharding-JDBC 1.5.2单库两表更新   | 7                 | 8941    |
| Sharding-JDBC 1.5.2两库各两表更新 | 5                 | 17997   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-rt.png)

### 单库单表插入与两库各一表插入

| 业务场景                        | 业务平均响应时间(ms) | 业务TPS |
|:-------------------------------|:-------------------|:-------|
| JDBC单库单表插入                 | 5                 | 11182   |
| Sharding-JDBC 1.5.2单库单表插入  | 5                 | 10882   |
| Sharding-JDBC 1.5.2两库各1表插入 | 4                 | 21414   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-rt.png)

### 单库两表与两库各2表插入

| 业务场景                         | 业务平均响应时间(ms) | 业务TPS |
|:--------------------------------|:------------------|:--------|
| JDBC单库两表插入                  | 4                 | 11574   |
| Sharding-JDBC 1.5.2单库两表插入   | 5                 | 10849   |
| Sharding-JDBC 1.5.2两库各两表插入 | 4                 | 23043   |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-rt.png)

### Sharding-JDBC 1.4.2与1.5.2两库各一表对比

| 业务场景 | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | 1.5.2 / 1.4.2 |
|:--------|:--------------------|:--------------------|:--------------|
| 查询    | 2934                | 2944                | 100.34%       |
| 更新    | 18454               | 18561               | 100.58%       |
| 插入    | 21045               | 21414               | 101.75%       |

TPS展示

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142tps.png)

RT展示

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142rt.png)

## 附录

本次测试，共使用两台数据库服务器，每台服务器上分别有1库，每个库中分别有2个主表，2个子表。

### 建表语句说明

主表结构如下：

```sql
CREATE TABLE `order0` (
  `id` bigint(50) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(50) NOT NULL,
  `order_type` int(11) DEFAULT NULL,
  `cust_id` int(11) DEFAULT NULL,
  `cust_type` int(11) DEFAULT NULL,
  `cust_email` varchar(50) DEFAULT NULL,
  `payment_method_type` int(11) DEFAULT NULL,
  `payment_provider_id` int(11) DEFAULT NULL,
  `shipping_method_type` int(11) DEFAULT NULL,
  `packing_type` int(11) DEFAULT NULL,
  `preferred_shipping_time_type` int(11) DEFAULT NULL,
  `receiver_name` varchar(100) DEFAULT NULL,
  `receiver_address` varchar(200) DEFAULT NULL,
  `receiver_country_id` int(11) DEFAULT NULL,
  `receiver_province_id` int(11) DEFAULT NULL,
  `receiver_city_id` int(11) DEFAULT NULL,
  `receiver_zip` varchar(20) DEFAULT NULL,
  `receiver_tel` varchar(50) DEFAULT NULL,
  `receiver_mobile_tel` varchar(50) DEFAULT NULL,
  `cust_message` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_id` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5189307 DEFAULT CHARSET=gbk 
```
子表结构如下：

```sql
CREATE TABLE `ordert0` (
  `idm` bigint(50) NOT NULL,
  `id` int(10) DEFAULT NULL,
  `order_idm` varchar(50) DEFAULT NULL,
  `order_typem` int(11) DEFAULT NULL,
  `cust_idm` int(11) DEFAULT NULL,
  `cust_typem` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=gbk ROW_FORMAT=DYNAMIC
```

### SQL语句说明

JDBC 插入

```sql
insert 
into order?(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

JDBC 查询

```sql
select a.id,order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order? a,ordert? b where a.id=? and a.id%100=b.idm%100;
```

JDBC 更新

```sql
Update order? SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```

Sharding-JDBC 插入

```sql
INSERT INTO `order`(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

Sharding-JDBC 查询

```sql
select cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order a,order_item b where a.id=? and a.id%100=b.idm%100;
```

Sharding-JDBC 更新

```sql
update order SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```
