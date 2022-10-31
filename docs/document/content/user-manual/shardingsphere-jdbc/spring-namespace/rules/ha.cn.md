+++
title = "高可用"
weight = 3
+++

## 背景信息

`Spring 命名空间` 的配置方式，适用于传统的 Spring 项目，通过命名空间 `xml` 配置文件的方式配置高可用规则，由 Spring 完成 `ShardingSphereDataSource` 对象的创建和管理。

## 参数解释

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.1.1.xsd)

\<database-discovery:rule />

| *名称*                  | *类型* | *说明*               |
| ----------------------- | ------ | ------------------ |
| id                      | 属性   | Spring Bean Id      |
| data-source-rule (+)    | 标签   | 数据源规则配置        |
| discovery-heartbeat (+) | 标签   | 检测心跳规则配置       |

\<database-discovery:data-source-rule />

| *名称*                       | *类型* | *说明*                                      |
| --------------------------- | ----- | ------------------------------------------ |
| id                          | 属性  | 数据源规则名称                                |
| data-source-names           | 属性  | 数据源名称，多个数据源用逗号分隔 如：ds_0, ds_1  |
| discovery-heartbeat-name    | 属性  | 检测心跳名称                                 |
| discovery-type-name         | 属性  | 数据库发现类型名称                               |

\<database-discovery:discovery-heartbeat />

| *名称*                       | *类型* | *说明*                                      |
| --------------------------- | ----- | ------------------------------------------  |
| id                          | 属性  | 监听心跳名称                                  |
| props                       | 标签  | 监听心跳属性配置，keep-alive-cron 属性配置 cron 表达式，如：'0/5 * * * * ?'  |

\<database-discovery:discovery-type />

| *名称*     | *类型* | *说明*                                    |
| --------- | ----- | ----------------------------------------- |
| id        | 属性  | 数据库发现类型名称                               |
| type      | 属性  | 数据库发现类型，如：MySQL.MGR               |
| props (?) | 标签  | 数据库发现类型配置，如 MGR 的 group-name 属性配置   |

## 操作步骤

### 1. 引入 MAVEN 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
``` 

## 配置示例

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

## 相关参考

- [高可用核心特性](/cn/features/ha/)
- [JAVA API：高可用配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/ha/)
- [YAML 配置：高可用配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/ha/)
- [Spring Boot Starter：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)