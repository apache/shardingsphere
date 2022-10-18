+++
title = "Encryption"
weight = 4
+++

## Background

Spring Namespace's data encryption configuration applies to the traditional Spring projects. Sharding rules and attributes are configured through the XML configuration file of the namespace. Spring creates and manages the ShardingSphereDataSource object, reducing unnecessary coding.

## Parameters

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.2.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.2.1.xsd)

\<encrypt:rule />

| *Name*                    | *Type*    | *Description*                                                                                  | *Default Value* |
| ------------------------- | --------- | ---------------------------------------------------------------------------------------------- | --------------- |
| id                        | Attribute | Spring Bean Id                                                                                 |                 |
| queryWithCipherColumn (?) | Attribute | Whether query with cipher column for data encrypt. User you can use plaintext to query if have | true            |
| table (+)                 | Tag       | Encrypt table configuration                                                                    |                 |

\<encrypt:table />

| *Name*                          | *Type*    | *Description*                                                                                            |
| ------------------------------- | --------- | -------------------------------------------------------------------------------------------------------- |
| name                            | Attribute | Encrypt table name                                                                                       |
| column (+)                      | Tag       | Encrypt column configuration                                                                             |
| query-with-cipher-column(?) (?) | Attribute | Whether the table query with cipher column for data encrypt. User you can use plaintext to query if have |

\<encrypt:column />

| *Name*                    | *Type*     | *Description*              |
| ------------------------- | ---------- | -------------------------- |
| logic-column              | Attribute  | Column logic name          |
| cipher-column             | Attribute  | Cipher column name         |
| assisted-query-column (?) | Attribute  | Assisted query column name |
| plain-column (?)          | Attribute  | Plain column name          |
| encrypt-algorithm-ref     | Attribute  | Encrypt algorithm name     |

\<encrypt:encrypt-algorithm />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| id        | Attribute  | Encrypt algorithm name       |
| type      | Attribute  | Encrypt algorithm type       |
| props (?) | Tag        | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/common-config/builtin-algorithm/encrypt) for more details about type of algorithm.

## Procedure 

1. Configure data encryption rules in the Spring namespace configuration file, including data sources, encryption rules, and global attributes.
2. Start the Spring program, and it will automatically load the configuration and initialize the ShardingSphereDataSource.

## Sample

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

## Related References

- [Core Feature: Data Encryption](/en/features/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
