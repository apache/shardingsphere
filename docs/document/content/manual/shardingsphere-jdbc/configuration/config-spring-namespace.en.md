+++
title = "Spring Namespace Configuration"
weight = 4
+++

## Notice

Inline expression identifier can can use `${...} ` or `$->{...}`, but the former one clashes with the placeholder in property documents of Spring, so it is suggested to use `$->{...}` for inline expression identifier under Spring environment.

## Example

example: [shardingsphere-example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/sharding-example/sharding-spring-namespace-jpa-example)

### Sharding

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.core.jpa.entity" />
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

    <bean id="ds0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="preciseModuloDatabaseShardingAlgorithm" class="org.apache.shardingsphere.example.algorithm.PreciseModuloShardingDatabaseAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="org.apache.shardingsphere.example.algorithm.PreciseModuloShardingTableAlgorithm" />

    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />

    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" key-generator-ref="itemKeyGenerator" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
            <sharding:broadcast-table-rules>
                <sharding:broadcast-table-rule table="t_config" />
            </sharding:broadcast-table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Read-Write Split

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.core.jpa" />

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.core.jpa.entity" />
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

    <!-- 4.0.0-RC1 version load balance algorithm configuration -->
    <!-- <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" /> -->

    <!-- 4.0.0-RC2 and later version load balance algorithm configuration -->
    <master-slave:load-balance-algorithm id="randomStrategy" type="RANDOM" />

    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
        <master-slave:props>
            <prop key="sql.show">true</prop>
            <prop key="executor.size">10</prop>
            <prop key="foo">bar</prop>
        </master-slave:props>
    </master-slave:data-source>
</beans>
```

### Data Masking

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xmlns:bean="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd 
                        http://www.springframework.org/schema/util 
                        http://www.springframework.org/schema/util/spring-util.xsd">
   
    <bean id="ds" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean:properties id="props">
        <prop key="aes.key.value">123456</prop>
    </bean:properties>
    
    <encrypt:data-source id="encryptDataSource" data-source-name="ds" >
        <encrypt:encrypt-rule>
            <encrypt:tables>
                <encrypt:table name="t_order">
                    <encrypt:column logic-column="user_id" plain-column="user_decrypt" cipher-column="user_encrypt" assisted-query-column="user_assisted" encryptor-ref="encryptor_aes" />
                    <encrypt:column logic-column="order_id" plain-column="order_decrypt" cipher-column="order_encrypt" assisted-query-column="order_assisted" encryptor-ref="encryptor_md5"/>
                </encrypt:table>
            </encrypt:tables>
            <encrypt:encryptors>
                <encrypt:encryptor id="encryptor_aes" type="AES" props-ref="props"/>
                <encrypt:encryptor id="encryptor_md5" type="MD5" />
            </encrypt:encryptors>
        </encrypt:encrypt-rule>
        <encrypt:props>
            <prop key="sql.show">true</prop>
            <prop key="query.with.cipher.column">true</prop>
        </encrypt:props>
    </encrypt:data-source>
 </beans>
```

### Sharding + Read-write splitting

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.core.jpa" />

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.core.jpa.entity" />
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

    <bean id="ds_master0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master0_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master0_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master1_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <!-- 4.0.0-RC1 version load balance algorithm configuration -->
    <!-- <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" /> -->

    <!-- 4.0.0-RC2 and later version load balance algorithm configuration -->
    <master-slave:load-balance-algorithm id="randomStrategy" type="RANDOM" />

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds_ms$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item$->{order_id % 2}" />

    <bean id="snowflakeAlgorithm" class="KeyGenerateAlgorithmFactoryBean">
        <property name="type" value="SNOWFLAKE" />
    </bean>

    <sharding:key-generator id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
    <sharding:key-generator id="itemKeyGenerator" column="order_item_id" algorithm-ref="snowflakeAlgorithm" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_master0,ds_master0_slave0,ds_master0_slave1,ds_master1,ds_master1_slave0,ds_master1_slave1">
            <sharding:master-slave-rules>
                <sharding:master-slave-rule id="ds_ms0" master-data-source-name="ds_master0" slave-data-source-names="ds_master0_slave0, ds_master0_slave1" strategy-ref="randomStrategy" />
                <sharding:master-slave-rule id="ds_ms1" master-data-source-name="ds_master1" slave-data-source-names="ds_master1_slave0, ds_master1_slave1" strategy-ref="randomStrategy" />
            </sharding:master-slave-rules>
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_ms$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_ms$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generator-ref="itemKeyGenerator" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
            <sharding:broadcast-table-rules>
                <sharding:broadcast-table-rule table="t_config" />
            </sharding:broadcast-table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Sharding + Data Masking

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:bean="http://www.springframework.org/schema/util"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">
    <import resource="classpath:META-INF/shardingTransaction.xml"/>
    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.core.jpa"/>

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.core.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create-drop</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />

    <bean id="demo_ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
        <property name="maximumPoolSize" value="16"/>
    </bean>
    
    <bean id="demo_ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
        <property name="maximumPoolSize" value="16"/>
    </bean>

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="demo_ds_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_${order_id % 2}" />
    <sharding:inline-strategy id="orderEncryptTableStrategy" sharding-column="order_id" algorithm-expression="t_order_encrypt_${order_id % 2}" />

    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" />

    <bean:properties id="dataProtectorProps">
        <prop key="appToken">business</prop>
    </bean:properties>

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="demo_ds_0, demo_ds_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generator-ref="itemKeyGenerator" />
            </sharding:table-rules>
            <sharding:encrypt-rule>
                <encrypt:tables>
                    <encrypt:table name="t_order">
                        <encrypt:column logic-column="user_id" plain-column="user_decrypt" cipher-column="user_encrypt" assisted-query-column="user_assisted" encryptor-ref="encryptor_aes" />
                        <encrypt:column logic-column="order_id" plain-column="order_decrypt" cipher-column="order_encrypt" assisted-query-column="order_assisted" encryptor-ref="encryptor_md5"/>
                    </encrypt:table>
                </encrypt:tables>
                <encrypt:encryptors>
                    <encrypt:encryptor id="encryptor_aes" type="AES"  props-ref="props"/>
                    <encrypt:encryptor id="encryptor_md5" type="MD5" />
                </encrypt:encryptors>
            </sharding:encrypt-rule>

        </sharding:sharding-rule>

        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
     <bean:properties id="props">
        <prop key="aes.key.value">123456</prop>
    </bean:properties>
</beans>
```


### Orchestration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave"
       xmlns:reg="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg 
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg/reg.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding 
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding/sharding.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave  
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave/master-slave.xsd">
    
    <util:properties id="instance-props">
        <prop key="max-retries">3</prop>
        <prop key="operation-timeout-milliseconds">3000</prop>
    </util:properties>
    <orchestration:instance id="regCenter" orchestration-type="registry_center,config_center,metadata_center" instance-type="zookeeper" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo"
                           props-ref="instance-props" />
    <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" instance-ref="regCenter" overwrite="true" />
    <orchestration:slave-data-source id="masterSlaveDataSource" data-source-ref="realMasterSlaveDataSource" instance-ref="regCenter" overwrite="true" />
    <orchestration:data-source id="encryptDataSource" data-source-ref="realEncryptDataSource" instance-ref="regCenter" overwrite="true" />
</beans>
```

## Configuration Item Explanation

### Sharding

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd

#### \<sharding:data-source />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Spring Bean Id              |
| sharding-rule | Tag       | Sharding rule configuration |
| props (?)     | Tag       | Properties                  |

#### \<sharding:sharding-rule />

| *Name*                            | *Type*    | *Description*                                                |
| --------------------------------- | --------- | ------------------------------------------------------------ |
| data-source-names                 | Attribute | Data source Bean list with comma separating multiple Beans   |
| table-rules                       | Tag       | Configuration objects of table sharding rules                |
| binding-table-rules (?)           | Tag       | Binding table rule list                                      |
| broadcast-table-rules (?)         | Tag       | Broadcast table rule list                                    |
| default-data-source-name (?)      | Attribute | Tables without sharding rules will be located through default data source |
| default-database-strategy-ref (?) | Attribute | Default database sharding strategy, which corresponds to id of \<sharding:xxx-strategy>; default means the database is not split |
| default-table-strategy-ref (?)    | Attribute | Default table sharding strategy,which corresponds to id of \<sharding:xxx-strategy>;  default means the database is not split |
| default-key-generator (?)         | Attribute | Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE/UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator` |
| encrypt-rule (?)                  | Tag       | Encrypt rule                                                 |

#### \<sharding:table-rules />

| *Name*         | *Type* | *Description*                                 |
| -------------- | ------ | --------------------------------------------- |
| table-rule (+) | Tag    | Configuration objects of table sharding rules |

#### \<sharding:table-rule />

| *Name*                    | *Type*    | *Description*                                                |
| ------------------------- | --------- | ------------------------------------------------------------ |
| logic-table               | Attribute | Name of logic table                                          |
| actual-data-nodes (?)     | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| database-strategy-ref (?) | Attribute | Databases sharding strategy, use default databases sharding strategy if absent |
| table-strategy-ref (?)    | Attribute | Tables sharding strategy, use default tables sharding strategy if absent |
| key-generator (?)         | Attribute | Key generator, use default key generator if absent.          |

#### \<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

#### \<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                                |
| ------------ | --------- | ------------------------------------------------------------ |
| logic-tables | Attribute | Logic table name bound with rules; multiple tables are separated by commas |

#### \<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*         |
| ------------------------ | ------ | --------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rules |

#### \<sharding:broadcast-table-rule />

| *Name*         | *Type*    | *Description* |
| -------------- | --------- | ------------- |
| tableAttribute | Attribute | Name of table |

#### \<sharding:standard-strategy />

| *Name*                  | *Type*    | *Description*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| id                      | Attribute | Spring Bean Id                                               |
| sharding-column         | Attribute | Sharding column name                                         |
| precise-algorithm-ref   | Attribute | Precise algorithm reference, applied in `=` and `IN`; the class needs to implement `PreciseShardingAlgorithm` interface |
| range-algorithm-ref (?) | Attribute | Range algorithm reference, applied in `BETWEEN`; the class needs to implement `RangeShardingAlgorithm` interface |

#### \<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Spring Bean Id                                               |
| sharding-columns | Attribute | Sharding column name; multiple columns are separated by commas |
| algorithm-ref    | Attribute | Complex sharding algorithm reference; the class needs to implement `ComplexKeysShardingAlgorithm` interface |

#### \<sharding:inline-strategy />

| *Name*               | *Type*    | *Description*                                                |
| -------------------- | --------- | ------------------------------------------------------------ |
| id                   | Attribute | Spring Bean Id                                               |
| sharding-column      | Attribute | Sharding column name                                         |
| algorithm-expression | Attribute | Sharding algorithm inline expression, which needs to conform to groovy statements |

#### \<sharding:hint-database-strategy />

| *Name*        | *Type*    | *Description*                                                |
| ------------- | --------- | ------------------------------------------------------------ |
| id            | Attribute | Spring Bean Id                                               |
| algorithm-ref | Attribute | Hint sharding algorithm; the class needs to implement `HintShardingAlgorithm` interface |

#### \<sharding:none-strategy />

| *Name* | *Type*    | *Description*  |
| ------ | --------- | -------------- |
| id     | Attribute | Spring Bean Id |

#### \<sharding:key-generator />

| *Name*    | *Type*    | *Explanation*                                                |
| --------- | --------- | ------------------------------------------------------------ |
| column    | Attribute | Auto-increment column name                                   |
| type      | Attribute | Auto-increment key generator `Type`; self-defined generator or internal Type generator (SNOWFLAKE/UUID) can both be selected |
| props-ref | Attribute | The Property configuration reference of key generators       |

#### Properties

Property configuration that can include these properties of these key generators.

##### SNOWFLAKE

| *Name*                                              | *DataType* | *Explanation*                                                                                                                                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        |   long     | The unique id for working machine, the default value is `0`                                                                                                                                                                    |
| max.tolerate.time.difference.milliseconds (?)        |   long     | The max tolerate time for different server's time difference in milliseconds, the default value is `10`                                                                                                                         |
| max.vibration.offset (?)                             |    int     | The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |

#### \<sharding:encrypt-rules />

| *Name*             | *Type* | *Explanation*    |
| -------------------| ------ | ---------------- |
| encryptor-rule (+) | Tag    | Encryptor rule   |

#### \<sharding:encrypt-rule />

| *Name*                  | *Type*    | *Description*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| encrypt:encrypt-rule(?) | Tag       | Encrypt rule                                                  

#### \<sharding:props />

| *Name*                             | *Type*    | *Explanation*                                                |
| ---------------------------------- | --------- | ------------------------------------------------------------ |
| sql.show (?)                       | Attribute | Show SQL or not; default value: false                        |
| executor.size (?)                  | Attribute | Executing thread number; default value: CPU core number      |
| max.connections.size.per.query (?) | Attribute | The maximum connection number that each physical database allocates to each query; default value: 1 |
| check.table.metadata.enabled (?)   | Attribute | Whether to check meta-data consistency of sharding table when it initializes; default value: false  |
| query.with.cipher.column (?)       | Attribute | When there is a plainColumn, use cipherColumn or not to query, default value: true                  |

### Read-Write Split

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *Name*                  | *Type*    | *Explanation*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| id                      | Attribute | Spring Bean id                                               |
| master-data-source-name | Attribute | Bean id of data source in master database                    |
| slave-data-source-names | Attribute | Bean id list of data source in slave database; multiple Beans are separated by commas |
| strategy-ref (?)        | Attribute | Slave database load balance algorithm reference; the class needs to implement `MasterSlaveLoadBalanceAlgorithm` interface |
| strategy-type (?)       | Attribute | Load balance algorithm type of slave database; optional value: ROUND_ROBIN and RANDOM; if there is `load-balance-algorithm-class-name`, the configuration can be omitted |
| config-map (?)          | Tag       | Users' self-defined configurations                           |
| props (?)               | Tag       | Attribute configurations                                     |

#### \<master-slave:props />

| *Name*                             | *Type*    | *Explanation*                                                |
| ---------------------------------- | --------- | ------------------------------------------------------------ |
| sql.show (?)                       | Attribute | Show SQL or not; default value: false                        |
| executor.size (?)                  | Attribute | Executing thread number; default value: CPU core number      |
| max.connections.size.per.query (?) | Attribute | The maximum connection number that each physical database allocates to each query; default value: 1 |
| check.table.metadata.enabled (?)   | Attribute | Whether to check meta-data consistency of sharding table when it initializes; default value: false |

#### \<master-slave:load-balance-algorithm />
4.0.0-RC2 version added

| *Name*                             | *Type*    | *Explanation*                                                |
| ---------------------------------- | --------- | ------------------------------------------------------------ |
| id                                 | Attribute | Spring Bean Id                                               |
| type                               | Attribute | Type of load balance algorithm, 'RANDOM'或'ROUND_ROBIN', support custom extension|
| props-ref (?)                      | Attribute | Properties of load balance algorithm                         |

### Data Masking

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd

#### \<encrypt:data-source />

| *Name*                  | *Type*    | *Type*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| id                      | Attribute | Spring Bean Id                                               |
| data-source-name        | Attribute | Encrypt data source Bean Id                                  |
| props (?)               | Tag       | Attribute configurations                                     |

#### \<encrypt:encryptors />

| *Name*               | *Type*  | *Type*                                                    |
| -------------------- | ------- | --------------------------------------------------------- |
| encryptor(+)         | Tag     | Encryptor configuration                                   |

#### \<encrypt:encryptor />

| *Name*                  | *Type*    | *Explanation*                                               |
| ----------------------- | --------- | ----------------------------------------------------------- |
| id                      | Attribute | Names of Encryptor                                          |
| type                    | Attribute | Types of Encryptor, including MD5/AES or customize type     |
| props-re                | Attribute | Attribute configurations                                    |

#### \<encrypt:tables />

| *Name*                  | *Type* | *Explanation*                                             |
| ----------------------- | -----  | --------------------------------------------------------- |
| table(+)                | Tag    | Encrypt table configuration                               |

#### \<encrypt:table />

| *Name*                  | *Type* | *Explanation*                                            |
| ----------------------- | ------ | ---------------------------------------------------------|
| column(+)               | Tag    | Encrypt column configuration                             |

#### \<encrypt:column />

| *Name*                   | *Type*   | *Explanation*                                                                                       |
| ----------------------- | --------- | --------------------------------------------------------------------------------------------------- |
| logic-column            | Attribute | Logic column name                                                                                   |
| plain-column            | Attribute | Plain column name                                                                                   |
| cipher-column           | Attribute | Cipher column name                                                                                  |
| assisted-query-columns  | Attribute | AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data|

#### \<encrypt:props />

| *Name*                             | *Type*    | *Explanation*                                                |
| ---------------------------------- | --------- | ------------------------------------------------------------ |
| sql.show (?)                       | Attribute | Show SQL or not; default value: false                        |
| query.with.cipher.column (?)       | Attribute | When there is a plainColumn, use cipherColumn or not to query, default value: true                  |

### Data Sharding + Orchestration

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:master-slave-data-source />

| *Name*              | *Type*    | *Explanation*                                                |
| ------------------- | --------- | ------------------------------------------------------------ |
| id                  | Attribute | ID                                                           |
| data-source-ref (?) | Attribute | Orchestrated database id                                     |
| instance-ref        | Attribute | The id of orchestration instance                                          |
| overwrite           | Attribute | Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations; default means not to overwrite |

### Read-Write Split + Orchestration

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:sharding-data-source />

| *Name*              | *Type*    | *Description*                                               |
| ------------------- | --------- | ----------------------------------------------------------- |
| id                  | Attribute | ID                                                          |
| data-source-ref (?) | Attribute | The id of data source to be orchestrated                    |
| instance-ref        | Attribute | The id of orchestration instance                                   |
| overwrite           | Attribute | Use local configuration to overwrite config center or not |

### Data Masking + Orchestration

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:encrypt-data-source />

| *Name*              | *Type*    | *Description*                                               |
| ------------------- | --------- | ----------------------------------------------------------- |
| id                  | Attribute | ID                                                          |
| data-source-ref (?) | Attribute | The id of data source to be orchestrated                    |
| instance-ref        | Attribute | The id of orchestration instance                                   |
| overwrite           | Attribute | Use local configuration to overwrite config center or not |


### Orchestration instance

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:instance />

| *Name*                             | *Type*    | *Description*                                                                   |
| ---------------------------------- | --------- | ------------------------------------------------------------------------------- |
| id                                 | Attribute | Spring Bean Id of center                                               |
| instance-type                      | Attribute | Center instance type. Example:zookeeper                                         |
| orchestration-type                 | Attribute | The type of orchestration center: config_center or registry_center or metadata_center  |
| server-lists                       | Attribute | Center servers list, multiple split as comma. Example: host1:2181,host2:2181  |
| namespace (?)                      | Attribute | Namespace of center                                                           |
| props-ref (?)                      | Attribute | Other customize properties of registry center                                   |
