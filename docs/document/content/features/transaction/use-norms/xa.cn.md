+++
title = "XA 事务"
weight = 2
+++

## 支持项

* 支持数据分片后的跨库事务；
* 两阶段提交保证操作的原子性和数据的强一致性；
* 服务宕机重启后，提交/回滚中的事务可自动恢复；
* 支持同时使用 XA 和非 XA 的连接池。

## 不支持项

* 服务宕机后，在其它机器上恢复提交/回滚中的数据。

## 通过 XA 语句控制的分布式事务
* 通过 XA START 可以手动开启 XA 事务，注意该事务完全由用户管理，ShardingSphere 只负责将语句转发至后端数据库；
* 服务宕机后，需要通过 XA RECOVER 获取未提交或回滚的事务，也可以在 COMMIT 时使用 ONE PHASE 跳过 PERPARE。

MySQL [(none)]> use test1                                                                               │MySQL [(none)]> use test2
Reading table information for completion of table and column names                                      │Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A                                          │You can turn off this feature to get a quicker startup with -A
                                                                                                        │
Database changed                                                                                        │Database changed
MySQL [test1]> XA START '61c052438d3eb';                                                                │MySQL [test2]> XA START '61c0524390927';
Query OK, 0 rows affected (0.030 sec)                                                                   │Query OK, 0 rows affected (0.009 sec)
                                                                                                        │
MySQL [test1]> update test set val = 'xatest1' where id = 1;                                            │MySQL [test2]> update test set val = 'xatest2' where id = 1;
Query OK, 1 row affected (0.077 sec)                                                                    │Query OK, 1 row affected (0.010 sec)
                                                                                                        │
MySQL [test1]> XA END '61c052438d3eb';                                                                  │MySQL [test2]> XA END '61c0524390927';
Query OK, 0 rows affected (0.006 sec)                                                                   │Query OK, 0 rows affected (0.008 sec)
                                                                                                        │
MySQL [test1]> XA PREPARE '61c052438d3eb';                                                              │MySQL [test2]> XA PREPARE '61c0524390927';
Query OK, 0 rows affected (0.018 sec)                                                                   │Query OK, 0 rows affected (0.011 sec)
                                                                                                        │
MySQL [test1]> XA COMMIT '61c052438d3eb';                                                               │MySQL [test2]> XA COMMIT '61c0524390927';
Query OK, 0 rows affected (0.011 sec)                                                                   │Query OK, 0 rows affected (0.018 sec)
                                                                                                        │
MySQL [test1]> select * from test where id = 1;                                                         │MySQL [test2]> select * from test where id = 1;
+----+---------+                                                                                        │+----+---------+
| id | val     |                                                                                        │| id | val     |
+----+---------+                                                                                        │+----+---------+
|  1 | xatest1 |                                                                                        │|  1 | xatest2 |
+----+---------+                                                                                        │+----+---------+
1 row in set (0.016 sec)                                                                                │1 row in set (0.129 sec)

MySQL [test1]> XA START '61c05243994c3';                                                                │MySQL [test2]> XA START '61c052439bd7b';
Query OK, 0 rows affected (0.047 sec)                                                                   │Query OK, 0 rows affected (0.006 sec)
                                                                                                        │
MySQL [test1]> update test set val = 'xarollback' where id = 1;                                         │MySQL [test2]> update test set val = 'xarollback' where id = 1;
Query OK, 1 row affected (0.175 sec)                                                                    │Query OK, 1 row affected (0.008 sec)
                                                                                                        │
MySQL [test1]> XA END '61c05243994c3';                                                                  │MySQL [test2]> XA END '61c052439bd7b';
Query OK, 0 rows affected (0.007 sec)                                                                   │Query OK, 0 rows affected (0.014 sec)
                                                                                                        │
MySQL [test1]> XA PREPARE '61c05243994c3';                                                              │MySQL [test2]> XA PREPARE '61c052439bd7b';
Query OK, 0 rows affected (0.013 sec)                                                                   │Query OK, 0 rows affected (0.019 sec)
                                                                                                        │
MySQL [test1]> XA ROLLBACK '61c05243994c3';                                                             │MySQL [test2]> XA ROLLBACK '61c052439bd7b';
Query OK, 0 rows affected (0.010 sec)                                                                   │Query OK, 0 rows affected (0.010 sec)
                                                                                                        │
MySQL [test1]> select * from test where id = 1;                                                         │MySQL [test2]> select * from test where id = 1;
+----+---------+                                                                                        │+----+---------+
| id | val     |                                                                                        │| id | val     |
+----+---------+                                                                                        │+----+---------+
|  1 | xatest1 |                                                                                        │|  1 | xatest2 |
+----+---------+                                                                                        │+----+---------+
1 row in set (0.009 sec)                                                                                │1 row in set (0.083 sec)

MySQL [test1]>  XA START '61c052438d3eb';
Query OK, 0 rows affected (0.030 sec)

MySQL [test1]> update test set val = 'recover' where id = 1;
Query OK, 1 row affected (0.072 sec)

MySQL [test1]> select * from test where id = 1;
+----+---------+
| id | val     |
+----+---------+
|  1 | recover |
+----+---------+
1 row in set (0.039 sec)

MySQL [test1]>  XA END '61c052438d3eb';
Query OK, 0 rows affected (0.005 sec)

MySQL [test1]> XA PREPARE '61c052438d3eb';
Query OK, 0 rows affected (0.020 sec)

MySQL [test1]> XA RECOVER;
+----------+--------------+--------------+---------------+
| formatID | gtrid_length | bqual_length | data          |
+----------+--------------+--------------+---------------+
|        1 |           13 |            0 | 61c052438d3eb |
+----------+--------------+--------------+---------------+
1 row in set (0.010 sec)

MySQL [test1]> XA RECOVER CONVERT XID;
+----------+--------------+--------------+------------------------------+
| formatID | gtrid_length | bqual_length | data                         |
+----------+--------------+--------------+------------------------------+
|        1 |           13 |            0 | 0x36316330353234333864336562 |
+----------+--------------+--------------+------------------------------+
1 row in set (0.011 sec)

MySQL [test1]> XA COMMIT 0x36316330353234333864336562;
Query OK, 0 rows affected (0.029 sec)

MySQL [test1]> XA RECOVER;
Empty set (0.011 sec)
