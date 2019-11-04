+++
pre = "<b>3.6.1. </b>"
toc = true
title = "SQL Case"
weight = 1
+++

## Configuration

After setup environment and initial data, developer need to define SQL test cases.
the SQL to be asserted in file `/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml`. For example: 

```xml
<sql-cases>
    <sql-case id="update_without_parameters" value="UPDATE t_order SET status = 'update' WHERE order_id = 1000 AND user_id = 10" />
    <sql-case id="update_with_alias" value="UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?" db-types="MySQL,H2" />
</sql-cases>
```

Developer setup the SQL for assertion and database type during on the configuration file. And these SQLs could share in different test engine, that's why we extract the `sharding-sql-test` as a stand alone module.
