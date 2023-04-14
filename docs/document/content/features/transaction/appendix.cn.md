+++
title = "附录"
weight = 3
+++

不支持的 SQL：

- 事务中使用 DistSQL 里的 RAL、RDL 操作；
- XA 事务中使用 DDL 语句。

XA 事务所需的权限：

在 MySQL8 中需要授予用户 `XA_RECOVER_ADMIN` 权限，否则 XA 事务管理器执行 `XA RECOVER` 语句时会报错。
