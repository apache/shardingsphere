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
       xmlns:replica-query="http://shardingsphere.apache.org/schema/shardingsphere/replica-query"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.springframework.org/schema/context 
                           http://www.springframework.org/schema/context/spring-context.xsd
                           http://www.springframework.org/schema/tx 
                           http://www.springframework.org/schema/tx/spring-tx.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/replica-query
                           http://shardingsphere.apache.org/schema/shardingsphere/replica-query/replica-query.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt
                           http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd
                           ">
						   
    <bean id="primary_ds0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/primary_ds?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="replica_ds0_0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
    <bean id="replica_ds0_1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
	<bean id="primary_ds1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
	
	<bean id="replica_ds1_0" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
    
    <bean id="replica_ds1_1" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <!-- ...Omit specific configuration. -->
    </bean>
	
	<!-- load balance algorithm configuration for replica-query -->
    <replica-query:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    
	<!-- replica-query rule configuration -->
    <replica-query:rule id="replicaQueryRuleDs0">
        <replica-query:data-source-rule id="ds_0" primary-data-source-name="primary_ds0" replica-data-source-names="replica_ds0_0, replica_ds0_1" load-balance-algorithm-ref="randomStrategy" />
		<replica-query:data-source-rule id="ds_1" primary-data-source-name="primary_ds1" replica-data-source-names="replica_ds1_0, replica_ds1_1" load-balance-algorithm-ref="randomStrategy" />
    </replica-query:rule>
    
	<!-- sharding strategy configuration -->
	<sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="inlineDatabaseStrategyAlgorithm" />
    <sharding:standard-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-ref="inlineOrderTableStrategyAlgorithm" />
    <sharding:standard-strategy id="orderItemTableStrategy" sharding-column="order_item_id" algorithm-ref="inlineOrderItemTableStrategyAlgorithm" />

    <sharding:sharding-algorithm id="inlineDatabaseStrategyAlgorithm" type="INLINE">
        <props>
            <!-- the expression enumeration is the logical data source name of the replica-query configuration -->
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
            <!-- the expression 'ds_${0..1}' enumeration is the logical data source name of the replica-query configuration  -->
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
    <shardingsphere:data-source id="replicaQueryDataSource" data-source-names="primary_ds0, replica_ds0_0, replica_ds0_1, primary_ds1, replica_ds1_0, replica_ds1_1" 
        rule-refs="replicaQueryRule, shardingRule, encryptRule" >
        <props>
            <prop key="query-with-cipher-column">true</prop>
            <prop key="sql-show">true</prop>
        </props>
    </shardingsphere:data-source>
  
</beans>
```
