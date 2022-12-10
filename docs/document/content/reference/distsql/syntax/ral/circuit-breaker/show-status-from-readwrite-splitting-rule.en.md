+++
title = "SHOW STATUS FROM READWRITE_SPLITTING RULE"
weight = 3
+++

### Description

The `SHOW STATUS FROM READWRITE_SPLITTING RULE` syntax is used to query readwrite splitting storage unit status for specified readwrite splitting rule in specified database.

### Syntax

```sql
ShowStatusFromReadwriteSplittingRule ::=
  'SHOW' 'STATUS' 'FROM' 'READWRITE_SPLITTING' ('RULES' | 'RULE' groupName) ('FROM' databaseName)?

groupName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return Value Description

| Columns        | Description         |
|----------------|---------------------|
| resource       | storage unit name   |
| status         | storage unit status |
| delay_time(ms) | delay time          |

### Example

- Query readwrite splitting storage unit status for specified readwrite splitting rule in specified database.

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM test1;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM test1;
+----------+---------+----------------+
| resource | status  | delay_time(ms) |
+----------+---------+----------------+
| su_0     | enabled | 0              |
| su_1     | enabled | 0              |
| ds_2     | enabled | 0              |
| ds_1     | enabled | 0              |
+----------+---------+----------------+
4 rows in set (0.01 sec)
```

- Query all readwrite splitting storage unit from specified database

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULES FROM test1;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES FROM test1;
+----------+---------+----------------+
| resource | status  | delay_time(ms) |
+----------+---------+----------------+
| su_0     | enabled | 0              |
| su_1     | enabled | 0              |
| ds_2     | enabled | 0              |
| ds_1     | enabled | 0              |
+----------+---------+----------------+
4 rows in set (0.00 sec)
```

- Query readwrite splitting storage unit status for specified readwrite splitting rule in current database

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
+----------+---------+----------------+
| resource | status  | delay_time(ms) |
+----------+---------+----------------+
| su_0     | enabled | 0              |
| su_1     | enabled | 0              |
| ds_2     | enabled | 0              |
| ds_1     | enabled | 0              |
+----------+---------+----------------+
4 rows in set (0.01 sec)
```

- Query all readwrite splitting storage unit from current database

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
+----------+---------+----------------+
| resource | status  | delay_time(ms) |
+----------+---------+----------------+
| su_0     | enabled | 0              |
| su_1     | enabled | 0              |
| ds_2     | enabled | 0              |
| ds_1     | enabled | 0              |
+----------+---------+----------------+
4 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `STATUS`, `FROM`, `READWRITE_SPLITTING`, `RULE`, `RULES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
