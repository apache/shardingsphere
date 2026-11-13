+++
title = "Appendix with SQL operator"
weight = 3
+++

Unsupported SQL：

- RAL and RDL operations of DistSQL that are used in transactions.
- DDL support in XA transactions depends on the database dialect. openGauss permits only DDL statements that do not change metadata.
  PostgreSQL additionally permits `CREATE TABLE`, `ALTER TABLE`, `DROP TABLE` and `RENAME TABLE`, whose metadata refresh is deferred until the transaction ends (see Limitations).

Privileges required for XA transactions:

In MySQL8, you need to grant the user `XA_RECOVER_ADMIN` privileges, otherwise, the XA transaction manager will report an error when executing the `XA RECOVER` statement.
