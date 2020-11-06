+++
pre = "<b>3.9.1. </b>"
title = "SQL测试用例"
weight = 1
+++

## 目标

SQL测试用例的代码位于 `sharding-sql-test` 模块下。该测试用例的作用主要有两个：
  
  1. 通过单元测试，测试通配符的替换以及 `SQLCasesLoader` 的稳定性。
  2. 将SQL测试用例中 `resources` 下定义的所有 SQL 共享给其他项目。

待测试的 SQL 存放在 `/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml`文件中。例如：

```xml
<sql-cases>
    <sql-case id="select_constant_without_table" value="SELECT 1 as a" />
    <sql-case id="select_with_same_table_name_and_alias" value="SELECT t_order.* FROM t_order t_order WHERE user_id = ? AND order_id = ?" />
    <sql-case id="select_with_same_table_name_and_alias_column_with_owner" value="SELECT t_order.order_id,t_order.user_id,status FROM t_order t_order WHERE t_order.user_id = ? AND order_id = ?" db-types="MySQL,H2"/>
</sql-cases>
```

开发者通过该文件指定待断言的 SQL 以及该 SQL 所适配的数据库类型。将 `sharding-sql-test` 提取为单独的模块，以保证每个 SQL 用例可以在不同模块的测试引擎中共享。

### 流程

如下图为 SQL 测试用例的数据流程：

![测试引擎](https://shardingsphere.apache.org/document/current/img/test-engine/sql-case.jpg)
