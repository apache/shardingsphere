+++
title = "HA"
weight = 3
+++

## Background

The Spring namespace configuration method, applicable to traditional Spring projects, configures highly availability rules by means of namespace XML configuration files, and Spring completes the creation and management of ShardingSphereDataSource objects.

## Parameters Explained

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.1.1.xsd)

\<database-discovery:rule />

| *Name*                  | *Type* | *Description*               |
| ----------------------- | ------ | ------------------ |
| id                      | Property   | Spring Bean Id      |
| data-source-rule (+)    | Tag   | Configuration of data source rules |
| discovery-heartbeat (+) | Tag   | Configuration of heartbeat rules detection |

\<database-discovery:data-source-rule />

| *Name*                       | *Type* | *Description*                                      |
| --------------------------- | ----- | ------------------------------------------ |
| id                          | Property  | Data source rules name  |
| data-source-names           | Property  | Data source name，multiple datasources are divided by comma,such as：ds_0, ds_1  |
| discovery-heartbeat-name    | Property  | Detect heartbeat name|
| discovery-type-name         | Property  | type name of database discovery |

\<database-discovery:discovery-heartbeat />

| *Name*                       | *Type* | *Description*                                      |
| --------------------------- | ----- | ------------------------------------------  |
| id                          | Property  | heartbeat listen name                                 |
| props                       | 标签  | property configuration of heartbeat listen，cron expression of keep-alive-cron property configuration，such as：'0/5 * * * * ?'  |

\<database-discovery:discovery-type />

| *Name*     | *Type* | *Description*                                    |
| --------- | ----- | ----------------------------------------- |
| id        | Property  | Type name of database discovery|
| type      | Property  | Database discovery type，such as：MySQL.MGR               |
| props (?) | Tag  | Configuration of database discovery type，such as group-name property configuration of MGR |

## Operating Procedures

### 1. Introduce Maven dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
``` 

## Configuration Example

```xml  
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:database-discovery="http://shardingsphere.apache.org/schema/shardingsphere/database-discovery"
       xmlns:readwrite-splitting="http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/database-discovery
                           http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           ">
    <bean id="ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://127.0.0.1:33306/primary_demo_ds?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <bean id="ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://127.0.0.1:33307/primary_demo_ds?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <bean id="ds_2" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://127.0.0.1:33308/primary_demo_ds?useSSL=false"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    <cluster:repository id="clusterRepository" type="ZooKeeper" namespace="governance" server-lists="localhost:2181">
        <props>
            <prop key="max-retries">3</prop>
            <prop key="operation-timeout-milliseconds">3000</prop>
        </props>
    </cluster:repository>
    <readwrite-splitting:rule id="readWriteSplittingRule">
        <readwrite-splitting:data-source-rule id="replica_ds">
            <readwrite-splitting:dynamic-strategy id="dynamicStrategy" auto-aware-data-source-name="readwrite_ds" />
        </readwrite-splitting:data-source-rule>
    </readwrite-splitting:rule>
    <database-discovery:rule id="mgrDatabaseDiscoveryRule">
        <database-discovery:data-source-rule id="readwrite_ds" data-source-names="ds_0,ds_1,ds_2" discovery-heartbeat-name="mgr-heartbeat" discovery-type-name="mgr" />
        <database-discovery:discovery-heartbeat id="mgr-heartbeat">
            <props>
                <prop key="keep-alive-cron" >0/5 * * * * ?</prop>
            </props>
        </database-discovery:discovery-heartbeat>
    </database-discovery:rule>
    <database-discovery:discovery-type id="mgr" type="MySQL.MGR">
        <props>
            <prop key="group-name">558edd3c-02ec-11ea-9bb3-080027e39bd2</prop>
        </props>
    </database-discovery:discovery-type>
    <shardingsphere:data-source id="databaseDiscoveryDataSource" schema-name="database-discovery-db" data-source-names="ds_0, ds_1, ds_2" rule-refs="readWriteSplittingRule, mgrDatabaseDiscoveryRule">
    <shardingsphere:mode repository-ref="clusterRepository" type="Cluster" />
    </shardingsphere:data-source>
</beans>
```

## Related References

- [Feature Description of HA](en/features/ha/)
- [JAVA API: HA](/en/user-manual/shardingsphere-jdbc/java-api/rules/ha/)
- [YAML Configuration: HA](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/ha/)
- [Spring Boot Starter: HA](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)