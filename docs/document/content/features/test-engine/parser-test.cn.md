+++
pre = "<b>3.9.2. </b>"
title = "SQL 解析测试"
weight = 2
+++

## 数据准备

SQL 解析无需真实的测试环境，开发者只需定义好待测试的 SQL，以及解析后的断言数据即可：

### SQL数据

在集成测试的部分提到过`sql-case-id`，其对应的SQL，可以在不同模块共享。开发者只需要在`shardingsphere-sql-parser/shardingsphere-sql-parser-test/src/main/resources/sql/supported/${SQL-TYPE}/*.xml` 添加待测试的 SQL 即可。

### 断言数据

断言的解析数据保存在 `shardingsphere-sql-parser/shardingsphere-sql-parser-test/src/main/resources/case/${SQL-TYPE}/*.xml`
在`xml`文件中，可以针对表名，token，SQL条件等进行断言，例如如下的配置：

```xml
<parser-result-sets>
    <parser-result sql-case-id="insert_with_multiple_values">
        <tables>
            <table name="t_order" />
        </tables>
        <tokens>
            <table-token start-index="12" table-name="t_order" length="7" />
        </tokens>
        <sharding-conditions>
            <and-condition>
                <condition column-name="order_id" table-name="t_order" operator="EQUAL">
                    <value literal="1" type="int" />
                </condition>
                <condition column-name="user_id" table-name="t_order" operator="EQUAL">
                    <value literal="1" type="int" />
                </condition>
            </and-condition>
            <and-condition>
                <condition column-name="order_id" table-name="t_order" operator="EQUAL">
                    <value literal="2" type="int" />
                </condition>
                <condition column-name="user_id" table-name="t_order" operator="EQUAL">
                    <value literal="2" type="int" />
                </condition>
            </and-condition>
        </sharding-conditions>
    </parser-result>
</parser-result-sets>
```

设置好上面两类数据，开发者就可以通过 `shardingsphere-sql-parser/shardingsphere-sql-parser-test` 下对应的测试引擎启动 SQL 解析的测试了。
