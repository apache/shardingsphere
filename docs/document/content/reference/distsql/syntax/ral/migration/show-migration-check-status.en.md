+++
title = "SHOW MIGRATION CHECK STATUS"
weight = 12
+++

### Description

The `SHOW MIGRATION CHECK STATUS` syntax is used to query migration check status for specified migration job.

### Syntax

```sql
ShowMigrationCheckStatus ::=
  'SHOW' 'MIGRATION' 'CHECK' 'STATUS' migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

### Return Value Description

| Columns               | Description               |
|-----------------------|---------------------------|
| tables                | migration check table     |
| result                | check result              |
| finished_percentage   | check finished percentag  |
| remaining_seconds     | check remaining time      |
| check_begin_time      | check begin time          |
| check_end_time        | check end time            |
| error_message         | error message             |

### Example

- Query migration check status

```sql
SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
```

```sql
mysql> SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| t_order | true   | 100                 | 0                 | 2022-11-01 17:57:39.940 | 2022-11-01 17:57:40.587 | 0                |               |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `CHECK`, `STATUS`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
