+++
title = "Session Management"
weight = 7
+++

ShardingSphere supports session management. You can view the current session or kill the session through the SQL of the native database. At present, this function is only available when the storage node is MySQL. MySQL `SHOW PROCESSLIST` and `KILL` commands are supported.

## Usage
### View Session

Different methods of viewing sessions are supported for different associated databases. The `SHOW PROCESSLIST` command can be used to view sessions for associated MySQL databases. ShardingSphere will automatically generate a unique UUID ID as the ID, and store the SQL execution information in each instance. When this command is executed, ShardingSphere will collect and synchronize the SQL execution information of each computing node through the governance center, and then summarize and return it to the user.

```sql
mysql> show processlist;
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
| Id                               | User | Host      | db          | Command | Time | State         | Info             |
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
| 05ede3bd584fd4a429dcaac382be2973 | root | 127.0.0.1 | sharding_db | Execute | 2    | Executing 0/1 | select sleep(10) |
| f9e5c97431567415fe10badc5fa46378 | root | 127.0.0.1 | sharding_db | Sleep   | 690  |               |                  |
+----------------------------------+------+-----------+-------------+---------+------+---------------+------------------+
```

- Output Description

Simulates the output of native MySQL, but the `Id` field is a special random string.

### Kill Session

The user determines whether the `KILL` statement needs to be executed according to the results returned by `SHOW PROCESSLIST`. ShardingSphere cancels the SQL being executed according to the ID in the `KILL` statement.

```sql
mysql> kill 05ede3bd584fd4a429dcaac382be2973;
Query OK, 0 rows affected (0.04 sec)

mysql> show processlist;
Empty set (0.02 sec)
```
