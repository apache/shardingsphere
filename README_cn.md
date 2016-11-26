##Sharding-JDBC - A JDBC driver for shard databases and tables

`Sharding-JDBC`是当当应用框架`ddframe`中，关系型数据库模块`dd-rdb`中分离出来的数据库水平扩展框架，即透明化数据库分库分表访问。

`Sharding-JDBC`继`dubbox`和`elastic-job`之后，是`ddframe`系列开源的第三个产品。

# Release Notes
* sharding-jdbc&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc)
* sharding-jdbc-core&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-core)
* sharding-jdbc-config-yaml&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-config-yaml/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-config-yaml)
* sharding-jdbc-config-spring&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-config-spring/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-config-spring)
* sharding-jdbc-transaction&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-transaction/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-transaction)
* sharding-jdbc-transaction-async-job&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-transaction-async-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-transaction-async-job)
* sharding-jdbc-self-id-generator&nbsp;&nbsp;[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-self-id-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc-self-id-generator)

# License
[![Hex.pm](http://dangdangdotcom.github.io/sharding-jdbc/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Build Status
[![Build Status](https://secure.travis-ci.org/dangdangdotcom/sharding-jdbc.png?branch=master)](https://travis-ci.org/dangdangdotcom/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/sharding-jdbc?branch=master)

# 主要贡献者

* 张亮 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) zhangliang@dangdang.com
* 高洪涛 [当当](http://www.dangdang.com/) gaohongtao@dangdang.com
* 曹昊 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) caohao@dangdang.com
* 岳令 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) yueling@dangdang.com

# 交流与参与

 - **讨论QQ群：** 532576663（不限于Sharding-JDBC，包括分布式，数据库相关以及其他互联网技术交流。由于QQ群已接近饱和，我们希望您在申请加群之前仔细阅读文档，并在加群申请中正确回答问题，以及在申请时写上您的姓名和公司名称。并且在入群后及时修改群名片。否则我们将有权拒绝您的入群申请。谢谢合作。）
 - **邮件组 sharding_jdbc@groups.163.com** ，如果在使用上需要帮助，或者疑似的bug，请发邮件到该邮件组中。发邮件之前请先阅读[FAQ](http://dangdangdotcom.github.io/sharding-jdbc/post/faq/)。
 - 报告确定的bug，提交增强功能建议和提交补丁等，请阅读[如何进行贡献](CONTRIBUTING.md)。

# 简介

`Sharding-JDBC`直接封装`JDBC API`，可以理解为增强版的`JDBC`驱动，旧代码迁移成本几乎为零：

* 可适用于任何基于`java`的`ORM`框架，如：`JPA`, `Hibernate`, `Mybatis`, `Spring JDBC Template`或直接使用`JDBC`。
* 可基于任何第三方的数据库连接池，如：`DBCP`, `C3P0`, `BoneCP`, `Druid`等。
* 理论上可支持任意实现`JDBC`规范的数据库。虽然目前仅支持`MySQL`，但已有支持`Oracle`，`SQLServer`，`DB2`等数据库的计划。

`Sharding-JDBC`定位为轻量级`java`框架，使用客户端直连数据库，以`jar`包形式提供服务，未使用中间层，无需额外部署，无其他依赖，`DBA`也无需改变原有的运维方式。`SQL`解析使用`Druid`解析器，是目前性能最高的`SQL`解析器。

`Sharding-JDBC`功能灵活且全面：

* 分片策略灵活，可支持`=`，`BETWEEN`，`IN`等多维度分片，也可支持多分片键共用。
* `SQL`解析功能完善，支持聚合，分组，排序，`Limit`，`OR`等查询，并且支持`Binding Table`以及笛卡尔积的表查询。
* 支持柔性事务(目前仅最大努力送达型)。
* 支持读写分离。
* 支持分布式生成全局主键。

`Sharding-JDBC`配置多样：

* 可支持YAML和Spring命名空间配置
* 灵活多样的`inline`方式

***

以下是常见的分库分表产品和`Sharding-JDBC`的对比：

| 功能          | Cobar         | Cobar-client  | TDDL        | Sharding-JDBC  |
| ------------- |:-------------:| -------------:| -----------:|---------------:|
| 分库          | 有            | 有             | 未开源      | 有              |
| 分表          | 无            | 无             | 未开源      | 有              |
| 中间层        | 是            | 否             | 否          | 否              |
| ORM支持       | 任意          | 仅MyBatis      | 任意        | 任意            |
| 数据库支持     | 仅MySQL       | 任意           | 任意        | 任意            |
| 异构语言       | 可           | 仅Java          | 仅Java     | 仅Java          |
| 外部依赖       | 无           | 无              | Diamond    | 无              |

***

# 整体架构图

![整体架构图](http://dangdangdotcom.github.io/sharding-jdbc/img/architecture.png)

![柔性事务-最大努力送达型](http://dangdangdotcom.github.io/sharding-jdbc/img/architecture-soft-transaction-bed.png)

# 相关文档

[FAQ](http://dangdangdotcom.github.io/sharding-jdbc/post/faq/)

[Release Notes](http://dangdangdotcom.github.io/sharding-jdbc/post/release_notes/)

[使用指南](http://dangdangdotcom.github.io/sharding-jdbc/post/user_guide/)

[详细功能列表](http://dangdangdotcom.github.io/sharding-jdbc/post/features/)

[核心概念](http://dangdangdotcom.github.io/sharding-jdbc/post/concepts/)

[架构图](http://dangdangdotcom.github.io/sharding-jdbc/post/architecture/)

[Yaml文件和Spring命名空间配置](http://dangdangdotcom.github.io/sharding-jdbc/post/configuration/)

[基于暗示(Hint)的分片键值注册方法](http://dangdangdotcom.github.io/sharding-jdbc/post/hint_sharding_value/)

[读写分离](http://dangdangdotcom.github.io/sharding-jdbc/post/master_slave)

[柔性事务](http://dangdangdotcom.github.io/sharding-jdbc/post/soft_transaction)

[Id生成器](http://dangdangdotcom.github.io/sharding-jdbc/post/id-generator)

[目录结构说明](http://dangdangdotcom.github.io/sharding-jdbc/post/directory_structure)

[阅读源码编译问题说明](http://dangdangdotcom.github.io/elastic-job/post/source_code_guide)

[使用限制](http://dangdangdotcom.github.io/sharding-jdbc/post/limitations/)

[SQL支持详细列表](http://dangdangdotcom.github.io/sharding-jdbc/post/sql_supported/)

[压力测试报告](http://dangdangdotcom.github.io/sharding-jdbc/post/stress_test/)

[未来线路规划](http://dangdangdotcom.github.io/sharding-jdbc/post/roadmap/)

[事务支持说明](http://dangdangdotcom.github.io/sharding-jdbc/post/transaction/)

[InfoQ新闻](http://www.infoq.com/cn/news/2016/01/sharding-jdbc-dangdang)

[CSDN文章](http://geek.csdn.net/news/detail/55513)

# Quick Start

## 引入maven依赖

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 规则配置
`Sharding-JDBC`的分库分表通过规则配置描述，请简单浏览配置全貌：

```java
ShardingRule shardingRule = ShardingRule.builder()
        .dataSourceRule(dataSourceRule)
        .tableRules(tableRuleList)
        .databaseShardingStrategy(new DatabaseShardingStrategy("sharding_column", new XXXShardingAlgorithm()))
        .tableShardingStrategy(new TableShardingStrategy("sharding_column", new XXXShardingAlgorithm())))
        .build();
```

规则配置包括数据源配置、表规则配置、分库策略和分表策略组成。这只是最简单的配置方式，实际使用可更加灵活，如：多分片键，分片策略直接和`tableRule`绑定等。

>详细的规则配置请参考[用户指南](http://dangdangdotcom.github.io/sharding-jdbc/post/user_guide/)

## 使用原生JDBC接口
通过`ShardingDataSourceFactory`工厂和规则配置对象获取`ShardingDataSource`，`ShardingDataSource`实现自`JDBC`的标准接口`DataSource`。然后可通过`DataSource`选择使用原生`JDBC`开发，或者使用`JPA`, `MyBatis`等`ORM`工具。
以`JDBC`原生实现为例：

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(shardingRule);
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    preparedStatement.setInt(1, 10);
    preparedStatement.setInt(2, 1001);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            System.out.println(rs.getInt(1));
            System.out.println(rs.getInt(2));
        }
    }
}
```

## 使用Spring命名空间配置

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

    <rdb:strategy id="orderTableStrategy" sharding-columns="order_id" algorithm-expression="t_order_${order_id.longValue() % 4}"/>
    <rdb:strategy id="orderItemTableStrategy" sharding-columns="order_id" algorithm-expression="t_order_item_${order_id.longValue() % 4}"/>
    <rdb:data-source id="shardingDataSource">
        <rdb:sharding-rule data-sources="dbtbl_0,dbtbl_1">
            <rdb:table-rules>
                <rdb:table-rule logic-table="t_order" actual-tables="t_order_${0..3}" table-strategy="orderTableStrategy"/>
                <rdb:table-rule logic-table="t_order_item" actual-tables="t_order_item_${0..3}" table-strategy="orderItemTableStrategy"/>
            </rdb:table-rules>
            <rdb:default-database-strategy sharding-columns="user_id" algorithm-expression="dbtbl_${user_id.longValue() % 2 + 1}"/>
        </rdb:sharding-rule>
    </rdb:data-source>
</beans>
```
