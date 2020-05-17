+++
title = "Sharding"
weight = 1
+++

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Rule Configuration Based on Java

Sharding database and table of ShardingSphere-JDBC configure descriptions according to rules. The following example is the configuration of two databases plus two tables, whose databases take module and split according to order_id, tables take module and split according to order_id.

```java
    // Configure actual data sources
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // Configure the first data source
    BasicDataSource dataSource1 = new BasicDataSource();
    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource1.setUrl("jdbc:mysql://localhost:3306/ds0");
    dataSource1.setUsername("root");
    dataSource1.setPassword("");
    dataSourceMap.put("ds0", dataSource1);
    
    // Configure the second data source
    BasicDataSource dataSource2 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource2.setUrl("jdbc:mysql://localhost:3306/ds1");
    dataSource2.setUsername("root");
    dataSource2.setPassword("");
    dataSourceMap.put("ds1", dataSource2);
    
    // Configure Order table rules
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("t_order","ds${0..1}.t_order${0..1}");
    
    // Configure strategies for database + table sharding 
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order${order_id % 2}"));
    
    // Configure sharding rules
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
    // Omit order_item table rule configuration
    // ...
    
    // Get data source
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties());
```

### Rule Configuration Based on Yaml

Or use Yaml to configure, similar to configurations above:

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

shardingRule:
  tables:
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      databaseStrategy: 
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
    t_order_item: 
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      databaseStrategy: 
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}
```

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

### Use Native JDBC

Configure objects with `ShardingDataSourceFactory` or `YamlShardingDataSourceFactory` to get `ShardingDataSource`, which is realized by standard JDBC DataSource. Or choose native JDBC to develop through DataSource; or use JPA, MyBatis and other ORM tools. Take native JDBC for example:

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
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

## Using Spring

### Introduce Maven Dependency

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Rule Configuration Based on Spring Boot

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}

spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
```

#### Rule Configuration Based on Spring Boot + JNDI

If you plan to use ShardingSphere-JDBC in Application Server (such as Tomcat) with `Spring boot + JNDI`, `spring.shardingsphere.datasource.${datasourceName}.jndiName` can be used as an alternative to series of configuration of datasource. For example:

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.jndi-name=java:comp/env/jdbc/ds0
spring.shardingsphere.datasource.ds1.jndi-name=jdbc/ds1

spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=user_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

spring.shardingsphere.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}

spring.shardingsphere.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
spring.shardingsphere.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
```

### Rule Configuration Based on Spring Name Space

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding 
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd 
                        ">
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
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item$->{order_id % 2}" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Use DataSource in Spring

Inject DataSource to use; or configure DataSource in JPA, Hibernate or MyBatis to use.

```java
@Resource
private DataSource dataSource;
```

Rule configurations include data source configuration, table rule configuration, database sharding strategy and table sharding strategy. All these are the simplest configuration methods, and the practical use can be more flexible, like multiple sharding keys, table rule configuration bound directly with sharding strategies, etc. For more detailed configurations, please refer to [Configuration Manual](https://shardingsphere.apache.org/document/current/en/manual/shardingsphere-jdbc/configuration/).
