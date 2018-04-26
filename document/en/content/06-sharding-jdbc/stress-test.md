+++
toc = true
title = "Performance test"
weight = 4
+++

## Summary of test result

1、The test of performance lose: When the server resources are sufficient and the number of concurrency is the same, we compare the performance loss of JDBC and Sharding-JDBC, the result is that the performance loss of Sharding-JDBC is less than 7%, compared with JDBC.<br>
2、The test of performance: The server resources are used to the limit, the throughput of Sharding-JDBC and JDBC are on a fairly level.<br>
3、The test of performance: The server resources are used to the limit, the throughput of Sharding-JDBC with Sharding is improved nearly twice than JDBC without Sharding.<br>
4、The test of performance: The server resources are used to the limit, The performance of Sharding-JDBC V1.5.2 is more stable V1.4.2.<br>


### Baseline performance test

| Operation   | JDBC   | Sharding-JDBC1.5.2 |The loss ratio of Sharding-JDBC1.5.2/JDBC |
|:----------|:-------|:-------------------|:---------------------------|
|The SELECT for single table in single DB| 493     | 470                | 4.7%                       |
|The UPDATE for single table in single DB| 6682    | 6303               | 5.7%                       |
|The INSERT for single table in single DB| 6855    | 6375               | 7%                         |

### Performance test for different DBs and Tables in JDBC and Sharding-JDBC

| Operation | Two tables in single DB for JDBC | Two tables in each of two DBs for Sharding-JDBC | The improved performance percentage  |
|:--------|:------------|:----------------------|:----------|
| SELECT    | 1736        | 3331                  | 192%       |
| UPDATE    | 9170        | 17997                 | 196%       |
| INSERT    | 11574       | 23043                 | 199%       |


| Operation | One table in single DB for JDBC | One table in each of two DBs for Sharding-JDBC | The improved performance percentage |
|:--------|:------------|:----------------------|:----------|
| SELECT   | 1586        | 2944                  | 185%       |
| UPDATE   | 9548        | 18561                 | 194%       |
| INSERT   | 11182       | 21414                 | 192%       |

### Sharding-JDBC v1.4.2 vs Sharding-JDBC v1.5.2

| Operation | Sharding-JDBC 1.4.2&emsp; | Sharding-JDBC 1.5.2&emsp;&emsp;&emsp; | The improved ratio of 1.5.2/1.4.2 |
|:--------|:--------------------------|:--------------------------------------|:------------|
| SELECT   | 2934                       | 2944                                  | 100.34%     |
| UPDATE   | 18454                      | 18561                                 | 100.58%     |
| INSERT   | 21045                      | 21414                                 | 101.75%     |

## The test purpose

- To get the performance loss comparison between Sharding-JDBC 1.5.2 and JDBC.
- To get the performance loss comparison between Sharding-jdbc 1.52 and 1.4.2.
- To check whether there are performance problems in Sharding-JDBC 1.5.2.


## The test scene

### The business scene for JDBC

| Operation&emsp;&emsp;&emsp; | The operation abbreviation&emsp;&emsp;&emsp;&emsp; |
|:--------------------------|:--------------------------------|
| The SELECT in one table in one DB    | JSdbStSelect                     |
| The INSERT in one table in one DB               | JSdbStInsert                      |
| The UPDATE in one table in one DB               | JSdbStUpdate                     |
| The SELECT in two tables in one DB               | JSdbMtSelect                     |
| The INSERT in two tables in one DB                  | JSdbMtInsert                      |
| The UPDATE in two tables in one DB                  | JSdbMtUpdate                     |

### The business scene for Sharding-JDBC

| Operation&emsp;&emsp;&emsp; | The operation abbreviation&emsp;&emsp;&emsp;&emsp; |
|:--------------------------|:--------------------------------|
| The SELECT in one table in one DB                | SJSdbStSelect                   |
| The INSERT in one table in one DB                | SJSdbStInsert                    |
| The UPDATE in one table in one DB                | SJSdbStUpdate                   |
| The SELECT in two tables in one DB               | SJSdbMtSelect                   |
| The INSERT in two tables in one DB               | SJSdbMtInsert                    |
| The UPDATE in two tables in one DB               | SJSdbMtUpdate                   |
| The SELECT in one table in each of two DBs               | SJMdbStSelect                   |
| The INSERT in one table in each of two DBs               | SJMdbStInsert                    |
| The UPDATE in one table in each of two DBs               | SJMdbStUpdate                   |
| The SELECT in two tables in each of two DBs               | SJMdbMtSelect                   |
| The INSERT in two tables in each of two DBs               | SJMdbMtInsert                    |
| The UPDATE in two tables in each of two DBs             | SJMdbMtUpdate                   |

The limit test scope is the whole operations, and the baseline test scope is the following operations:

| Operation&emsp;&emsp;&emsp; | The operation abbreviation&emsp;&emsp;&emsp;&emsp; |
|:--------------------------|:--------------------------------|
| The SELECT in one table in one DB                | JSdbStSelect                    |
| The INSERT in one table in one DB                | JSdbStInsert                     |
| The UPDATE in one table in one DB                | JSdbStUpdate                    |
| The SELECT in one table in one DB                | SJSdbStSelect                   |
| The INSERT in one table in one DB                | SJSdbStInsert                    |
| The UPDATE in one table in one DB                | SJSdbStUpdate                   |

## The test method

The baseline test: the server resources are sufficient and the number of concurrency is the same.<br>
The limit test: The server resources are used to the limit, and TPS is no longer increasing.

## The test environment

The Server Configuration:

| DB-Name | Hardware Information                                                                                                      | Software Information      | Hybrid Application |
|:----|:-------------------------------------------------------------------------------------------------------------|:-------------|:--------|
| DB0 | OS：CentOS 6.6 64bit<br/>CPU：2C 4core<br/>Memory：32G<br/>Storage：250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card：1000mbps | Mysql 5.7.13 | N      |
| DB1 | OS：CentOS 6.6 64bit<br/>CPU：2C 4core<br/>Memory：32G<br/>Storage：250G\*2\_RAID1+600G\*4\_RAID10<br/>Network card：1000mbps | Mysql 5.7.13 | N      |

## The test procedure

## The baseline test

### The SELECT in one table in one DB 

| Operation                       | Average response time(ms) | TPS |
|:------------------------------|:-------------------|:-------|
| The SELECT in one table in one DB for JDBC                | 7                 | 493     |
| The SELECT in one table in one DB for Sharding-JDBC 1.5.2 | 8                 | 470     |

The display of TPS info:

![TPS](/img/b-SdbSt-query-tps.png)

The display of RT info:

![RT](/img/b-SdbSt-query-rt.png)

### The UPDATE in one table in one DB

| Operation                       | Average response time(ms) | TPS |
|:------------------------------|:------------------|:--------|
| The UPDATE in one table in one DB for JDBC                 | 2                 | 6682    |
| The UPDATE in one table in one DB for Sharding-JDBC 1.5.2 | 3                 | 6303    |

The display of TPS info:

![TPS](/img/b-SdbSt-update-tps.png)

The display of RT info:

![RT](/img/b-SdbSt-update-rt.png)

### The INSERT in one table in one DB

| Operation                       | Average response time(ms) | TPS |
|:------------------------------|:-------------------|:-------|
| The INSERT in one table in one DB for JDBC               | 2                  | 6855   |
| The INSERT in one table in one DB for Sharding-JDBC 1.5.2 | 2                  | 6375   |

The display of TPS info:

![TPS](/img/b-SdbSt-insert-tps.png)

The display of RT info:

![RT](/img/b-SdbSt-insert-rt.png)

## The limit test

### The SELECT in one table in one DB and The SELECT in one table in each of two DBs

| Operation                        | Average response time(ms) | TPS |
|:-------------------------------|:-------------------|:-------|
| The SELECT in one table in one DB for JDBC                  | 7                  | 1586   |
| Sharding-JDBC 1.5.2单库单表查询  | 7                  | 1600   |
| Sharding-JDBC 1.5.2两库各1表查询 | 13                 | 2944   |

The display of TPS info

![TPS](/img/l-SdbSt-MdbSt-query-tps.png)

The display of RT info:

![RT](/img/l-SdbSt-MdbSt-query-rt.png)

### The SELECT in two tables in one DB and The SELECT in two tables in each of two DBs 

| Operation                        | Average response time(ms) | TPS |
|:--------------------------------|:-------------------|:-----|
| The SELECT in two tables in one DB for JDBC                    | 6                  | 1736 |
| The SELECT in two tables in one DB for Sharding-JDBC 1.5.2   | 7                  | 1732 |
| The SELECT in two tables in each of two DBs for Sharding-JDBC | 10                 | 3331 |

The display of TPS info

![TPS](/img/l-SdbMt-MdbMt-query-tps.png)

The display of RT info:

![RT](/img/l-SdbMt-MdbMt-query-rt.png)

### The UPDATE in one table in one DB and The UPDATE in one table in each of two DBs

| Operation                        | Average response time(ms) | TPS  |
|:--------------------------------|:-------------------|:--------|
|  The UPDATE in two tables in one DB for JDBC                 | 7                  | 9548    |
| The UPDATE in one table in one DB for Sharding-JDBC 1.5.2     | 7                  | 9263    |
| The UPDATE in one table in each of two DBs for Sharding-JDBC 1.5.2  | 4                  | 18561   |

The display of TPS info

![TPS](/img/l-SdbSt-MdbSt-update-tps.png)

The display of RT info:

![RT](/img/l-SdbSt-MdbSt-update-rt.png)

### The UPDATE in two tables in one DB and The UPDATE in two tables in each of two DBs

| Operation                        | Average response time(ms) | TPS |
|:--------------------------------|:------------------|:--------|
| The UPDATE in two tables in one DB for JDBC                  | 7                 | 9170    |
| The UPDATE in two tables in one DB for Sharding-JDBC 1.5.2   | 7                 | 8941    |
| The UPDATE in two tables in each of two DBs for Sharding-JDBC 1.5.2 | 5                 | 17997   |

The display of TPS info

![TPS](/img/l-SdbMt-MdbMt-update-tps.png)

The display of RT info:

![RT](/img/l-SdbMt-MdbMt-update-rt.png)

### The INSERT in one table in one DB and The INSERT in one table in each of two DBs

| Operation                        | Average response time(ms) | TPS  |
|:-------------------------------|:-------------------|:-------|
| The INSERT in one table in one DB for JDBC                  | 5                 | 11182   |
| The INSERT in one table in one DB for Sharding-JDBC 1.5.2   | 5                 | 10882   |
| The INSERT in one table in each of two DBs for Sharding-JDBC 1.5.2 | 4                 | 21414   |

The display of TPS info

![TPS](/img/l-SdbSt-MdbSt-insert-tps.png)

The display of RT info:

![RT](/img/l-SdbSt-MdbSt-insert-rt.png)

### The INSERT in two tables in one DB and The INSERT in two tables in each of two DBs

| Operation                        | Average response time(ms) | TPS  |
|:--------------------------------|:------------------|:--------|
| The INSERT in two tables in one DB for JDBC                 | 4                 | 11574   |
| The INSERT in two tables in one DB for Sharding-JDBC 1.5.2    | 5                 | 10849   |
| The INSERT in two tables in each of two DBs Sharding-JDBC 1.5.2  | 4                 | 23043   |

The display of TPS info

![TPS](/img/l-SdbMt-MdbMt-insert-tps.png)

The display of RT info:

![RT](/img/l-SdbMt-MdbMt-insert-rt.png)

### The operations in one table in each of two DBs for Sharding-JDBC 1.4.2 and 1.5.2

| Operation | Sharding-JDBC 1.4.2&emsp; | Sharding-JDBC 1.5.2&emsp;&emsp;&emsp; | 1.5.2/1.4.2&emsp; |
|:--------|:--------------------------|:--------------------------------------|:------------------|
| SELECT    | 2934                       | 2944                                  | 100.34%           |
| UPDATE    | 18454                      | 18561                                 | 100.58%           |
| INSERT    | 21045                      | 21414                                 | 101.75%           |

The display of TPS info

![TPS](/img/152vs142tps.png)

The display of RT info:

![RT](/img/152vs142rt.png)

## The appendix

There are two test servers, and two parent tables and two child tables in single database in each of servers.

### The SQL of creating tables

The structure of parent table:

```sql
order_0 | CREATE TABLE `order_0` (
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
The structure of child table:

```sql
ordert_0 | CREATE TABLE `ordert_0` (
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
into order_?(order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message) VALUES (?, 0, 10, 1, 'dtest002@dangdang.com', 1, 6, 1, 0, 3, 'ttt ttt', 'beijingshijinganzhongxin', 9000, 111, 1, '100011', '51236117', ' ', ' ');
```

JDBC SELECT

```sql
select a.id,order_id,order_type,cust_id,cust_type,cust_email,payment_method_type,payment_provider_id,shipping_method_type,packing_type,preferred_shipping_time_type,receiver_name,receiver_address,receiver_country_id,receiver_province_id,receiver_city_id,receiver_zip,receiver_tel,receiver_mobile_tel,cust_message from order_? a,ordert_? b where a.id=? and a.id%100=b.idm%100;
```

JDBC UPDATE

```sql
Update order_? SET order_id=?,order_type=0,cust_id=10,cust_type=1,cust_email='dtest002@dangdang.com' where id=?;
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
