+++
title = "Mixed Rules"
weight = 6
+++

## Configuration Item Explanation
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:readwrite-splitting="http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.springframework.org/schema/context 
                           http://www.springframework.org/schema/context/spring-context.xsd
                           http://www.springframework.org/schema/tx 
                           http://www.springframework.org/schema/tx/spring-tx.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting
                           http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd
                           ">
						   
    <bean id="write_ds0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/write_ds?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="read_ds0_0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
    <bean id="read_ds0_1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
	<bean id="write_ds1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
	
	<bean id="read_ds1_0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
    <bean id="read_ds1_1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
	
	<!-- load balance algorithm configuration for readwrite-splitting -->
    <readwrite-splitting:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    
	<!-- readwrite-splitting rule configuration -->
    <readwrite-splitting:rule id="readWriteSplittingRule">
        <readwrite-splitting:data-source-rule id="ds_0" write-data-source-name="write_ds0" read-data-source-names="read_ds0_0, read_ds0_1" load-balance-algorithm-ref="randomStrategy" />
		<readwrite-splitting:data-source-rule id="ds_1" write-data-source-name="write_ds1" read-data-source-names="read_ds1_0, read_ds1_1" load-balance-algorithm-ref="randomStrategy" />
    </readwrite-splitting:rule>
    
	<!-- sharding strategy configuration -->
	<sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="inlineDatabaseStrategyAlgorithm" />
    <sharding:standard-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-ref="inlineOrderTableStrategyAlgorithm" />
    <sharding:standard-strategy id="orderItemTableStrategy" sharding-column="order_item_id" algorithm-ref="inlineOrderItemTableStrategyAlgorithm" />

    <sharding:sharding-algorithm id="inlineDatabaseStrategyAlgorithm" type="INLINE">
        <props>
            <!-- the expression enumeration is the logical data source name of the readwrite-splitting configuration -->
            <prop key="algorithm-expression">ds_${user_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    <sharding:sharding-algorithm id="inlineOrderTableStrategyAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">t_order_${order_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    <sharding:sharding-algorithm id="inlineOrderItemTableStrategyAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">t_order_item_${order_item_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
	
	<!-- sharding rule configuration -->	
	<sharding:rule id="shardingRule">
        <sharding:table-rules>
            <!-- the expression 'ds_${0..1}' enumeration is the logical data source name of the readwrite-splitting configuration  -->
            <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generate-strategy-ref="orderKeyGenerator"/>
            <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generate-strategy-ref="itemKeyGenerator"/>
        </sharding:table-rules>
        <sharding:binding-table-rules>
            <sharding:binding-table-rule logic-tables="t_order, t_order_item"/>
        </sharding:binding-table-rules>
        <sharding:broadcast-table-rules>
            <sharding:broadcast-table-rule table="t_address"/>
        </sharding:broadcast-table-rules>
    </sharding:rule>
    
    <!-- data encrypt configuration -->
    <encrypt:encrypt-algorithm id="name_encryptor" type="AES">
        <props>
            <prop key="aes-key-value">123456</prop>
        </props>
    </encrypt:encrypt-algorithm>
    <encrypt:encrypt-algorithm id="pwd_encryptor" type="assistedTest" />
    
    <encrypt:rule id="encryptRule">
        <encrypt:table name="t_user">
            <encrypt:column logic-column="user_name" cipher-column="user_name" plain-column="user_name_plain" encrypt-algorithm-ref="name_encryptor" />
            <encrypt:column logic-column="pwd" cipher-column="pwd" assisted-query-column="assisted_query_pwd" encrypt-algorithm-ref="pwd_encryptor" />
        </encrypt:table>
    </encrypt:rule>
    
	<!-- datasource configuration -->
	<!-- the element data-source-names's value is all of the datasource name -->
    <shardingsphere:data-source id="readQueryDataSource" data-source-names="write_ds0, read_ds0_0, read_ds0_1, write_ds1, read_ds1_0, read_ds1_1" 
        rule-refs="readWriteSplittingRule, shardingRule, encryptRule" >
        <props>
            <prop key="sql-show">true</prop>
        </props>
    </shardingsphere:data-source>
</beans>
```
