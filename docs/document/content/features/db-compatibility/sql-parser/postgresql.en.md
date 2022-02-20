+++
title = "PostgreSQL"
weight = 2
+++

The unsupported SQL list for PostgreSQL are as follows:

| SQL                                                                                                       |
| --------------------------------------------------------------------------------------------------------- |
| CREATE type avg_state AS (total bigint, count bigint);                                                    |
| CREATE AGGREGATE my_avg(int4) (stype = avg_state, sfunc = avg_transfn, finalfunc = avg_finalfn)           |
| CREATE TABLE agg_data_2k AS SELECT g FROM generate_series(0, 1999) g;                                     |
| CREATE SCHEMA alt_nsp1;                                                                                   |
| ALTER AGGREGATE alt_agg3(int) OWNER TO regress_alter_generic_user2;                                       |
| CREATE CONVERSION alt_conv1 FOR 'LATIN1' TO 'UTF8' FROM iso8859_1_to_utf8;                                |
| CREATE FOREIGN DATA WRAPPER alt_fdw1                                                                      |
| CREATE SERVER alt_fserv1 FOREIGN DATA WRAPPER alt_fdw1                                                    |
| CREATE LANGUAGE alt_lang1 HANDLER plpgsql_call_handler                                                    |
| CREATE STATISTICS alt_stat1 ON a, b FROM alt_regress_1                                                    |
| CREATE TEXT SEARCH DICTIONARY alt_ts_dict1 (template=simple)                                              |
| CREATE RULE def_view_test_ins AS ON INSERT TO def_view_test DO INSTEAD INSERT INTO def_test SELECT new.*  |
| ALTER TABLE alterlock SET (toast.autovacuum_enabled = off)                                                |
| CREATE PUBLICATION pub1 FOR TABLE alter1.t1, ALL TABLES IN SCHEMA alter2                                  |

