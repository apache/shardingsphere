+++
title = "ShardingSphere 5.3.x Update: Spring Configuration Update Guide"
weight = 90
chapter = true 

+++

![img](https://shardingsphere.apache.org/blog/img/2023_03_07_ShardingSphere_5.3.x_Update_Spring_Configuration_Update_Guide1.png)

Before 5.3.0, ShardingSphere-JDBC supported Java API, YAML, Spring Boot Starter, and Spring Namespace.

However, compatibility with Spring has brought the following challenges to the community:

- Many configuration files need to be adjusted when adding or updating API, which causes a heavy workload.
- The community needs to maintain lots of configuration documents and examples.
- Spring Bean life cycle management is susceptible to other project dependencies, such as PostProcessor.[1][2]
- Spring Boot Starter and Spring Namespace's style differs from YAML in ShardingSphere.
- Spring Boot Starter and Spring Namespace are affected by the update of Spring, causing more configuration compatibility problems.

For example, in the latest release of Spring Boot 3.0.0,

`spring.factories` supported in 2.x has been removed.[3][4]

This is a challenge for ShardingSphere Spring Boot Starter users. It's hard to upgrade because it would cause new compatibility problems.

As a result, our community decided to remove all Spring dependencies and configuration support in the ShardingSphere 5.3.0 Release.

So, how do the ShardingSphere-JDBC users who need to use Spring Boot or Spring Namespace access ShardingSphere and upgrades? Read on to find the solutions.

# **Impact**

For the convenience of users who are using ShardingSphere Spring Boot Starter or ShardingSphere Spring Namespace to assess the impact of the upgrade, we have sorted out the impact of this upgrade:

## Maven Coordinates

The original Spring-related dependency coordinates will be invalid after the upgrade to ShardingSphere 5.3.0 and future versions.

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

Adjusted to:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Custom Algorithm

The `AlgorithmProvided` classes will be removed as well after removing the Spring module. Logic related to Bean injection will be invalid after the update, though it has been previously used in the custom algorithm.

For scenarios that require Spring Beans, developers need to proactively manage them.

## Transaction

There will be no `@ShardingSphereTransactionType` annotation, which supports declarative method-level transactions.

If you need to change the transaction type, see Java API[5].

## Configuration

After the 5.3.0 update, the original Spring Boot Starter and Spring Namespace data source configuration will be invalid. See the next chapter to learn how to configure it after the update.

# Guide

## ShardingSphereDriver

From 5.1.2, ShardingSphere-JDBC provides a native JDBC driver, `ShardingSphereDriver`. Engineers can access through configuration directly without rewriting the code.

This way, the formats of ShardingSphere-JDBC and ShardingSphere-Proxy can be more unified. They can be reused with only a few changes. See User Manual-JDBC Driver[6] for details.

After the update to 5.3.x, those who use Spring Boot Starter or Spring Namespace are advised to use `ShardingSphereDriver` to access ShardingSphered-JDBC.

## For Spring Boot Starter Users

## Before the Update

The ShardingSphere configuration in `application.yml` is as follows:

**application.yml**

```yaml
spring:
  shardingsphere:
    database:
      name: sharding_db
    datasource:
      names: ds_0,ds_1
      ds_0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
        username: root
        password:
      ds_1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
        username: root
        password:
    rules:
      sharding:
        default-database-strategy:
          standard:
            sharding-column: id
            sharding-algorithm-name: database_inline
        tables:
          t_order:
            actual-data-nodes: ds_$->{0..1}.t_order_$->{0..1}
            table-strategy:
              standard:
                sharding-column: count
                sharding-algorithm-name: t_order_inline
        sharding-algorithms: 
          database_inline:
            type: INLINE
            props:
              algorithm-expression: ds_$->{user_id % 2}
          t_order_inline:
            type: INLINE
            props:
              algorithm-expression: t_order_$->{order_id % 2}
    props:
      sql-show: true
```

## After the Update

Create a new YAML configuration file in `resources`, such as `sharding.yaml`. Then rewrite the original configuration according to the user manual -YAML configuration[7].

**sharding.yaml**

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    username: root
    password:

rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
      tableStrategy:
        standard:
          shardingColumn: count
          shardingAlgorithmName: t_order_inline
  defaultDatabaseStrategy:
    standard:
      shardingColumn: id
      shardingAlgorithmName: database_inline
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_$->{user_id % 2}
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_$->{order_id % 2}

props:
  sql-show: true
```

If the cluster mode is deployed and required configurations for the `namespace` exist, only `mode` needs to be configured.

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
```

**application.yml**

Replace the original ShardingSphere configuration with the ShardingSphereDriver configuration:

```yaml
spring:
  datasource:
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
    url: jdbc:shardingsphere:classpath:sharding.yaml
```

## For Spring Namespace Users

## Before the Update

**spring-sharding.xml**

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

    <bean id="ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="standardJdbcUrl" value="jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="standardJdbcUrl" value="jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="inlineStrategyShardingAlgorithm" />

    <sharding:sharding-algorithm id="inlineStrategyShardingAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">ds_${user_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    
    <sharding:standard-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-ref="orderTableAlgorithm" />
    
    <sharding:sharding-algorithm id="orderTableAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">t_order_${order_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    
    <sharding:rule id="shardingRule">
        <sharding:table-rules>
            <sharding:table-rule logic-table="t_order" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" />
        </sharding:table-rules>
    </sharding:rule>
    
    <shardingsphere:data-source id="shardingDataSource" database-name="sharding-databases" data-source-names="ds_0,ds_1" rule-refs="shardingRule" >
        <props>
            <prop key="sql-show">true</prop>
        </props>
    </shardingsphere:data-source>
</beans>
```

## After the Update

**sharding.yaml**

`sharding.yaml` has been added in the same format as Spring Boot's YAML above.

**spring-sharding.xml**

The original ShardingSphere configuration in `spring-sharding.xml` has been replaced with the configuration of ShardingSphereDriver.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <bean id="shardingDataSource" class="com.zaxxer.hikari.HikariDataSource">
        <property name="driverClass" value="org.apache.shardingsphere.driver.ShardingSphereDriver" />
        <property name="url" value="jdbc:shardingsphere:classpath:sharding.yaml" />
    </bean>
</beans>
```

🏆 Complete the above configuration and you can enjoy the new version of ShardingSphere-JDBC!

# Conclusion

This update greatly reduces the difference between ShardingSphere-JDBC and ShardingSphere-Proxy.

It will help ShardingSphere-JDBC users to make a smooth transition to ShardingSphere cluster architecture. API standardization and configuration compatibility have made solid progress.

For new ShardingSphere users, the configuration of `ShardingSphereDriver` is also easier and less intrusive.

Since then, the Apache ShardingSphere community has been able to focus more on its own iterations, bringing ever-better features to all users and developers.

For more information about the update, please refer to the user manual of the official website[8].

If readers have any questions or suggestions about Apache ShardingSphere, please raise them on the GitHub issue[9], or visit our slack[10] for discussion.

# Author

**Jiahao Chen**

*Middleware Development Engineer at SphereEx, Apache ShardingSphere Contributor*

# Relevant Links:

🔗 [ShardingSphere Official Website](https://shardingsphere.apache.org/)

🔗 [ShardingSphere Official Project Repo](https://github.com/apache/shardingsphere)

🔗 [ShardingSphere Twitter](https://twitter.com/ShardingSphere)

🔗 [ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[1] [issue: ShardingSphereAlgorithmPostProcessor.init()](https://github.com/apache/shardingsphere/issues/18093)

[2] [issue: ShardingSphere Bean is not eligible for getting processed by all BeanPostProcessors](https://github.com/apache/shardingsphere/issues/11650)

[3] [Spring Boot: Remove spring.factories auto-configuration support](https://github.com/spring-projects/spring-boot/issues/29699)

[4] [issue: spring boot 3.0.0-M5 Failed to determine a suitable driver class](https://github.com/apache/shardingsphere/issues/21225)

[5] [User Manual -Java API](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/special-api/transaction/java-api/)

[6] [User Manual -JDBC Driver](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/)

[7] [User Manual-YAML Configuration](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/)

[8] [User Manual](https://shardingsphere.apache.org/document/current/en/user-manual/)

[9] [GitHub issue](https://github.com/apache/shardingsphere/issues)

[10] [ShardingSphere Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)