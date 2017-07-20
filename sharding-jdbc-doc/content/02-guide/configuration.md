+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "配置手册"
weight = 5
prev = "/02-guide/master-slave/"
next = "/02-guide/hint-sharding-value/"

+++

## YAML配置

## 引入maven依赖

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-config-yaml</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Java示例

```java
    DataSource dataSource = new YamlShardingDataSource(yamlFile);
```

### 配置示例

```yaml
dataSource:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 
    
defaultDataSourceName: ds_0

tables:
  config:
    actualTables: t_config
    
  t_order: 
    actualTables: t_order_${0..1}
    tableStrategy: &table001
      shardingColumns: order_id
      algorithmExpression: t_order_${order_id.longValue() % 2}
  
  #绑定表中其余的表的策略与t_order的策略相同
  t_order_item:
    actualTables: t_order_item_${0..1}
    tableStrategy: *table001

bindingTables:
  - tableNames: t_order,t_order_item
  - tableNames: ...

defaultDatabaseStrategy:
  shardingColumns: none
  algorithmClassName: com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm

props:
  metrics.enable: false
  
```

### 配置项说明

```yaml
dataSource: 数据源配置
  <data_source_name> 可配置多个: !!数据库连接池实现类
    driverClassName: 数据库驱动类名
    url: 数据库url连接
    username: 数据库用户名
    password: 数据库密码
    ... 数据库连接池的其它属性
    
defaultDataSourceName: 默认数据源，未配置分片规则的表将通过默认数据源定位
  
tables: 分库分表配置，可配置多个logic_table_name
    <logic_table_name>: 逻辑表名
        dynamic: 是否为动态表
        actualTables: 真实表名，多个表以逗号分隔，支持inline表达式，指定数据源需要加前缀，不加前缀为默认数据源。不填写表示为只分库不分表或动态表(需要配置dynamic=true)。
        dataSourceNames: 数据源名称，多个数据源用逗号分隔，支持inline表达式。不填写表示使用全部数据源
        databaseStrategy: 分库策略
            shardingColumns: 分片列名，多个列以逗号分隔
            algorithmClassName: 分库算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithmExpression出现一个即可
            algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
        tableStrategy: 分表策略
            shardingColumns: 分片列名，多个列以逗号分隔
            algorithmClassName: 分库算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithmExpression出现一个即可
            algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
  bindingTables: 绑定表列表
  - tableNames: 逻辑表名列表，多个<logic_table_name>以逗号分隔
  
defaultDatabaseStrategy: 默认数据库分片策略
  shardingColumns: 分片列名，多个列以逗号分隔
  algorithmClassName: 分库算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithmExpression出现一个即可
  algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
  
defaultTableStrategy: 默认数据表分片策略
  shardingColumns: 分片列名，多个列以逗号分隔
  algorithmClassName: 分表算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithmExpression出现一个即可
  algorithmExpression: 分表算法表达式，与algorithmClassName出现一个即可

props: 属性配置(可选)
    metrics.enable: 是否开启度量采集，默认值: false
    sql.show: 是否开启SQL显示，默认值: false
    metrics.millisecond.period: 度量输出周期，单位: 毫秒，默认值: 30000毫秒
    
    executor.min.idle.size: 最小空闲工作线程数量，默认值: 0
    executor.max.size: 最大工作线程数量，默认值: CPU核数乘2
    executor.max.idle.timeout.millisecond: 工作线程空闲时超时时间，单位: 毫秒，默认值: 60000毫秒
```

#### YAML格式特别说明
!! 表示实现类

& 表示变量定义

* 表示变量引用

- 表示多个

## Spring命名空间配置

### 引入maven依赖

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-config-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 配置示例
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
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true"/>
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <rdb:strategy id="databaseStrategy" sharding-columns="user_id" algorithm-class="com.dangdang.ddframe.rdb.sharding.spring.algorithm.SingleKeyModuloDatabaseShardingAlgorithm"/>
    <rdb:strategy id="tableStrategy" sharding-columns="order_id" algorithm-class="com.dangdang.ddframe.rdb.sharding.spring.algorithm.SingleKeyModuloTableShardingAlgorithm"/>

    <rdb:data-source id="shardingDataSource">
        <rdb:sharding-rule data-sources="dbtbl_0,dbtbl_1" default-data-source="dbtbl_0">
            <rdb:table-rules>
                <rdb:table-rule logic-table="t_order" actual-tables="t_order_${0..3}" table-strategy="tableStrategy"/>
                <rdb:table-rule logic-table="t_order_item" actual-tables="t_order_item_${0..3}" database-strategy="databaseStrategy" table-strategy="tableStrategy"/>
            </rdb:table-rules>
            <rdb:binding-table-rules>
                <rdb:binding-table-rule logic-tables="t_order, t_order_item"/>
            </rdb:binding-table-rules>
            <rdb:default-database-strategy sharding-columns="none" algorithm-class="com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm"/>
        </rdb:sharding-rule>
        <rdb:props>
            <prop key="metrics.enable">true</prop>
        </rdb:props>
    </rdb:data-source>
</beans>
```
### 标签说明

#### \<rdb:data-source/\>

定义sharding-jdbc数据源

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*         |
| ----------------------------- | ------------ |  --------- | ------ | -------------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID |
| sharding-rule                 | 标签         |   -         |   是   | 分片规则        |
| binding-table-rules?        | 标签         |   -         |   是   | 绑定表规则       |
| default-database-strategy?  | 标签         |   -         |   是   | 默认分库策略     |
| default-table-strategy?     | 标签         |   -         |   是   | 默认分表策略     |
| props?                      | 标签         |   -         |   是   | 相关属性配置     |

#### \<rdb:sharding-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                                                    |
| ----------------------------- | ------------ | ---------- | ------ | -------------------------------------------------------- |
| data-sources                  | 属性         | String      |   是   | 数据源Bean列表，多个Bean以逗号分隔                           |
| default-data-source           | 属性         | String      |   否   | 默认数据源名称，未配置分片规则的表将通过默认数据源定位           |
| table-rules                   | 标签         |   -         |   是   | 分片规则列表                                               |

#### \<rdb:table-rules/>

| *名称*                         | *类型*      | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ----------- | ---------- | ------ | ------- |
| table-rule+                 | 标签         |   -         |   是  | 分片规则 |

#### \<rdb:table-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*  |
| --------------------          | ------------ | ---------- | ------ | ------- |
| logic-table                   | 属性         |  String     |   是   | 逻辑表名 |
| dynamic                       | 属性         |  boolean    |   否   | 是否动态表 |
| actual-tables                 | 属性         |  String     |   否   | 真实表名，多个表以逗号分隔，支持inline表达式，指定数据源需要加前缀，不加前缀为默认数据源 指定数据源需要加前缀，不加前缀为默认数据源。不填写表示为只分库不分表或动态表(需要配置dynamic=true) |
| data-source-names             | 属性         |  String     |   否   | 数据源名称，多个数据源用逗号分隔，支持inline表达式。不填写表示使用全部数据源                |
| database-strategy             | 属性         |  String     |   否   | 分库策略，对应<rdb:strategy>中分库策略id, 如果不填需配置<rdb:default-database-strategy/> |
| table-strategy                | 属性         |  String     |   否   | 分表策略，对应<rdb:strategy>中分表策略id, 如果不填需配置<rdb:default-table-strategy/>    |

#### \<rdb:binding-table-rules/>

| *名称*                         | *类型*      | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ----------- |  --------- | ------ | ------- |
| binding-table-rule            | 标签         |   -         |   是  | 绑定规则 |

#### \<rdb:binding-table-rule/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*                   |
| ----------------------------- | ------------ | ---------- | ------ | ------------------------ |
| logic-tables                  | 属性         |  String     |   是   | 逻辑表名，多个表名以逗号分隔 |

#### \<rdb:default-database-strategy/>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ------------ | ---------- | ------ | ------- |
| sharding-columns              | 属性         |  String     |   是  | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否  | 默认分库算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithm-expression有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否  | 默认分库算法表达式，与algorithm-class有且仅有一个出现 |

#### \<rdb:default-table-strategy/\>

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ------------ |  --------- | ------ | ------- |
| sharding-columns              | 属性         |  String     |   是   | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否   | 默认分表算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithm-expression有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否   | 默认分表算法表达式，与algorithm-class有且仅有一个出现 |

#### \<rdb:strategy/\>*

定义数据分库或分表策略

| *名称*                         | *类型*       | *数据类型*  |  *必填* | *说明*  |
| ----------------------------- | ------------ | ---------- | ------ | ------- |
| id                            | 属性         |  String     |   是   | Spring Bean ID |
| sharding-columns              | 属性         |  String     |   是   | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否   | 分库或分表算法全类名，该类需使用默认的构造器或者提供无参数的构造器，与algorithm-expression有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否   | 分库或分表算法表达式，与algorithm-class有且仅有一个出现 |

#### \<rdb:props/\>

| *名称*                                | *类型*       | *数据类型*  | *必填* | *说明*                              |
| ------------------------------------ | ------------ | ---------- | ----- | ----------------------------------- |
| metrics.enable                       | 属性         |  boolean   |   否   | 是否开启度量采集，默认为false不开启     |
| sql.show                             | 属性         |  boolean   |   是   | 是否开启SQL显示，默认为true开启     |
| metrics.millisecond.period           | 属性         |  String    |   否   | 度量输出周期，单位为毫秒               |
| executor.min.idle.size               | 属性         |  int       |   否   | 最小空闲工作线程数量                  |
| executor.max.size                    | 属性         |  int       |   否   | 最大工作线程数量                      |
| executor.max.idle.timeout.millisecond| 属性         |  int       |   否   | 工作线程空闲时超时时间，默认以毫秒为单位 |

#### Spring格式特别说明
如需使用inline表达式，需配置ignore-unresolvable为true，否则placeholder会把inline表达式当成属性key值导致出错. 


## 分片算法表达式语法说明

### inline表达式特别说明
${begin..end} 表示范围区间

${[unit1, unit2, unitX]} 表示枚举值

inline表达式中连续多个${...}表达式，整个inline最终的结果将会根据每个子表达式的结果进行笛卡尔组合，例如正式表inline表达式如下：
```groovy
dbtbl_${[online, offline]}_${1..3}
```
最终会解析为dbtbl_online_1，dbtbl_online_2，dbtbl_online_3，dbtbl_offline_1，dbtbl_offline_2和dbtbl_ offline_3这6张表。

### 字符串内嵌groovy代码
表达式本质上是一段字符串，字符串中使用${}来嵌入groovy代码。

```groovy 
data_source_${id.longValue() % 2 + 1}
```

上面的表达式中data_source_是字符串前缀，id.longValue() % 2 + 1是groovy代码。

### 分区键值获取
groovy代码中可以使用分区键的名字直接获取表达式的值对象。

该对象是com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ShardingValueWrapper类型的对象。

该类中提供了一些方法，方便数据类型的转换。包装的原始类型一般为Number，java.util.Date，String 三种类型。使用类中的方法可以将这三种类型转换为需要的其他类型。

方法列表如下：

| *方法名*                  | *参数*         | *返回类型*      |
| ------------------------ | -------------- | -------------- |
| longValue()              |                | long           |
| doubleValue()            |                | double         |
| dateValue(String format) | 时间格式化表达式 | java.util.Date |
| dateValue()              |                | java.util.Date |
| toString(String format)  | 时间格式化表达式 | String         |
| toString()               |                | String         |
