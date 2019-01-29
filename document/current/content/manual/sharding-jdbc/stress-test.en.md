+++
pre = "<b>4.1.4. </b>"
toc = true
title = "Performance Test"
weight = 4
+++

## Summary of test result

1. Performance lose: When the server resources are sufficient and the number of concurrency is the same, we compare the performance loss of JDBC and Sharding-JDBC, the result is that the performance loss of Sharding-JDBC is less than 7%, compared with JDBC.
1. Performance test: The server resources are used to the limit, the throughput of Sharding-JDBC and JDBC are on a fairly level.
1. Performance test: The server resources are used to the limit, the throughput of Sharding-JDBC with Sharding is improved nearly twice than JDBC without Sharding.
1. Performance test: The server resources are used to the limit, The performance of Sharding-JDBC V1.5.2 is more stable V1.4.2.

### Baseline performance test

| Operation                               | JDBC | Sharding-JDBC1.5.2 | Loss ratio of Sharding-JDBC1.5.2/JDBC |
|:----------------------------------------|:-----|:-------------------|:--------------------------------------|
|The SELECT for single table in single DB | 493  | 470                | 4.7%                                  |
|The UPDATE for single table in single DB | 6682 | 6303               | 5.7%                                  |
|The INSERT for single table in single DB | 6855 | 6375               | 7%                                    |

### Performance test for different DBs and Tables in JDBC and Sharding-JDBC

| Operation | Two tables in single DB for JDBC | Two tables in each of two DBs for Sharding-JDBC | Improved performance percentage |
|:----------|:---------------------------------|:------------------------------------------------|:--------------------------------|
| SELECT    | 1736                             | 3331                                            | 192%                            |
| UPDATE    | 9170                             | 17997                                           | 196%                            |
| INSERT    | 11574                            | 23043                                           | 199%                            |

| Operation | One table in single DB for JDBC | One table in each of two DBs for Sharding-JDBC | Improved performance percentage |
|:----------|:--------------------------------|:-----------------------------------------------|:--------------------------------|
| SELECT     | 1586                           | 2944                                           | 185%                            |
| UPDATE     | 9548                           | 18561                                          | 194%                            |
| INSERT     | 11182                          | 21414                                          | 192%                            |

### Sharding-JDBC v1.4.2 vs Sharding-JDBC v1.5.2

| Operation | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | Improved ratio of 1.5.2 / 1.4.2 |
|:----------|:--------------------|:--------------------|:--------------------------------|
| SELECT    | 2934                | 2944                | 100.34%                         |
| UPDATE    | 18454               | 18561               | 100.58%                         |
| INSERT    | 21045               | 21414               | 101.75%                         |

## Test purpose

- To get the performance loss comparison between Sharding-JDBC 1.5.2 and JDBC.
- To get the performance loss comparison between Sharding-jdbc 1.52 and 1.4.2.
- To check whether there are performance problems in Sharding-JDBC 1.5.2.


## Test scene

### Business scene for JDBC

| Operation                      | Operation abbreviation |
|:-------------------------------|:-----------------------|
| SELECT in one table in one DB  | JSdbStSelect           |
| INSERT in one table in one DB  | JSdbStInsert           |
| UPDATE in one table in one DB  | JSdbStUpdate           |
| SELECT in two tables in one DB | JSdbMtSelect           |
| INSERT in two tables in one DB | JSdbMtInsert           |
| UPDATE in two tables in one DB | JSdbMtUpdate           |

### Business scene for Sharding-JDBC

| Operation                               | Operation abbreviation |
|:----------------------------------------|:-----------------------|
| SELECT in one table in one DB           | SJSdbStSelect          |
| INSERT in one table in one DB           | SJSdbStInsert          |
| UPDATE in one table in one DB           | SJSdbStUpdate          |
| SELECT in two tables in one DB          | SJSdbMtSelect          |
| INSERT in two tables in one DB          | SJSdbMtInsert          |
| UPDATE in two tables in one DB          | SJSdbMtUpdate          |
| SELECT in one table in each of two DBs  | SJMdbStSelect          |
| INSERT in one table in each of two DBs  | SJMdbStInsert          |
| UPDATE in one table in each of two DBs  | SJMdbStUpdate          |
| SELECT in two tables in each of two DBs | SJMdbMtSelect          |
| INSERT in two tables in each of two DBs | SJMdbMtInsert          |
| UPDATE in two tables in each of two DBs | SJMdbMtUpdate          |

The limit test scope is the whole operations, and the baseline test scope is the following operations:

| Operation                     | The operation abbreviation |
|:------------------------------|:---------------------------|
| SELECT in one table in one DB | JSdbStSelect               |
| INSERT in one table in one DB | JSdbStInsert               |
| UPDATE in one table in one DB | JSdbStUpdate               |
| SELECT in one table in one DB | SJSdbStSelect              |
| INSERT in one table in one DB | SJSdbStInsert              |
| UPDATE in one table in one DB | SJSdbStUpdate              |

## Test method

Baseline test: the server resources are sufficient and the number of concurrency is the same.

Limit test: The server resources are used to the limit, and TPS is no longer increasing.

## Test environment

Server environment:

| DB-Name | Hardware Information                                                                                                          | Software Information | Hybrid Application |
|:--------|:------------------------------------------------------------------------------------------------------------------------------|:---------------------|:-------------------|
| DB0     | OS: CentOS 6.6 64bit<br/>CPU: 2C 4core<br/>Memory: 32G<br/>Storage: 250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card: 1000mbps | MySQL 5.7.13         | N                  |
| DB1     | OS: CentOS 6.6 64bit<br/>CPU: 2C 4core<br/>Memory: 32G<br/>Storage: 250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card: 1000mbps | MySQL 5.7.13         | N                  |

## Test procedure

## Baseline test

### SELECT in one table in one DB 

| Operation                                             | Average response time(ms) | TPS |
|:------------------------------------------------------|:--------------------------|:----|
| SELECT in one table in one DB for JDBC                | 7                         | 493 |
| SELECT in one table in one DB for Sharding-JDBC 1.5.2 | 8                         | 470 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-query-rt.png)

### UPDATE in one table in one DB

| Operation                                             | Average response time(ms) | TPS  |
|:------------------------------------------------------|:--------------------------|:-----|
| UPDATE in one table in one DB for JDBC                | 2                         | 6682 |
| UPDATE in one table in one DB for Sharding-JDBC 1.5.2 | 3                         | 6303 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-update-rt.png)

### INSERT in one table in one DB

| Operation                                             | Average response time(ms) | TPS  |
|:------------------------------------------------------|:--------------------------|:-----|
| INSERT in one table in one DB for JDBC                | 2                         | 6855 |
| INSERT in one table in one DB for Sharding-JDBC 1.5.2 | 2                         | 6375 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/b-SdbSt-insert-rt.png)

## Limit test

### SELECT in one table in one DB and The SELECT in one table in each of two DBs

| Operation                                                       | Average response time(ms) | TPS  |
|:----------------------------------------------------------------|:--------------------------|:-----|
| SELECT in one table in one DB for JDBC                          | 7                         | 1586 |
| SELECT in one table in one DB for Sharding-JDBC 1.5.2           | 7                         | 1600 |
| SELECT in two tables in each of two DBs for Sharding-JDBC 1.5.2 | 13                        | 2944 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-query-rt.png)

### SELECT in two tables in one DB and The SELECT in two tables in each of two DBs 

| Operation                                                       | Average response time(ms) | TPS  |
|:----------------------------------------------------------------|:--------------------------|:-----|
| SELECT in two tables in one DB for JDBC                         | 6                         | 1736 |
| SELECT in two tables in one DB for Sharding-JDBC 1.5.2          | 7                         | 1732 |
| SELECT in two tables in each of two DBs for Sharding-JDBC 1.5.2 | 10                        | 3331 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-query-rt.png)

### UPDATE in one table in one DB and The UPDATE in one table in each of two DBs

| Operation                                                       | Average response time(ms) | TPS   |
|:----------------------------------------------------------------|:--------------------------|:------|
| UPDATE in two tables in one DB for JDBC                         | 7                         | 9548  |
| UPDATE in one table in one DB for Sharding-JDBC 1.5.2           | 7                         | 9263  |
| UPDATE in one table in each of two DBs for Sharding-JDBC 1.5.2  | 4                         | 18561 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-update-rt.png)

### UPDATE in two tables in one DB and The UPDATE in two tables in each of two DBs

| Operation                                                           | Average response time(ms) | TPS   |
|:--------------------------------------------------------------------|:--------------------------|:------|
| The UPDATE in two tables in one DB for JDBC                         | 7                         | 9170  |
| The UPDATE in two tables in one DB for Sharding-JDBC 1.5.2          | 7                         | 8941  |
| The UPDATE in two tables in each of two DBs for Sharding-JDBC 1.5.2 | 5                         | 17997 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-update-rt.png)

### INSERT in one table in one DB and The INSERT in one table in each of two DBs

| Operation                                                      | Average response time(ms) | TPS   |
|:---------------------------------------------------------------|:--------------------------|:------|
| INSERT in one table in one DB for JDBC                         | 5                         | 11182 |
| INSERT in one table in one DB for Sharding-JDBC 1.5.2          | 5                         | 10882 |
| INSERT in one table in each of two DBs for Sharding-JDBC 1.5.2 | 4                         | 21414 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbSt-MdbSt-insert-rt.png)

### INSERT in two tables in one DB and The INSERT in two tables in each of two DBs

| Operation                                                   | Average response time(ms) | TPS   |
|:------------------------------------------------------------|:--------------------------|:------|
| INSERT in two tables in one DB for JDBC                     | 4                         | 11574 |
| INSERT in two tables in one DB for Sharding-JDBC 1.5.2      | 5                         | 10849 |
| INSERT in two tables in each of two DBs Sharding-JDBC 1.5.2 | 4                         | 23043 |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/l-SdbMt-MdbMt-insert-rt.png)

### Operations in one table in each of two DBs for Sharding-JDBC 1.4.2 and 1.5.2

| Operation | Sharding-JDBC 1.4.2 | Sharding-JDBC 1.5.2 | 1.5.2 / 1.4.2 |
|:----------|:--------------------|:--------------------|:--------------|
| SELECT    | 2934                | 2944                | 100.34%       |
| UPDATE    | 18454               | 18561               | 100.58%       |
| INSERT    | 21045               | 21414               | 101.75%       |

TPS:

![TPS](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142tps.png)

Response Time:

![RT](https://shardingsphere.apache.org/document/current/img/stress-test/152vs142rt.png)

## Appendix

There are two test servers, and two parent tables and two child tables in single database in each of servers.

### SQL of creating tables

Structure of table `order`:

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

Structure of table `ordert`:

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

### SQL Statements

JDBC INSERT

```sql
insert 
into order?(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

JDBC SELECT

```sql
select a.id,order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order? a,ordert? b where a.id=? and a.id%100=b.idm%100;
```

JDBC UPDATE

```sql
Update order? SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```

Sharding-JDBC INSERT

```sql
INSERT INTO `order`(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

Sharding-JDBC SELECT

```sql
select cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order a,order_item b where a.id=? and a.id%100=b.idm%100;
```

Sharding-JDBC UPDATE

```sql
update order SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
```
