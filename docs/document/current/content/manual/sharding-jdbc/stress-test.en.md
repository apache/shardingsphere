+++
pre = "<b>4.1.4. </b>"
toc = true
title = "Performance Test Report"
weight = 4
+++

## Test Result Overview

1. Performance loss test: with sufficient server resources and equal concurrency, the performance loss of Sharding-JDBC, compared to that of JDBC, is lower than 7%.
2. Performance comparison test: with the extreme use of server resources, JDBC and Sharding-JDBC have equal throughput under the same situation.
3. Performance comparison test: with the extreme use of server resources, Sharding-JDBC, after database and table sharding, can have a throughput increase of nearly twice, compared to JDBC without sharding table.
4. Performance comparison test: with the extreme use of server resources, Sharding-JDBC V1.5.2 is relatively more stable in performance compared to V1.4.2.

### Benchmark Test Performance Comparison

| Business scenario                          | JDBC | Sharding-JDBC 1.5.2 | Sharding-JDBC 1.5.2 / JDBC loss |
| :----------------------------------------- | ---- | ------------------- | ------------------------------- |
| Single database with single table `SELECT` | 493  | 470                 | 4.7%                            |
| Single database with single table `UPDATE` | 6682 | 6303                | 5.7%                            |
| Single database with single table `INSERT` | 6855 | 6375                | 7%                              |

### Comparison between Single Database Each with Double Tables of JDBC and Double Databases Each with Double Tables of Sharding-JDBC

| Business scenario | JDBC  | Sharding-JDBC | Performance Increase |
| :---------------- | ----- | ------------- | -------------------- |
| `SELECT`          | 1736  | 3331          | 192%                 |
| `UPDATE`          | 9170  | 17997         | 196%                 |
| `INSERT`          | 11574 | 23043         | 199%                 |

### Comparison between Single Database with One Table of JDBC and Double Databases Each with One Table of Sharding-JDBC

| Business scenario | JDBC  | Sharding-JDBC | Performance Increase |
| :---------------- | ----- | ------------- | -------------------- |
| `SELECT`          | 1586  | 2944          | 185%                 |
| `UPDATE`          | 9548  | 18561         | 194%                 |
| `INSERT`          | 11182 | 21414         | 192%                 |

### Comparison between Sharding-JDBC v1.4.2 and Sharding-JDBC v1.5.2

| Business scenario | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | 1.5.2 / 1.4.2 |
| :---------------- | ------------------- | ------------------- | ------------- |
| `SELECT`          | 2934                | 2944                | 100.34%       |
| `UPDATE`          | 18454               | 18561               | 100.58%       |
| `INSERT`          | 21045               | 21414               | 101.75%       |

## Test Purpose

- Compare if there is great performance loss difference between Sharding-JDBC 1.5.2 and JDBC;
- Find if there is performance loss of Sharding-JDBC 1.5.2, compared with Sharding-JDBC 1.4.2;
- Find out whether Sharding-JDBC 1.5.2 has non-functional problems, in order to provide reference for optimization;

## Test Scenario

### JDBC Business Scenario

| Business scenario                           | Scenario abbreviation |
| :------------------------------------------ | --------------------- |
| Single database with single table `SELECT`  | JSdbStSelect          |
| Single database with single table `INSERT`  | JSdbStInsert          |
| Single database with single table `UPDATE`  | JSdbStUpdate          |
| Single database with double tables `SELECT` | JSdbMtSelect          |
| Single database with double tables `INSERT` | JSdbMtInsert          |
| Single database with double tables `UPDATE` | JSdbMtUpdate          |

### Sharding-JDBC Business Scenario

| Business scenario                                 | Scenario abbreviation |
| :------------------------------------------------ | --------------------- |
| Single database with single table `SELECT`        | SJSdbStSelect         |
| Single database with single table `INSERT`        | SJSdbStInsert         |
| Single database with single table `UPDATE`        | SJSdbStUpdate         |
| Single database with double tables `SELECT`       | SJSdbMtSelect         |
| Single database with double tables `INSERT`       | SJSdbMtInsert         |
| Single database with double tables `UPDATE`       | SJSdbMtUpdate         |
| Double databases each with single table `SELECT`  | SJMdbStSelect         |
| Double databases each with single table `INSERT`  | SJMdbStInsert         |
| Double databases each with single table `UPDATE`  | SJMdbStUpdate         |
| Double databases each with double tables `SELECT` | SJMdbMtSelect         |
| Double databases each with double tables `INSERT` | SJMdbMtInsert         |
| Double databases each with double tables `UPDATE` | SJMdbMtUpdate         |

All the test scenario can be divided into the following two categories: the extreme test range includes all the scenarios and the benchmark test range is the following scenarios:

| Business scenario                          | Scenario abbreviation |
| :----------------------------------------- | --------------------- |
| Single database with single table `SELECT` | JSdbStSelect          |
| Single database with single table `INSERT` | JSdbStInsert          |
| Single database with single table `UPDATE` | JSdbStUpdate          |
| Single database with single table `SELECT` | SJSdbStSelect         |
| Single database with single table `INSERT` | SJSdbStInsert         |
| Single database with single table `UPDATE` | SJSdbStUpdate         |

## Test Method

Benchmark test: with sufficient server resources, use the same number of concurrency threads and compare them under the same business scenarios.

Extreme test: when the use of server resources has reached its limit and TPS does not increase, compare  JDBC and Sharding-JDBC of their sharding database and table.

## Test Environment Configuration

Server configuration

| Name | Hardware Configuration                                       | Software Configuration | Hybrid Application |
| :---- | ----------------------------------------------------------- | ---------------------- | :----------------- |
| DB0  | OS: CentOS 6.6 64bit<br/>CPU: 2C 4core<br/>Memory: 32G<br/>Storage: 250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card: 1000mbps | MySQL 5.7.13           | None               |
| DB1  | OS: CentOS 6.6 64bit<br/>CPU: 2C 4core<br/>Memory: 32G<br/>Storage: 250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card: 1000mbps | MySQL 5.7.13           | None               |

## Test Process Data

## Benchmark Test

### Single Database with Single Table `SELECT`

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with single table `SELECT`                   | 7                                   | 493          |
| Single database with single table `SELECT` of Sharding-JDBC 1.5.2 | 8                                   | 470          |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-rt.png)

### Single Database with Single Table `UPDATE`

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with single table `UPDATE` of JDBC           | 2                                   | 6682         |
| Single database with single table `UPDATE` of Sharding-JDBC 1.5.2 | 3                                   | 6303         |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-rt.png)

### Single Database with Single Table `INSERT`

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with single table `INSERT` of JDBC           | 2                                   | 6855         |
| Single database with single table `INSERT` of Sharding-JDBC 1.5.2 | 2                                   | 6375         |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-rt.png)

## Extreme Test

### `SELECT` of Single Database with Single Table and Double Databases Each with Double Tables

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with single table `SELECT` of JDBC           | 7                                   | 1586         |
| Single database with single table `SELECT` of Sharding-JDBC 1.5.2 | 7                                   | 1600         |
| Double databases each with double tables `SELECT` of Sharding-JDBC 1.5.2 | 13                                  | 2944         |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-rt.png)



### `SELECT` of Single Database with Double Tables and Double Databases Each with Double Tables

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with double tables `SELECT` of JDBC          | 6                                   | 1736         |
| Single database with double tables `SELECT` of Sharding-JDBC 1.5.2 | 7                                   | 1732         |
| Double databases each with double tables `SELECT` of Sharding-JDBC 1.5.2 | 10                                  | 3331         |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-rt.png)

### `UPDATE` of Single Database with Single Table and Double Databases Each with One Table

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single Database with Single Table `UPDATE` of JDBC           | 7                                   | 9548         |
| Single database with double tables `UPDATE` of Sharding-JDBC 1.5.2 | 7                                   | 9263         |
| Double databases each with one table `SELECT` of Sharding-JDBC 1.5.2 | 4                                   | 18561        |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-rt.png)

### `UPDATE` of Single Database with Double Tables and Double Databases Each with Double Tables

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single Database with Double Tables `UPDATE` of JDBC          | 7                                   | 9170         |
| Single database with double tables `UPDATE` of Sharding-JDBC 1.5.2 | 7                                   | 8941         |
| Double databases each with double tables `UPDATE` of Sharding-JDBC 1.5.2 | 5                                   | 17997        |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-rt.png)

### `INSERT` of Single Database with Single Table and Double Databases Each with One Table

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with single table `INSERT` of JDBC           | 5                                   | 11182        |
| Single database with single table `INSERT` of Sharding-JDBC 1.5.2 | 5                                   | 10882        |
| Double databases each with one table `INSERT` of Sharding-JDBC 1.5.2 | 4                                   | 21414        |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-rt.png)

### `INSERT` of Single Database with Double Tables and Double Databases Each with Double Tables

| Business scenario                                            | Average business response time (ms) | Business TPS |
| :----------------------------------------------------------- | ----------------------------------- | ------------ |
| Single database with double tables `INSERT` of JDBC          | 4                                   | 11574        |
| Single database with double tables `INSERT` of Sharding-JDBC 1.5.2 | 5                                   | 10849        |
| Double databases each with double tables `INSERT` of Sharding-JDBC 1.5.2 | 4                                   | 23043        |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-rt.png)



### Comparison of Double Databases Each with One Table between Sharding-JDBC 1.4.2 and Sharding-JDBC 1.5.2

| Business scenario | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | 1.5.2 / 1.4.2 |
| :---------------- | ------------------- | ------------------- | ------------- |
| `SELECT`          | 2934                | 2944                | 100.34%       |
| `UPDATE`          | 18454               | 18561               | 100.58%       |
| `INSERT`          | 21045               | 21414               | 101.75%       |

TPS

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142tps.png)

Response Time

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142rt.png)

## Appendix

In this test, two database servers are used, each with one database; each database has two master tables and two slave tables.

### Table Creating SQL Explanation

The master table structure is as follow:

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

The slave table structure is as follow:

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

### SQL Explanation

JDBC `INSERT`

```sql
insert 
into order?(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

JDBC `SELECT`

```sql
select a.id,order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order? a,ordert? b where a.id=? and a.id%100=b.idm%100;
```

JDBC `UPDATE`

```sql
Update order? SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```

Sharding-JDBC `INSERT`

```sql
INSERT INTO `order`(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

Sharding-JDBC `SELECT`

```sql
select cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order a,order_item b where a.id=? and a.id%100=b.idm%100;
```

Sharding-JDBC `UPDATE`

```sql
update order SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```