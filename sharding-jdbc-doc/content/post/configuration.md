+++
date = "2016-03-25T16:14:21+08:00"
title = "Spring命名空间和Yaml配置"
weight = 2
+++

# Yaml配置

## 引入maven依赖

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-yaml</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## Java示例

```java
    DataSource dataSource = new YamlShardingDataSource(yamlFile);
```

## 配置示例

```yaml
dataSource:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dbtbl_0
    username: root
    password: 
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dbtbl_1
    username: root
    password: 
    
tables:
  config:
    actualTables: t_config
    
  t_order: 
    actualTables: t_order_${0..7}
    databaseStrategy: &db001
      shardingColumns: order_id
      algorithmClassName: com.dangdang.ddframe.rdb.sharding.config.yaml.algorithm.SingleAlgorithm
    tableStrategy: &table001
      shardingColumns: id
      algorithmExpression: t_order_${id.longValue() % 2}
  
  #绑定表中其余的表的策略与t_order的策略相同
  t_order_item:
    actualTables: t_order_item_${0..7}
    databaseStrategy: *db001
    tableStrategy: *table001

bindingTables:
  - tableNames: t_order,t_order_item
  - tableNames: ...

defaultDatabaseStrategy:
  shardingColumns: order_id, user_id
  algorithmExpression: t_order_${id.longValue() % 2}

defaultTableStrategy:
  shardingColumns: id, order_id
  algorithmClassName: com.dangdang.ddframe.rdb.sharding.config.yaml.algorithm.MultiAlgorithm

props:
  metrics.enable: false
```

## 配置项说明

```yaml
dataSource: 数据源配置
  <data_source_name> 可配置多个: !!数据库连接池实现类
    driverClassName: 数据库驱动类名
    url: 数据库url连接
    username: 数据库用户名
    password: 数据库密码
    ... 数据库连接池的其它属性
  
  tables: 分库分表配置，可配置多个logic_table_name
    <logic_table_name>: 逻辑表名
        actualTables: 真实表名，多个表以逗号分隔，支持inline表达式
        databaseStrategy: 分库策略
            shardingColumns: 分片列名，多个列以逗号分隔
            algorithmClassName: 分库算法全类名，与algorithmExpression出现一个即可
            algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
        tableStrategy: 分表策略
            shardingColumns: 分片列名，多个列以逗号分隔
            algorithmClassName: 分库算法全类名，与algorithmExpression出现一个即可
            algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
  bindingTables: 绑定表列表
  - tableNames: 逻辑表名列表，多个<logic_table_name>以逗号分隔
  
defaultDatabaseStrategy: 默认数据库分片策略
  shardingColumns: 分片列名，多个列以逗号分隔
  algorithmClassName: 分库算法全类名，与algorithmExpression出现一个即可
  algorithmExpression: 分库算法表达式，与algorithmClassName出现一个即可
  
defaultTableStrategy: 默认数据表分片策略
  shardingColumns: 分片列名，多个列以逗号分隔
  algorithmClassName: 分表算法全类名，与algorithmExpression出现一个即可
  algorithmExpression: 分表算法表达式，与algorithmClassName出现一个即可

props: 属性配置(可选)
    metrics.enable: 是否开启度量采集，默认值为false不开启
    metrics.second.period: 度量输出周期，单位为秒，默认为1秒
    metrics.package.name: 度量输出在日志中的标识名称，默认为com.dangdang.ddframe.rdb.sharding.metrics
    parallelExecutor.worker.minIdleSize: 最小空闲工作线程数量，默认为0
    parallelExecutor.worker.maxSize: 最大工作线程数量，默认为CPU核数乘2
    parallelExecutor.worker.maxIdleTimeout: 工作线程空闲时超时时间，单位为秒，默认为60秒
```

### Yaml格式特别说明
`!!` 表示实现类

`&` 表示变量定义

`\*` 表示变量引用

`\-` 表示多个

# Spring命名空间配置

## 引入maven依赖

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 配置示例
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
        <rdb:sharding-rule data-sources="dbtbl_0,dbtbl_1">
            <rdb:table-rules>
                <rdb:table-rule logic-table="t_order" actual-tables="t_order_${0..3}" table-strategy="tableStrategy"/>
                <rdb:table-rule logic-table="t_order_item" actual-tables="t_order_item_${0..3}" database-strategy="databaseStrategy" table-strategy="tableStrategy"/>
            </rdb:table-rules>
        </rdb:sharding-rule>
        <rdb:binding-table-rules>
            <rdb:binding-table-rule logic-tables="t_order, t_order_item"/>
        </rdb:binding-table-rules>
        <rdb:default-database-strategy sharding-columns="user_id" algorithm-expression="dbtbl_${id % 2 + 1}"/>
        <rdb:props>
            <prop key="metrics.enable">${metrics.enable}</prop>
        </rdb:props>
    </rdb:data-source>
</beans>
```
## 标签说明

### \<rdb:data-source/\>

定义sharding-jdbc数据源

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| id                            | 属性         |  String     |   是  | Spring Bean ID |
| sharding-rule                 | 标签         |   -         |   是  | 分片规则 |
| binding-table-rules`?`        | 标签         |   -         |   是  | 绑定表规则 |  
| default-database-strategy`?`  | 标签         |   -         |   是  | 默认分库策略 |  
| default-table-strategy`?`     | 标签         |   -         |   是  | 默认分表策略 |
| props`?`                      | 标签         |   -         |   是  | 相关属性配置 |

### \<rdb:sharding-rule/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     | ---------  | ------| -----   |
| data-sources                  | 属性         | String      |   是  | 数据源Bean列表，多个Bean以逗号分隔 |
| table-rules                   | 标签         |   -         |   是  | 分片规则列表 |

### \<rdb:table-rules/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     | ---------  | ------| -----   |
| table-rule`+`                 | 标签         |   -         |   是  | 分片规则 |

### \<rdb:table-rule/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| logic-table                   | 属性         |  String     |   是  | 逻辑表名 |
| sharding-columns              | 属性         |  String     |   是  | 分片列名，多个列以逗号分隔 |
| database-strategy             | 属性         |  String     |   是  | 分库策略，对应`<rdb:strategy>`中分库策略id |
| table-strategy                | 属性         |  String     |   是  | 分表策略，对应`<rdb:strategy>`中分表策略id |

### \<rdb:binding-table-rules/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| binding-table-rule           | 标签         |   -         |   是  | 绑定规则 |

### \<rdb:binding-table-rule/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| logic-tables                  | 属性         |  String     |   是  | 逻辑表名，多个表名以逗号分隔 |

### \<rdb:default-database-strategy/>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| sharding-columns              | 属性         |  String     |   是  | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否  | 默认分库算法全类名，与`algorithm-expression`有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否  | 默认分库算法表达式，与`algorithm-class`有且仅有一个出现 |

### \<rdb:default-table-strategy/\>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| sharding-columns              | 属性         |  String     |   是  | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否  | 默认分表算法全类名，与`algorithm-expression`有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否  | 默认分表算法表达式，与`algorithm-class`有且仅有一个出现 |

### \<rdb:strategy/\>`*`

定义数据分库或分表策略

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| id                            | 属性         |  String     |   是  | Spring Bean ID |
| sharding-columns              | 属性         |  String     |   是  | 分片列名，多个列以逗号分隔 |
| algorithm-class               | 属性         |  Class      |   否  | 分库或分表算法全类名，与`algorithm-expression`有且仅有一个出现 |
| algorithm-expression          | 属性         |  String     |   否  | 分库或分表算法表达式，与`algorithm-class`有且仅有一个出现 |

### \<rdb:props/\>

| *名称*                         | 类型         | *数据类型*  |  *必填*| *说明*  |
| --------------------          | --------     |  --------- | ------| -----   |
| metrics.enable                | 属性         |  boolean   |   否  | 是否开启度量采集，默认为false不开启 |
| metrics.second.period         | 属性         |  String    |   否  | 度量输出周期，单位为秒 |
| metrics.package.name          | 属性         |  String    |   否  | 度量输出在日志中的标识名称 |
| parallelExecutor.worker.minIdleSize| 属性         |  int    |   否  | 最小空闲工作线程数量 |
| parallelExecutor.worker.maxSize| 属性         |  int    |   否  | 最大工作线程数量 |
| parallelExecutor.worker.maxIdleTimeout| 属性         |  int    |   否  | 工作线程空闲时超时时间，默认以秒为单位 |

### Spring格式特别说明
如需使用inline表达式，需配置`ignore-unresolvable`为`true`，否则placeholder会把inline表达式当成属性key值导致出错. 


# 分片算法表达式语法说明

## inline表达式特别说明
`${begin..end}` 表示范围区间

`${[unit1, unit2, unitX]}` 表示枚举值

## 字符串内嵌groovy代码
表达式本质上是一段字符串，字符串中使用`${}`来嵌入`groovy`代码。

```groovy 
data_source_${id.longValue() % 2 + 1}
```

上面的表达式中`data_source_`是字符串前缀，`id.longValue() % 2 + 1`是`groovy`代码。

## 分区键值获取
`groovy`代码中可以使用分区键的名字直接获取表达式的值对象。

该对象是`com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ShardingValueWrapper`类型的对象。

该类中提供了一些方法，方便数据类型的转换。包装的原始类型一般为`Number`，`java.util.Date`，`String` 三种类型。使用类中的方法可以将这三种类型转换为需要的其他类型。

方法列表如下：

| *方法名*                  | *入参*         | *返回类型* |
| ------------------------ | -------------- | --------- |
| longValue()              |                | long      |
| doubleValue()            |                | double    |
| dateValue(String format) | 时间格式化表达式 | java.util.Date |
| dateValue()              |                | java.util.Date |
| toString(String format)  | 时间格式化表达式 | String |
| toString()               |                | STring |
