+++
pre = "<b>3.9.3. </b>"
title = "SQL解析测试引擎"
weight = 3
+++

## 数据准备

SQL解析不需要真实的测试环境，开发者只需定义好待测试的SQL，以及解析后的断言数据即可：

### SQL数据

在集成测试的部分提到过`sql-case-id`，其对应的SQL，可以在不同模块共享。开发者只需要在`/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml` 添加待测试的SQL即可。

### 断言解析数据

断言的解析数据保存在 `/sharding-core/sharding-core-parse/sharding-core-parse-test/src/test/resources/sharding/SQL-TYPE/*.xml`
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

设置好上面两类数据，开发者就可以通过 `sharding-core-parse-test` 下对应的engine启动SQL解析的测试了。
