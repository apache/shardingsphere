+++
title = "Sharding"
weight = 1
+++

## Background

The configuration method of data sharding Spring Namespace is applicable to traditional Spring projects. The sharding rules and attributes are configured through the namespace xml configuration file. Spring completes the creation and management of ShardingSphereDataSource objects to avoid additional coding work.

## Parameters

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.1.2.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.1.2.xsd)

\<sharding:rule />

| *Name*                                | *Type*    | *Description*                               |
| ------------------------------------- | --------- | ------------------------------------------- |
| id                                    | Attribute | Spring Bean Id                              |
| table-rules (?)                       | Tag       | Sharding table rule configuration           |
| auto-table-rules (?)                  | Tag       | Automatic sharding table rule configuration |
| binding-table-rules (?)               | Tag       | Binding table rule configuration            |
| broadcast-table-rules (?)             | Tag       | Broadcast table rule configuration          |
| default-database-strategy-ref (?)     | Attribute | Default database strategy name              |
| default-table-strategy-ref (?)        | Attribute | Default table strategy name                 |
| default-key-generate-strategy-ref (?) | Attribute | Default key generate strategy name          |
| default-sharding-column (?)           | Attribute | Default sharding column name                |

\<sharding:table-rule />

| *Name*                    | *Type*    | *Description*              |
| ------------------------- | --------- | -------------------------- |
| logic-table               | Attribute | Logic table name           |
| actual-data-nodes         | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. |
| actual-data-sources       | Attribute | Data source names for auto sharding table |
| database-strategy-ref     | Attribute | Database strategy name for standard sharding table     |
| table-strategy-ref        | Attribute | Table strategy name for standard sharding table        |
| sharding-strategy-ref     | Attribute | sharding strategy name for auto sharding table         |
| key-generate-strategy-ref | Attribute | Key generate strategy name |

\<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

\<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Binding table name, multiple tables separated with comma |

\<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*                      |
| ------------------------ | ------ | ---------------------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rule configuration |

\<sharding:broadcast-table-rule />

| *Name* | *Type*    | *Description*        |
| ------ | --------- | -------------------- |
| table  | Attribute | Broadcast table name |

\<sharding:standard-strategy />

| *Name*          | *Type*    | *Description*                   |
| --------------- | --------- | ------------------------------- |
| id              | Attribute | Standard sharding strategy name |
| sharding-column | Attribute | Sharding column name            |
| algorithm-ref   | Attribute | Sharding algorithm name         |

\<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Complex sharding strategy name                               |
| sharding-columns | Attribute | Sharding column names, multiple columns separated with comma |
| algorithm-ref    | Attribute | Sharding algorithm name                                      |

\<sharding:hint-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Hint sharding strategy name |
| algorithm-ref | Attribute | Sharding algorithm name     |

\<sharding:none-strategy />

| *Name* | *Type*    | *Description*          |
| ------ | --------- | ---------------------- |
| id     | Attribute | Sharding strategy name |

\<sharding:key-generate-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Key generate strategy name  |
| column        | Attribute | Key generate column name    |
| algorithm-ref | Attribute | Key generate algorithm name |

\<sharding:sharding-algorithm />

| *Name*    | *Type*    | *Description*                 |
| --------- | --------- | ----------------------------- |
| id        | Attribute | Sharding algorithm name       |
| type      | Attribute | Sharding algorithm type       |
| props (?) | Tag       | Sharding algorithm properties |

\<sharding:key-generate-algorithm />

| *Name*    | *Type*    | *Description*                     |
| --------- | --------- | --------------------------------- |
| id        | Attribute | Key generate algorithm name       |
| type      | Attribute | Key generate algorithm type       |
| props (?) | Tag       | Key generate algorithm properties |

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/common-config/builtin-algorithm/sharding) and [Built-in Key Generate Algorithm List](/en/user-manual/common-config/builtin-algorithm/keygen) for more details about type of algorithm.

> Attention: Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Procedure

1. Configure data sharding rules in the Spring Namespace configuration file, including data source, sharding rules, global attributes and other configuration items.
2. Start the Spring program, the configuration will be loaded automatically, and the ShardingSphereDataSource will be initialized.

## Sample

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.springframework.org/schema/tx 
                           http://www.springframework.org/schema/tx/spring-tx.xsd
                           http://www.springframework.org/schema/context 
                           http://www.springframework.org/schema/context/spring-context.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                           ">
    <context:component-scan base-package="org.apache.shardingsphere.example.core.mybatis" />

    <bean id="demo_ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="inlineStrategyShardingAlgorithm" />

    <sharding:sharding-algorithm id="inlineStrategyShardingAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">demo_ds_${user_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>

    <sharding:key-generate-algorithm id="snowflakeAlgorithm" type="SNOWFLAKE">
    </sharding:key-generate-algorithm>

    <sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
    <sharding:key-generate-strategy id="itemKeyGenerator" column="order_item_id" algorithm-ref="snowflakeAlgorithm" />

    <sharding:rule id="shardingRule">
        <sharding:table-rules>
            <sharding:table-rule logic-table="t_order" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="orderKeyGenerator" />
            <sharding:table-rule logic-table="t_order_item" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="itemKeyGenerator" />
        </sharding:table-rules>
        <sharding:binding-table-rules>
            <sharding:binding-table-rule logic-tables="t_order,t_order_item"/>
        </sharding:binding-table-rules>
        <sharding:broadcast-table-rules>
            <sharding:broadcast-table-rule table="t_address"/>
        </sharding:broadcast-table-rules>
    </sharding:rule>

    <shardingsphere:data-source id="shardingDataSource" database-name="sharding-databases" data-source-names="demo_ds_0, demo_ds_1" rule-refs="shardingRule" />

    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="shardingDataSource" />
    </bean>
    <tx:annotation-driven />

    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="shardingDataSource"/>
        <property name="mapperLocations" value="classpath*:META-INF/mappers/*.xml"/>
    </bean>

    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="org.apache.shardingsphere.example.core.mybatis.repository"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
- [Developer Guide: Data Sharding](/en/dev-manual/sharding/)
