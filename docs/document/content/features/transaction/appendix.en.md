+++
title = "Appendix with SQL operator"
weight = 3
+++

Unsupported SQL：

- RAL and RDL operations of DistSQL that are used in transactions.
- DDL statements that are used in XA transactions.

Privileges required for XA transactions:

In MySQL8, you need to grant the user `XA_RECOVER_ADMIN` privileges, otherwise, the XA transaction manager will report an error when executing the `XA RECOVER` statement.
