+++
title = "Read-write Split"
weight = 2

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

```java
    // Configure actual data sources
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // Configure master data source
    BasicDataSource masterDataSource = new BasicDataSource();
    masterDataSource.setDriverClassName("com.mysql.jdbc.Driver");
    masterDataSource.setUrl("jdbc:mysql://localhost:3306/ds_master");
    masterDataSource.setUsername("root");
    masterDataSource.setPassword("");
    dataSourceMap.put("ds_master", masterDataSource);
    
    // Configure the first slave data source
    BasicDataSource slaveDataSource1 = new BasicDataSource();
    slaveDataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    slaveDataSource1.setUrl("jdbc:mysql://localhost:3306/ds_slave0");
    slaveDataSource1.setUsername("root");
    slaveDataSource1.setPassword("");
    dataSourceMap.put("ds_slave0", slaveDataSource1);
    
    // Configure the second slave data source
    BasicDataSource slaveDataSource2 = new BasicDataSource();
    slaveDataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    slaveDataSource2.setUrl("jdbc:mysql://localhost:3306/ds_slave1");
    slaveDataSource2.setUsername("root");
    slaveDataSource2.setPassword("");
    dataSourceMap.put("ds_slave1", slaveDataSource2);
    
    // Configure read-write split rule
    MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ds_master_slave", "ds_master", Arrays.asList("ds_slave0", "ds_slave1"));
    
    // Get data source
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig, new Properties());
```

### Rule Configuration Based on Yaml

Or configure by yaml, similar to the configuration method above:

```yaml
dataSources:
  ds_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
  ds_slave0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave0
    username: root
    password:
  ds_slave1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: [ds_slave0, ds_slave1]
  
props:
  sql.show: true
```

```java
    DataSource dataSource = YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### Use Native JDBC

Configure objects with `YamlMasterSlaveDataSourceFactory` to get `MasterSlaveDataSource`, which is realized by standard JDBC DataSource interface. Or choose native JDBC to develop through DataSource; or use JPA, MyBatis and other ORM tools. Take native JDBC for example:

```java
DataSource dataSource = YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
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

## Use Spring

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
spring.shardingsphere.datasource.names=master,slave0,slave1

spring.shardingsphere.datasource.ds-master.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds-master.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds-master.url=jdbc:mysql://localhost:3306/master
spring.shardingsphere.datasource.ds-master.username=root
spring.shardingsphere.datasource.ds-master.password=

spring.shardingsphere.datasource.ds-slave0.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds-slave0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds-slave0.url=jdbc:mysql://localhost:3306/slave0
spring.shardingsphere.datasource.ds-slave0.username=root
spring.shardingsphere.datasource.ds-slave0.password=

spring.shardingsphere.datasource.ds-slave1.type=org.apache.commons.dbcp.BasicDataSource
spring.shardingsphere.datasource.ds-slave1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds-slave1.url=jdbc:mysql://localhost:3306/slave1
spring.shardingsphere.datasource.ds-slave1.username=root
spring.shardingsphere.datasource.ds-slave1.password=

spring.shardingsphere.masterslave.name=ms
spring.shardingsphere.masterslave.master-data-source-name=master
spring.shardingsphere.masterslave.slave-data-source-names=slave0,slave1

spring.shardingsphere.props.sql.show=true
```

#### Rule Configuration Based on Spring Boot + JNDI

If you plan to use ShardingSphere-JDBC in Application Server (such as Tomcat) with `Spring boot + JNDI`, `spring.shardingsphere.datasource.${datasourceName}.jndiName` can be used as an alternative to series of configuration of datasource. For example:
```properties
spring.shardingsphere.datasource.names=master,slave0,slave1

spring.shardingsphere.datasource.master.jndi-name=java:comp/env/jdbc/master
spring.shardingsphere.datasource.slave0.jndi-name=jdbc/slave0
spring.shardingsphere.datasource.slave1.jndi-name=jdbc/slave1

spring.shardingsphere.masterslave.name=ms
spring.shardingsphere.masterslave.master-data-source-name=master
spring.shardingsphere.masterslave.slave-data-source-names=slave0,slave1

spring.shardingsphere.props.sql.show=true
```

### Rule Configuration Based on Spring Name Space

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave 
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd 
                        ">
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
    
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" >
            <master-slave:props>
                    <prop key="sql.show">true</prop>
                    <prop key="executor.size">10</prop>
                    <prop key="foo">bar</prop>
                </master-slave:props>
    </master-slave:data-source>
</beans>
```

### Use DataSource in Spring

Inject DataSource to use; or configure DataSource in JPA, Hibernate or MyBatis to use.

```java
@Resource
private DataSource dataSource;
```

For more detailed configurations, please refer to [Configuration Manual](/en/manual/shardingsphere-jdbc/configuration/).
