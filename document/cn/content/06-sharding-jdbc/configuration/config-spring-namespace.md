+++
toc = true
title = "Spring命名空间配置"
weight = 4
+++

## Spring命名空间配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 配置示例

#### 数据分片

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven/>
    
    <bean id="demo_ds_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="demo_ds_0, demo_ds_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```
##### 标签说明

##### 分库分表

##### \<sharding:data-source/\>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*         |
| ----------------------------- | ------------ |  --------- | ------ | -------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID |
| sharding-rule                 | 标签         |   -         |   是   | 分片规则        |
| config-map?                   | 标签         |   -         |   否   |         配置映射关系|
| props?                        | 标签         |   -         |   否   | 相关属性配置     |

##### \<sharding:sharding-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                                                                |
| ----------------------------- | ------------ | ---------- | ------ | --------------------------------------------------------------------- |
| data-source-names             | 属性         | String      |   是   | 数据源Bean列表，需要配置所有需要被Sharding-JDBC管理的数据源BEAN ID（包括默认数据源），多个Bean以逗号分隔 |
| default-data-source-name?      | 属性         | String      |   否   | 默认数据源名称，未配置分片规则的表将通过默认数据源定位                        |
| default-database-strategy-ref？ | 属性         | String      |   否   | 默认分库策略，对应\<sharding:xxx-strategy>中的策略id，不填则使用不分库的策略 |
| default-table-strategy-ref？    | 属性         | String      |   否   | 默认分表策略，对应\<sharding:xxx-strategy>中的策略id，不填则使用不分表的策略 |
| default-key-generator?        | 属性          | String       |  否    |默认自增列生成策略                                                      |
| table-rules                   | 标签         |   -         |   是   | 分片规则列表                                                            |
| binding-table-rules?           | 标签         | -      | 否| 绑定表规则|

##### \<sharding:table-rules/>

| *名称*                         | *类型*      | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ----------- | ---------- | ------ | ------- |
| table-rule+                   | 标签         |   -        |   是  | 分片规则 |

##### \<sharding:table-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*  |
| --------------------          | ------------ | ---------- | ------ | ------- |
| logic-table                   | 属性         |  String     |   是   | 逻辑表名 |
| actual-data-nodes？             | 属性         |  String     |   否   | 真实数据节点，由数据源名（读写分离引用<master-slave:data-source>中的id属性） + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。不填写表示将为现有已知的数据源 + 逻辑表名称生成真实数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况。|
| database-strategy-ref？         | 属性         |  String     |   否   | 分库策略，对应\<sharding:xxx-strategy>中的策略id，不填则使用\<sharding:sharding-rule/>配置的default-database-strategy-ref   |
| table-strategy-ref？            | 属性         |  String     |   否   | 分表策略，对应\<sharding:xxx-strategy>中的略id，不填则使用\<sharding:sharding-rule/>配置的default-table-strategy-ref        |
| logic-index？                   | 属性         |  String     |   否   | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表        |
| generate-key-column-name？ | 属性| String | 否 | 自增列名|
| key-generator？ | 属性 | String | 否| 自增列生成策略|

##### \<sharding:binding-table-rules/>

| *名称*                         | *类型*      | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ----------- |  --------- | ------ | ------- |
| binding-table-rule+            | 标签         |   -         |   是  | 绑定规则 |

##### \<sharding:binding-table-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                   |
| ----------------------------- | ------------ | ---------- | ------ | ------------------------ |
| logic-tables                  | 属性         |  String     |   是   | 逻辑表名，多个表名以逗号分隔 |

##### \<sharding:standard-strategy/>

标准分片策略，用于单分片键的场景

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                            |
| ----------------------------- | ------------ | ---------- | ------ | --------------------------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID                    |
| sharding-column               | 属性         |  String     |   是   | 分片列名                           |
| precise-algorithm-ref         | 属性         |  String     |   是   | 精确的分片算法Bean引用，用于=和IN。   |
| range-algorithm-ref？         | 属性         |  String     |   否   | 范围的分片算法Bean引用，用于BETWEEN。 |


##### \<sharding:complex-strategy/>

复合分片策略，用于多分片键的场景

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                 |
| ----------------------------- | ------------ | ---------- | ------ | ---------------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID         |
| sharding-columns              | 属性         |  String     |   是   | 分片列名，多个列以逗号分隔 |
| algorithm-ref                 | 属性         |  String     |   是   | 分片算法Bean引用         |

##### \<sharding:inline-strategy/>

inline表达式分片策略

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*       |
| ----------------------------- | ------------ | ---------- | ------ | ------------ |
| id                            | 属性         |  String     |   是   | Spring Bean ID |
| sharding-column               | 属性         |  String     |   是   | 分片列名      |
| algorithm-expression          | 属性         |  String     |   是   | 分片算法表达式 |

##### \<sharding:hint-database-strategy/>

Hint方式分片策略

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*         |
| ----------------------------- | ------------ | ---------- | ------ | -------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID |
| algorithm-ref                 | 属性         |  String     |   是   | 分片算法Bean引用 |

##### \<sharding:none-strategy/>

不分片的策略

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                                              |
| ----------------------------- | ------------ | ---------- | ------ | --------------------------------------------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID |

##### \<sharding:props/\>

| *名称*                                | *类型*       | *数据类型*  | *必填* | *说明*                              |
| ------------------------------------ | ------------ | ---------- | ----- | ----------------------------------- |
| sql.show                             | 属性         |  boolean   |   是   | 是否开启SQL显示，默认为false不开启     |
| executor.size？                        | 属性         |  int       |   否   | 最大工作线程数量                      |

##### \<sharding:config-map/\>

##### 读写分离

##### \<master-slave:data-source/\>


| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                                   |
| ----------------------------- | ------------ |  --------- | ------ | ---------------------------------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID                           |
| master-data-source-name       | 属性         |   String        |   是   | 主库数据源Bean ID                         |
| slave-data-source-names       | 属性         |   String        |   是   | 从库数据源Bean列表，多个Bean以逗号分隔       |
| strategy-ref?                 | 属性         |   String         |   否   | 主从库复杂策略Bean ID，可以使用自定义复杂策略 |
| strategy-type?                | 属性         |  String     |   否   | 主从库复杂策略类型<br />可选值：ROUND_ROBIN, RANDOM<br />默认值：ROUND_ROBIN |
| config-map?                   | 标签         |   -         |   否   |         配置映射关系|

##### \<sharding:config-map/\>

##### Spring格式特别说明
如需使用inline表达式，需配置ignore-unresolvable为true，否则placeholder会把inline表达式当成属性key值导致出错. 


##### 分片算法表达式语法说明

##### inline表达式特别说明
${begin..end} 表示范围区间

${[unit1, unit2, unitX]} 表示枚举值

inline表达式中连续多个${...}表达式，整个inline最终的结果将会根据每个子表达式的结果进行笛卡尔组合，例如正式表inline表达式如下：
```groovy
dbtbl_${['online', 'offline']}_${1..3}
```
最终会解析为dbtbl_online_1，dbtbl_online_2，dbtbl_online_3，dbtbl_offline_1，dbtbl_offline_2和dbtbl_offline_3这6张表。

##### 字符串内嵌groovy代码
表达式本质上是一段字符串，字符串中使用${}来嵌入groovy代码。

```groovy 
data_source_${id % 2 + 1}
```

上面的表达式中data_source_是字符串前缀，id % 2 + 1是groovy代码。

#### 读写分离

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave  
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven/>
    
    <bean id="demo_ds_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="demo_ds_master" slave-data-source-names="demo_ds_slave_0, demo_ds_slave_1" strategy-ref="randomStrategy" />
</beans>
```

##### 使用Hint强制路由主库示例

```java
HintManager hintManager = HintManager.getInstance();
hintManager.setMasterRouteOnly();
// 继续JDBC操作
```

#### 分库分表 + 读写分离

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding"
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven/>
    
    <bean id="demo_ds_master_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />

    <master-slave:data-source id="demo_ds_ms_0" master-data-source-name="demo_ds_master_0" slave-data-source-names="demo_ds_master_0_slave_0, demo_ds_master_0_slave_1" strategy-ref="randomStrategy" />
    <master-slave:data-source id="demo_ds_ms_1" master-data-source-name="demo_ds_master_1" slave-data-source-names="demo_ds_master_1_slave_0, demo_ds_master_1_slave_1" strategy-ref="randomStrategy" />

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="demo_ds_ms_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_${order_id % 2}" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="demo_ds_ms_0,demo_ds_ms_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_ms_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_ms_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>

</beans>

```

#### 编排治理

##### Zookeeper配置示例
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:zookeeper id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
</beans>
```

##### Zookeeper标签说明

##### \<reg:zookeeper/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                         |
| server-lists                    | String | 是     |       | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                       | String | 是     |       | Zookeeper的命名空间                                                                                |
| base-sleep-time-milliseconds    | int    | 否     | 1000  | 等待重试的间隔时间的初始值<br />单位：毫秒                                                               |
| max-sleep-time-milliseconds     | int    | 否     | 3000  | 等待重试的间隔时间的最大值<br />单位：毫秒                                                               |
| max-retries                     | int    | 否     | 3     | 最大重试次数                                                                                          |
| session-timeout-milliseconds    | int    | 否     | 60000 | 会话超时时间<br />单位：毫秒                                                                           |
| connection-timeout-milliseconds | int    | 否     | 15000 | 连接超时时间<br />单位：毫秒                                                                           |
| digest                          | String | 否     |       | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证                                                      |

##### Etcd配置示例
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
        
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:etcd id="regCenter" server-lists="http://localhost:2379" time-to-live-seconds="60" timeout-milliseconds="500" max-retries="3" retry-interval-milliseconds="200"/>
</beans>
```

##### Etcd标签说明

##### \<reg:etcd/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                           |
| server-lists                    | String | 是     |       | 连接Etcd服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: http://host1:2379,http://host2:2379 |
| time-to-live-seconds            | int    | 否     | 60    | 临时节点存活时间<br />单位：秒                                                                         |
| timeout-milliseconds            | int    | 否     | 500   | 每次请求的超时时间<br />单位：毫秒                                                                      |
| max-retries                     | int    | 否     | 3     | 每次请求的最大重试次数                                                                                 |
| retry-interval-milliseconds     | int    | 否     | 200   | 重试间隔时间<br />单位：毫秒                                                                           |


#### 柔性事务

##### 事务管理器配置项

##### SoftTransactionConfiguration配置
用于配置事务管理器。


| *名称*                              | *类型*                                     | *必填* | *默认值*   | *说明*                                                                                       |
| ---------------------------------- | ------------------------------------------ | ------ | --------- | ------------------------------------------------------------------------------------------- |
| shardingDataSource                 | ShardingDataSource                         | 是     |           | 事务管理器管理的数据源                                                                         |
| syncMaxDeliveryTryTimes            | int                                        | 否     | 3         | 同步的事务送达的最大尝试次数                                                                    |
| storageType                        | enum                                       | 否     | RDB       | 事务日志存储类型。可选值: RDB,MEMORY。使用RDB类型将自动建表                                       |
| transactionLogDataSource           | DataSource                                 | 否     | null      | 存储事务日志的数据源，如果storageType为RDB则必填                                                 |
| bestEffortsDeliveryJobConfiguration| NestedBestEffortsDeliveryJobConfiguration  | 否     | null      | 最大努力送达型内嵌异步作业配置对象。如需使用，请参考NestedBestEffortsDeliveryJobConfiguration配置 |

##### NestedBestEffortsDeliveryJobConfiguration配置 (仅开发环境)
用于配置内嵌的异步作业，仅用于开发环境。生产环境应使用独立部署的作业版本。

| *名称*                              | *类型*                       | *必填* | *默认值*                    | *说明*                                                         |
| ---------------------------------- | --------------------------- | ------ | ------------------------ | --------------------------------------------------------------- |
| zookeeperPort                      | int                         | 否     | 4181                     | 内嵌的注册中心端口号                                               |
| zookeeperDataDir                   | String                      | 否     | target/test_zk_data/nano/| 内嵌的注册中心的数据存放目录                                        |
| asyncMaxDeliveryTryTimes           | int                         | 否     | 3                        | 异步的事务送达的最大尝试次数                                        |
| asyncMaxDeliveryTryDelayMillis     | long                        | 否     | 60000                    | 执行异步送达事务的延迟毫秒数，早于此间隔时间的入库事务才会被异步作业执行  |

