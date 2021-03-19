+++
title = "变更历史"
weight = 7
+++

## 5.0.0-alpha

### 读写分离

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/replica-query/replica-query-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/replica-query/replica-query-5.0.0.xsd)

\<replica-query:rule />

| *名称*                | *类型* | *说明*           |
| -------------------- | ------ | --------------- |
| id                   | 属性   | Spring Bean Id   |
| data-source-rule (+) | 标签   | 读写分离数据源规则配置 |

\<replica-query:data-source-rule />

| *名称*                     | *类型* | *说明*                          |
| -------------------------- | ----- | ------------------------------- |
| id                         | 属性  | 读写分离数据源规则名称             |
| primary-data-source-name   | 属性  | 主数据源名称                      |
| replica-data-source-names  | 属性  | 从数据源名称，多个从数据源用逗号分隔 |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称                   |


\<replica-query:load-balance-algorithm />

| *名称*    | *类型* | *说明*            |
| --------- | ----- | ----------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。

## 4.x

### 读写分离

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd](http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd)

\<master-slave:data-source />

| *名称*                  | *类型* | *说明*                                                       |
| :---------------------- | :----- | :----------------------------------------------------------- |
| id                      | 属性   | Spring Bean Id                                               |
| master-data-source-name | 属性   | 主库数据源Bean Id                                            |
| slave-data-source-names | 属性   | 从库数据源Bean Id列表，多个Bean以逗号分隔                    |
| strategy-ref (?)        | 属性   | 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口 |
| strategy-type (?)       | 属性   | 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`strategy-ref`存在则忽略该配置 |
| props (?)               | 标签   | 属性配置                                                     |

\<master-slave:props />

| *名称*                             | *类型* | *说明*                                                |
| :--------------------------------- | :----- | :---------------------------------------------------- |
| sql.show (?)                       | 属性   | 是否开启SQL显示，默认值: false                        |
| executor.size (?)                  | 属性   | 工作线程数量，默认值: CPU核数                         |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性   | 是否在启动时检查分表元数据一致性，默认值: false       |

\<master-slave:load-balance-algorithm />

4.0.0-RC2 版本 添加

| *名称*        | *类型* | *说明*                                                    |
| :------------ | :----- | :-------------------------------------------------------- |
| id            | 属性   | Spring Bean Id                                            |
| type          | 属性   | 负载均衡算法类型，‘RANDOM'或’ROUND_ROBIN’，支持自定义拓展 |
| props-ref (?) | 属性   | 负载均衡算法配置参数                                      |

## 3.x

### 读写分离

#### 配置项说明

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.io/schema/shardingsphere/masterslave  
                        http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="randomStrategy" class="io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
            <master-slave:props>
                <prop key="sql.show">${sql_show}</prop>
                <prop key="executor.size">10</prop>
                <prop key="foo">bar</prop>
            </master-slave:props>
    </master-slave:data-source>
</beans>
```

## 2.x

### 读写分离

#### Spring命名空间配置示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingsphere.io/schema/shardingjdbc/sharding"
    xmlns:masterslave="http://shardingsphere.io/schema/shardingjdbc/masterslave"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingsphere.io/schema/shardingjdbc/sharding
                        http://shardingsphere.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://shardingsphere.io/schema/shardingjdbc/masterslave
                        http://shardingsphere.io/schema/shardingjdbc/masterslave/master-slave.xsd
                        ">
    <!-- 配置真实数据源 -->
    <bean id="dbtbl_0_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <!-- 定义读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理 -->
    <master-slave:data-source id="dbtbl_0" master-data-source-name="dbtbl_0_master" slave-data-source-names="dbtbl_0_slave_0, dbtbl_0_slave_1" strategy-type="ROUND_ROBIN" />
    <master-slave:data-source id="dbtbl_1" master-data-source-name="dbtbl_1_master" slave-data-source-names="dbtbl_1_slave_0, dbtbl_1_slave_1" strategy-type="ROUND_ROBIN" />
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="dbtbl_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 4}" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="dbtbl_0, dbtbl_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy"/>
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

## 1.x

### 读写分离

#### Spring命名空间配置示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:rdb="http://www.dangdang.com/schema/ddframe/rdb" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://www.dangdang.com/schema/ddframe/rdb 
                        http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd 
                        ">
    <!-- 配置真实数据源 -->
    <bean id="dbtbl_0_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <!-- 定义读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理 -->
    <rdb:master-slave-data-source id="dbtbl_0" master-data-source-ref="dbtbl_0_master" slave-data-sources-ref="dbtbl_0_slave_0, dbtbl_0_slave_1" strategy-type="ROUND_ROBIN" />
    <rdb:master-slave-data-source id="dbtbl_1" master-data-source-ref="dbtbl_1_master" slave-data-sources-ref="dbtbl_1_slave_0, dbtbl_1_slave_1" strategy-type="ROUND_ROBIN" />
    
    <!-- 通过rdb:strategy和rdb:data-source继续构建分片数据源 -->
    <rdb:strategy id="databaseStrategy" sharding-columns="user_id" algorithm-expression="dbtbl_${user_id.longValue() % 2}"/>
    <rdb:strategy id="orderTableStrategy" sharding-columns="order_id" algorithm-expression="t_order_${order_id.longValue() % 4}"/>
    
    <rdb:data-source id="shardingDataSource">
        <rdb:sharding-rule data-sources="dbtbl_0, dbtbl_1">
            <rdb:table-rules>
                <rdb:table-rule logic-table="t_order" actual-tables="t_order_${0..3}" database-strategy="databaseStrategy" table-strategy="orderTableStrategy"/>
            </rdb:table-rules>
        </rdb:sharding-rule>
    </rdb:data-source>
</beans>
```
