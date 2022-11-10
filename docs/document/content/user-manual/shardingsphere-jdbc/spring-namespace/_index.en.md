+++
title = "Spring Namespace"
weight = 4
chapter = true
+++

## Overview

ShardingSphere-JDBC provides official Spring Namespace to make convenient for developers to integrate ShardingSphere-JDBC and Spring.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Configure Spring Bean

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.2.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.2.1.xsd)

\<shardingsphere:data-source />

| *Name*            | *Type*    | *Description*                                                                                                                                  |
| ----------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| id                | Attribute | Spring Bean Id                                                                                                                                 |
| database-name (?)   | Attribute | JDBC data source alias                                                                                                                         |
| data-source-names | Attribute | Data source name, multiple data source names are separated by commas                                                                           |
| rule-refs         | Attribute | Rule name, multiple rule names are separated by commas                                                                                         |
| mode (?)          | Tag       | Mode configuration                                                                                                                             |
| props (?)         | Tag       | Properties configuration, Please refer to [Properties Configuration](/en/user-manual/shardingsphere-jdbc/props) for more details |

#### Example

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           ">
    <shardingsphere:data-source id="ds" database-name="foo_schema" data-source-names="..." rule-refs="...">
        <shardingsphere:mode type="..." />
        <props>
            <prop key="xxx.xxx">${xxx.xxx}</prop>
        </props>
    </shardingsphere:data-source>
</beans>
```

### Use Data Source

Same with Spring Boot Starter.
