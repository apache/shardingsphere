+++
title = "Spring 命名空间"
weight = 4
chapter = true
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring 命名空间，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 Spring Bean

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.1.2.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.1.2.xsd)

\<shardingsphere:data-source />

| *名称*             | *类型* | *说明*                                                                            |
| ----------------- | ----- | --------------------------------------------------------------------------------- |
| id                | 属性  | Spring Bean Id                                                                     |
| database-name (?)   | 属性  | JDBC 数据源别名                                                                      |
| data-source-names | 标签  | 数据源名称，多个数据源以逗号分隔                                                         |
| rule-refs         | 标签  | 规则名称，多个规则以逗号分隔                                                            |
| mode (?)          | 标签  | 运行模式配置                                                                         |
| props (?)         | 标签  | 属性配置，详情请参见[属性配置](/cn/user-manual/shardingsphere-jdbc/props) |

#### 配置示例

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

### 使用数据源

使用方式同 Spring Boot Starter。
