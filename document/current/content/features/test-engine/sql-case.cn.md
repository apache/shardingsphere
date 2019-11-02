+++
pre = "<b>3.6.1. </b>"
toc = true
title = "SQL测试用例"
weight = 1
+++

## 配置

设置好集成测试的相关环境以及初始化的数据之后，接下来开发者需要定义待测试的 SQL。
待测试的 SQL 存放在 `/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml`文件中。例如：

```xml
<sql-cases>
    <sql-case id="update_without_parameters" value="UPDATE t_order SET status = 'update' WHERE order_id = 1000 AND user_id = 10" />
    <sql-case id="update_with_alias" value="UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?" db-types="MySQL,H2" />
</sql-cases>
```

开发者通过该文件指定待断言的 SQL 以及该 SQL 所适配的数据库类型。将 `sharding-sql-test` 提取为单独的模块，以保证每个 SQL 用例可以在不同模块的测试引擎中共享。
