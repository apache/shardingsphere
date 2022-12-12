+++
title = "SHOW MIGRATION STATUS"
weight = 9
+++

### Description

The `SHOW MIGRATION STATUS` syntax is used to query migration job status for specified migration job.

### Syntax

```sql
ShowMigrationStatus ::=
  'SHOW' 'MIGRATION' 'STATUS' migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through `SHOW MIGRATION LIST` syntax query

### Return Value Description

| column                         | Description                          |
|--------------------------------|--------------------------------------|
| item                           | migration job sharding serial number |
| data source                    | migration source                     |
| status                         | migration job status                 |
| processed_records_count        | number of processed rows             |
| inventory_finished_percentage  | finished percentage of migration job |
| incremental_idle_seconds       | incremental idle time                |
| error_message                  | error message                        |

### Example

- Query migration job status

```sql
SHOW MIGRATION STATUS 'j010180026753ef0e25d3932d94d1673ba551';
```

```sql
mysql> SHOW MIGRATION STATUS 'j010180026753ef0e25d3932d94d1673ba551';
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | su_1        | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           | 25                       |               |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `STATUS`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
