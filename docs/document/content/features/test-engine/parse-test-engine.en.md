+++
pre = "<b>3.9.3. </b>"
title = "SQL Parse Test Engine"
weight = 3
+++

## Prepare Data

Not like Integration test, SQL parse test does not need a specific database environment, just define the sql  to parse, and the assert data:

### SQL Data

As mentioned `sql-case-id` in Integration test，test-case-id could be shared in different module to test, and the file is at `/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml` 

### Parser Assert Data

The assert data is at `/sharding-core/sharding-core-parse/sharding-core-parse-test/src/test/resources/sharding/SQL-TYPE/*.xml`
in that xml file, it could assert against the table name, token or sql condition and so on. For example:

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

When these configs are ready, launch the test engine in sharding-core-parse-test to test SQL parse. 
