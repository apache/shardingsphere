<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xmlns:readwrite-splitting="http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xmlns:shadow="http://shardingsphere.apache.org/schema/shardingsphere/shadow"
       xmlns:database-discovery="http://shardingsphere.apache.org/schema/shardingsphere/database-discovery"
       xmlns:sql-parser="http://shardingsphere.apache.org/schema/shardingsphere/sql-parser"
       xmlns:standalone="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://www.springframework.org/schema/context
                           http://www.springframework.org/schema/context/spring-context.xsd
                           http://www.springframework.org/schema/tx
                           http://www.springframework.org/schema/tx/spring-tx.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding
                           http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/shadow
                           http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/database-discovery
                           http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/sql-parser 
                           http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository.xsd
                           ">
<#assign package = feature?replace('-', '')?replace(',', '.') />

    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}"/>
<#if framework=="spring-namespace-jpa">
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQL5Dialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create-drop</prop>
                <prop key="hibernate.show_sql">false</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
</#if>
    
    <!--
    Notice: If you are using the db-discovery module, please replace the database address with the corresponding database cluster address
    -->
    
    <bean id="ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://${host}:${port}/demo_ds_0?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${(password)?string}"/>
    </bean>
    
    <bean id="ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://${host}:${port}/demo_ds_1?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${(password)?string}"/>
    </bean>
    
    <bean id="ds_2" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://${host}:${port}/demo_ds_2?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${(password)?string}"/>
    </bean>
<#list feature?split(",") as item>
    <#include "${item}.ftl">
</#list>
    
<#if framework=="spring-namespace-mybatis">
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
    </bean>
    <tx:annotation-driven />
    
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <property name="mapperLocations" value="classpath*:mappers/*.xml"/>
    </bean>
    
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</#if>

<#if mode?exists>
    <#include "../mode/spring-namespace/config/${mode}.ftl" />
</#if>
<#assign ruleRefs="">
<#list feature?split(",") as item>
    <#assign ruleRefs += toCamel(item) + "Rule" />
    <#if item_has_next>
        <#assign ruleRefs += ", " />
    </#if>
</#list>
    <shardingsphere:data-source id="dataSource" data-source-names="ds_0, ds_1, ds_2" rule-refs="${ruleRefs}">
        <#if mode?contains("cluster")>
            <#include "../mode/spring-namespace/cluster.ftl" />
        <#elseif mode?contains("standalone")>
            <#include "../mode/spring-namespace/standalone.ftl" />
        </#if>
        <props>
            <prop key="sql-show">true</prop>
        <#if transaction=="xa-atomikos">
            <prop key="xa-transaction-manager-type">Atomikos</prop>
        <#elseif transaction=="xa-narayana">
            <prop key="xa-transaction-manager-type">Narayana</prop>
        <#elseif transaction=="xa-bitronix">
            <prop key="xa-transaction-manager-type">Bitronix</prop>
        </#if>
        </props>
    </shardingsphere:data-source>
</beans>
<#function toCamel(s)>
    <#return s
    ?replace('(^-+)|(-+$)', '', 'r')
    ?replace('\\-+(\\w)?', ' $1', 'r')
    ?replace('([A-Z])', ' $1', 'r')
    ?capitalize
    ?replace(' ' , '')
    ?uncap_first
    >
</#function>
