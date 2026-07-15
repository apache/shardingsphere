+++
title = "Metrics"
weight = 3
+++

| Name                                  | Type      | Description                                                                                            |
|:--------------------------------------|:----------|:-------------------------------------------------------------------------------------------------------|
| build_info                            | GAUGE     | Build information                                                                                      |
| parsed_sql_total                      | COUNTER   | Total count of parsed by type (INSERT, UPDATE, DELETE, SELECT, DDL, DCL, DAL, TCL, RQL, RDL, RAL, RUL) |
| routed_sql_total                      | COUNTER   | Total count of routed by database and sql type (INSERT, UPDATE, DELETE, SELECT)                        |
| routed_storage_unit_total             | COUNTER   | Total count of routed by storage unit of database                                                      |
| routed_table_total                    | COUNTER   | Total count of routed by table of database                                                             |
| jdbc_state                            | GAUGE     | Status information of ShardingSphere-JDBC. 0 is OK; 1 is CIRCUIT BREAK; 2 is LOCK                      |
| jdbc_meta_data_info                   | GAUGE     | Meta data information of ShardingSphere-JDBC                                                           |
| jdbc_statement_execute_total          | GAUGE     | Total number of statements executed                                                                    |
| jdbc_statement_execute_errors_total   | GAUGE     | Total number of statement execution errors                                                             |
| jdbc_statement_execute_latency_millis | HISTOGRAM | Statement execution latency                                                                            |
| jdbc_transactions_total               | GAUGE     | Total number of transactions, classify by commit and rollback                                          |
