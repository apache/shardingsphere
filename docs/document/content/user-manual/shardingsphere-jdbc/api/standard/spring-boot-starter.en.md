+++
title = "Use Spring Boot Starter"
weight = 3
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

Note: The example connection pool is HikariCP, which can be replaced with other connection pools according to business scenarios.

```properties
# Configure actual data sources
spring.shardingsphere.datasource.names=ds1,ds2

# Configure the 1st data source
spring.shardingsphere.datasource.ds1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

# Configure the 2nd data source
spring.shardingsphere.datasource.ds2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds2.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds2.jdbc-url=jdbc:mysql://localhost:3306/ds2
spring.shardingsphere.datasource.ds2.username=root
spring.shardingsphere.datasource.ds2.password=

# Please reference concentrate rule configurations
# ...
```

### Use JNDI Data Source

If developer plan to use ShardingSphere-JDBC in Web Server (such as Tomcat) with JNDI data source, 
`spring.shardingsphere.datasource.${datasourceName}.jndiName` can be used as an alternative to series of configuration of datasource. 
For example:

```properties
# Configure actual data sources
spring.shardingsphere.datasource.names=ds1,ds2

# Configure the first data source
spring.shardingsphere.datasource.ds1.jndi-name=java:comp/env/jdbc/ds1
# Configure the second data source
spring.shardingsphere.datasource.ds2.jndi-name=java:comp/env/jdbc/ds2

# Please reference concentrate rule configurations
# ...
```

## Use ShardingSphere Data Source in Spring

Same with Spring Boot Starter.
