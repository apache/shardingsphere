+++
title = "Spring 命名空间"
weight = 4
chapter = true
+++

## 简介

ShardingSphere 提供 JDBC 驱动，开发者可以在 Spring 中配置 `ShardingSphereDriver` 来使用 ShardingSphere。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 Spring Bean

#### 配置项说明

| *名称*        | *类型* | *说明*                                     |
|-------------|------|------------------------------------------|
| driverClass | 属性   | 数据库 Driver，这里需要指定使用 ShardingSphereDriver |
| url         | 属性   | YAML 配置文件路径                              |

#### 配置示例

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <bean id="shardingDataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
        <property name="driverClass" value="org.apache.shardingsphere.driver.ShardingSphereDriver" />
        <property name="url" value="jdbc:shardingsphere:classpath:xxx.yaml" />
    </bean>
</beans>
```

### 使用数据源

使用方式同 Spring Boot。
