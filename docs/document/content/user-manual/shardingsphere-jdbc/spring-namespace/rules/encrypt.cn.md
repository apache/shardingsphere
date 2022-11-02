+++
title = "数据加密"
weight = 4
+++

## 背景信息

数据加密 Spring 命名空间的配置方式，适用于传统的 Spring 项目，通过命名空间 xml 配置文件的方式配置分片规则和属性，由 Spring 完成 ShardingSphereDataSource 对象的创建和管理，避免额外的编码工作。

## 参数解释

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.2.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.2.1.xsd)

\<encrypt:rule />

| *名称*                     | *类型* | *说明*                                               | *默认值* |
| ------------------------- | ----- | ---------------------------------------------------- | ------- |
| id                        | 属性  | Spring Bean Id                                        |         |
| queryWithCipherColumn (?) | 属性  | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 | true   |
| table (+)                 | 标签  | 加密表配置                                              |         |

\<encrypt:table />

| *名称*                       | *类型* | *说明*                                                     |
| --------------------------- | ------ | --------------------------------------------------------- |
| name                        | 属性    | 加密表名称                                                  |
| column (+)                  | 标签    | 加密列配置                                                  |
| query-with-cipher-column(?) | 属性    | 该表是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 |

\<encrypt:column />

| *名称*                    | *类型* | *说明*       |
| ------------------------- | ----- | ----------- |
| logic-column              | 属性  | 加密列逻辑名称 |
| cipher-column             | 属性  | 加密列名称    |
| assisted-query-column (?) | 属性  | 查询辅助列名称 |
| plain-column (?)          | 属性  | 原文列名称    |
| encrypt-algorithm-ref     | 属性  | 加密算法名称   |

\<encrypt:encrypt-algorithm />

| *名称*    | *类型* | *说明*        |
| --------- | ----- | ------------ |
| id        | 属性  | 加密算法名称    |
| type      | 属性  | 加密算法类型    |
| props (?) | 标签  | 加密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/common-config/builtin-algorithm/encrypt)。

## 操作步骤

1. 在 Spring 命名空间配置文件中配置数据加密规则，包含数据源、加密规则、全局属性等配置项；
2. 启动 Spring 程序，会自动加载配置，并初始化 ShardingSphereDataSource。

## 配置示例

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.springframework.org/schema/tx 
                           http://www.springframework.org/schema/tx/spring-tx.xsd
                           http://www.springframework.org/schema/context 
                           http://www.springframework.org/schema/context/spring-context.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd 
                           ">
    <context:component-scan base-package="org.apache.shardingsphere.example.core.mybatis" />
    
    <bean id="ds" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <encrypt:encrypt-algorithm id="name_encryptor" type="AES">
        <props>
            <prop key="aes-key-value">123456</prop>
        </props>
    </encrypt:encrypt-algorithm>
    <encrypt:encrypt-algorithm id="pwd_encryptor" type="assistedTest" />
    
    <encrypt:rule id="encryptRule">
        <encrypt:table name="t_user">
            <encrypt:column logic-column="username" cipher-column="username" plain-column="username_plain" encrypt-algorithm-ref="name_encryptor" />
            <encrypt:column logic-column="pwd" cipher-column="pwd" assisted-query-column="assisted_query_pwd" encrypt-algorithm-ref="pwd_encryptor" />
        </encrypt:table>
    </encrypt:rule>
    
    <shardingsphere:data-source id="encryptDataSource" data-source-names="ds" rule-refs="encryptRule" />
    
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="encryptDataSource" />
    </bean>
    <tx:annotation-driven />
    
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="encryptDataSource"/>
        <property name="mapperLocations" value="classpath*:META-INF/mappers/*.xml"/>
    </bean>
    
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="org.apache.shardingsphere.example.core.mybatis.repository"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```

## 相关参考

- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
