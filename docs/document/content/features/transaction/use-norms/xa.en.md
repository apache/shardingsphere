+++
title = "XA"
weight = 2
+++

## Supported

* Support cross-database transactions after sharding;
* Operation atomicity and high data consistency in 2PC transactions;
* When service is down and restarted, commit and rollback transactions can be recovered automatically;
* Support use XA and non-XA connection pool together.

## Unsupported

* Recover committing and rolling back in other machines after the service is down.

## XA Transaction managed by XA Statement

* When using XA START to open a XA Transaction, ShardingSphere will pass it to backend database directly, you have to manage this transaction by yourself;
* When recover from a crush, you have to call XA RECOVER to check unfinished transaction, and choose to commit or rollback using xid. Or you can use ONE PHASE commit without PREPARE.

```sql
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
```