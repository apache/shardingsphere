+++
title = "数据分片"
weight = 1
+++

## 背景信息

数据分片 Spring 命名空间的配置方式，适用于传统的 Spring 项目，通过命名空间 xml 配置文件的方式配置分片规则和属性，由 Spring 完成 ShardingSphereDataSource 对象的创建和管理，避免额外的编码工作。

## 参数解释

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.2.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.2.0.xsd)

\<sharding:rule />

| *名称*                                  | *类型* | *说明*           |
|---------------------------------------| ------ |----------------|
| id                                    | 属性   | Spring Bean Id |
| table-rules (?)                       | 标签   | 分片表规则配置        |
| auto-table-rules (?)                  | 标签   | 自动分片表规则配置      |
| binding-table-rules (?)               | 标签   | 绑定表规则配置        |
| broadcast-table-rules (?)             | 标签   | 广播表规则配置        |
| default-database-strategy-ref (?)     | 属性   | 默认分库策略名称       |
| default-table-strategy-ref (?)        | 属性   | 默认分表策略名称       |
| default-key-generate-strategy-ref (?) | 属性   | 默认分布式序列策略名称    |
| default-audit-strategy-ref (?)        | 属性   | 默认分片审计策略名称     |
| default-sharding-column (?)           | 属性   | 默认分片列名称        |

\<sharding:table-rule />

| *名称*                      | *类型* | *说明*                                                                                                                       |
|---------------------------| ----- |----------------------------------------------------------------------------------------------------------------------------|
| logic-table               | 属性  | 逻辑表名称                                                                                                                      |
| actual-data-nodes         | 属性  | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持 inline 表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| actual-data-sources       | 属性  | 自动分片表数据源名                                                                                                                  |
| database-strategy-ref     | 属性  | 标准分片表分库策略名称                                                                                                                |
| table-strategy-ref        | 属性  | 标准分片表分表策略名称                                                                                                                |
| sharding-strategy-ref     | 属性  | 自动分片表策略名称                                                                                                                  |
| key-generate-strategy-ref | 属性  | 分布式序列策略名称                                                                                                                  |
| audit-strategy-ref        | 属性  | 分片审计策略名称                                                                                                                   |

\<sharding:binding-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| binding-table-rule (+) | 标签   | 绑定表规则配置 |

\<sharding:binding-table-rule />

| *名称*       | *类型*  | *说明*                   |
| ------------ | ------ | ------------------------ |
| logic-tables | 属性   | 绑定表名称，多个表以逗号分隔 |

\<sharding:broadcast-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| broadcast-table-rule (+) | 标签   | 广播表规则配置 |

\<sharding:broadcast-table-rule />

| *名称* | *类型* | *说明*   |
| ------ | ----- | -------- |
| table  | 属性  | 广播表名称 |

\<sharding:standard-strategy />

| *名称*          | *类型* | *说明*          |
| --------------- | ----- | -------------- |
| id              | 属性   | 标准分片策略名称 |
| sharding-column | 属性   | 分片列名称      |
| algorithm-ref   | 属性   | 分片算法名称    |

\<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                    |
| ---------------- | ----- | ------------------------- |
| id               | 属性   | 复合分片策略名称            |
| sharding-columns | 属性   | 分片列名称，多个列以逗号分隔 |
| algorithm-ref    | 属性   | 分片算法名称               |

\<sharding:hint-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | Hint 分片策略名称 |
| algorithm-ref | 属性   | 分片算法名称      |

\<sharding:none-strategy />

| *名称* | *类型* | *说明*      |
| ------ | ----- | ----------- |
| id     | 属性   | 分片策略名称 |

\<sharding:key-generate-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | 分布式序列策略名称 |
| column        | 属性   | 分布式序列列名称   |
| algorithm-ref | 属性   | 分布式序列算法名称 |

\<sharding:audit-strategy />

| *名称*              | *类型*   | *说明*         |
| -------------------|--------|--------------|
| id                 | 属性     | 分片审计策略名称     |
| allow-hint-disable | 属性     | 是否禁用分片审计hint |
| auditors           | 标签     | 分片审计算法名称     |

\<sharding:auditors />

| *名称*             | *类型*   | *说明*     |
| -----------------|--------|----------|
| auditor          | 标签     | 分片审计算法名称 |

\<sharding:auditor />

| *名称*             | *类型* | *说明*     |
| -----------------|------|----------|
| algorithm-ref    | 属性   | 分片审计算法名称 |

\<sharding:sharding-algorithm />

| *名称*    | *类型* | *说明*        |
| --------- | ----- | ------------- |
| id        | 属性  | 分片算法名称    |
| type      | 属性  | 分片算法类型    |
| props (?) | 标签  | 分片算法属性配置 |

\<sharding:key-generate-algorithm />

| *名称*    | *类型* | *说明*              |
| --------- | ----- | ------------------ |
| id        | 属性  | 分布式序列算法名称    |
| type      | 属性  | 分布式序列算法类型    |
| props (?) | 标签  | 分布式序列算法属性配置 |

\<sharding:audit-algorithm />

| *名称*    | *类型* | *说明*       |
| --------- | ----- |------------|
| id        | 属性  | 分片审计算法名称   |
| type      | 属性  | 分片审计算法类型   |
| props (?) | 标签  | 分片审计算法属性配置 |

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/common-config/builtin-algorithm/sharding)和[内置分布式序列算法列表](/cn/user-manual/common-config/builtin-algorithm/keygen)。

> 注意事项：行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。

## 操作步骤

1. 在 Spring 命名空间配置文件中配置数据分片规则，包含数据源、分片规则、全局属性等配置项；
2. 启动 Spring 程序，会自动加载配置，并初始化 ShardingSphereDataSource。

## 配置示例

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

    <sharding:audit-algorithm id="auditAlgorithm" type="DML_SHARDING_CONDITIONS" />
    
    <sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
    <sharding:key-generate-strategy id="itemKeyGenerator" column="order_item_id" algorithm-ref="snowflakeAlgorithm" />

    <sharding:audit-strategy id="defaultAudit" allow-hint-disable="true">
        <sharding:auditors>
            <sharding:auditor algorithm-ref="auditAlgorithm" />
        </sharding:auditors>
    </sharding:audit-strategy>
    <sharding:audit-strategy id="shardingKeyAudit" allow-hint-disable="true">
        <sharding:auditors>
            <sharding:auditor algorithm-ref="auditAlgorithm" />
        </sharding:auditors>
    </sharding:audit-strategy>
    
    <sharding:rule id="shardingRule">
        <sharding:table-rules>
            <sharding:table-rule logic-table="t_order" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="orderKeyGenerator" audit-strategy-ref="shardingKeyAudit" />
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

## 相关参考

- [核心特性：数据分片](/cn/features/sharding/)
- [开发者指南：数据分片](/cn/dev-manual/sharding/)
