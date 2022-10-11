+++
title = "会话管理"
weight = 5
+++

ShardingSphere 支持会话管理，可通过原生数据库的 SQL 查看当前会话或杀掉会话。目前此功能仅限于存储节点为 MySQL 的情况，支持 MySQL `SHOW PROCESSLIST` 命令和 `KILL` 命令。

## 相关操作

### 查看会话

针对不同关联数据库支持不同的查看会话方法，关联 MySQL 数据库可使用 `SHOW PROCESSLIST` 命令查看会话。ShardingSphere 会自动生成唯一的 UUID 标识作为 ID，并将 SQL 执行信息存储在各个实例中。当执行此命令时，ShardingSphere 会通过治理中心收集并同步各个计算节点的 SQL 执行信息，然后汇总返回给用户。

```
mysql> show processlist;
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
| Id                               | User | Host      | db          | Command | Time | State         | Info             |
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
| 05ede3bd584fd4a429dcaac382be2973 | root | 127.0.0.1 | sharding_db | Execute | 2    | Executing 0/1 | select sleep(10) |
| f9e5c97431567415fe10badc5fa46378 | root | 127.0.0.1 | sharding_db | Sleep   | 690  |               |                  |
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
```

- 输出说明

模拟原生 MySQL 的输出，但 `Id` 字段较为特殊为随机字符串。

### 杀掉会话

用户根据 `SHOW PROCESSLIST` 返回的结果，判断是否需要执行 `KILL` 语句，ShardingSphere 会根据 `KILL` 语句中的 ID 取消正在执行中的 SQL。

```
mysql> kill 05ede3bd584fd4a429dcaac382be2973;
Query OK, 0 rows affected (0.04 sec)

mysql> show processlist;
Empty set (0.02 sec)
```
