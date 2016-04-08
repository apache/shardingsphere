# 主要贡献者

* 张亮 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) zhangliang@dangdang.com
* 高洪涛 [当当](http://www.dangdang.com/) gaohongtao@dangdang.com
* 曹昊 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) caohao@dangdang.com
* 岳令 &nbsp;&nbsp;&nbsp;[当当](http://www.dangdang.com/) yueling@dangdang.com

**讨论QQ群：**532576663（不限于Sharding-JDBC，包括分布式，数据库相关以及其他互联网技术交流）

# 简介
`Sharding-JDBC`是当当应用框架`ddframe`中，关系型数据库模块`dd-rdb`中分离出来的数据库水平扩展框架，即透明化数据库分库分表访问。

`Sharding-JDBC`直接封装`JDBC API`，可以理解为增强版的`JDBC`驱动，旧代码迁移成本几乎为零：

* 可适用于任何基于`java`的`ORM`框架，如：`JPA`, `Hibernate`, `Mybatis`, `Spring JDBC Template`或直接使用`JDBC`。
* 可基于任何第三方的数据库连接池，如：`DBCP`, `C3P0`, `BoneCP`, `Druid`等。
* 理论上可支持任意实现`JDBC`规范的数据库。虽然目前仅支持`MySQL`，但已有支持`Oracle`，`SQLServer`，`DB2`等数据库的计划。

`Sharding-JDBC`定位为轻量级`java`框架，使用客户端直连数据库，以`jar`包形式提供服务，未使用中间层，无需额外部署，无其他依赖，`DBA`也无需改变原有的运维方式。`SQL`解析使用`Druid`解析器，是目前性能最高的`SQL`解析器。

`Sharding-JDBC`功能灵活且全面：

* 分片策略灵活，可支持`=`，`BETWEEN`，`IN`等多维度分片，也可支持多分片键共用。
* `SQL`解析功能完善，支持聚合，分组，排序，`Limit`，`OR`等查询，并且支持`Binding Table`以及笛卡尔积的表查询。

***

以下是常见的分库分表产品和`Sharding-JDBC`的对比：

| 功能          | Cobar         | Cobar-client   | TDDL        | Sharding-JDBC   |
| ------------- |:-------------:| --------------:| -----------:|----------------:|
| 分库          | 有            | 有             | 未开源      | 有                |
| 分表          | 无            | 无             | 未开源      | 有                |
| 中间层        | 是            | 否             | 否          | 否                |
| ORM支持       | 任意          | 仅MyBatis      | 任意        | 任意              |
| 数据库支持     | 仅MySQL       | 任意           | 任意        | 任意              |
| 异构语言       | 可            | 仅Java         | 仅Java      | 仅Java           |
| 外部依赖       | 无            | 无             | Diamond     | 无               |

***

# 整体架构图

![整体架构图1](img/architecture.png)

# Quick Start

## 引入maven依赖
```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 规则配置
`Sharding-JDBC`的分库分表通过规则配置描述，请简单浏览配置全貌：
```java
 ShardingRule shardingRule = new ShardingRule(
                dataSourceRule, 
                Arrays.asList(tableRule), 
                new DatabaseShardingStrategy("sharding_column_1", new XXXShardingAlgorithm()),
                new TableShardingStrategy("sharding_column_2", new XXXShardingAlgorithm()));
```
规则配置包括数据源配置、表规则配置、分库策略和分表策略组成。这只是最简单的配置方式，实际使用可更加灵活，如：多分片键，分片策略直接和`tableRule`绑定等。

>详细的规则配置请参考[用户指南](post/user_guide)

## 使用基于ShardingDataSource的JDBC接口
通过规则配置对象获取`ShardingDataSource`，`ShardingDataSource`实现自`JDBC`的标准接口`DataSource`。然后可通过`DataSource`选择使用原生`JDBC`开发，或者使用`JPA`, `MyBatis`等`ORM`工具。
以`JDBC`原生实现为例：
```java
DataSource dataSource = new ShardingDataSource(shardingRule);
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
            System.out.println(rs.getInt(3));
        }
    }
}
```

