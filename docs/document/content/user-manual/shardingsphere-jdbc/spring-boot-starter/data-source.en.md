+++
title = "Data Source"
weight = 2
chapter = true
+++

## Background information

### Use local datasource

The database driver showed in the example is MySQL and the connection pool is HikariCP, either of which can be replaced by other database drivers and connection pools. When using ShardingSphere JDBC, the property names of the JDBC pools depend on its own definition instead of being fixed by ShardingSphere. See relevant procedures at `org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator`. For example, using `url` instead of `jdbc-url` for Alibaba Druid 1.2.9 is the expected behavior.

### Use datasource JNDI

If you wish to use JNDI for database configuration, you can replace a series of datasource configurations with `spring.shardingsphere.datasource.${datasourceName}.jndiName` when you are using ShardingSphere-JDBC on application servers(e.g. Tomcat).

## Parameters Explanation

### Using local datasource

```properties
spring.shardingsphere.datasource.names= # Actual datasource names. Multiple datasources are separated with comma

# <actual-data-source-name>  to show actual datasource name
spring.shardingsphere.datasource.<actual-data-source-name>.type= # Full class name of the database connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.driver-class-name= # Database-driven class name, based on the database connection pool's own configuration
spring.shardingsphere.datasource.<actual-data-source-name>.jdbc-url= # Database URL connection, in ine with the connection pool's own configuration 
spring.shardingsphere.datasource.<actual-data-source-name>.username= # database user names，in line with the connection pool's own configuration
spring.shardingsphere.datasource.<actual-data-source-name>.password= # database password ，in line with the connection pool's own configuration 
spring.shardingsphere.datasource.<actual-data-source-name>.<xxx>= # ... Other properties of the database connection pool
```

### Using JNDI datasource

```properties
spring.shardingsphere.datasource.names= # Authentic datasource names. Multiple datasources are separated with comma
# <actual-data-source-name> to show actual datasource name
spring.shardingsphere.datasource.<actual-data-source-name>.jndi-name= # datasource JNDI
```

## Configuration Examples

### Using local datasource

```properties
# configure actual datasource
spring.shardingsphere.datasource.names=ds1,ds2

# configure the first datasource
spring.shardingsphere.datasource.ds1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

# configure the second datasource
spring.shardingsphere.datasource.ds2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds2.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds2.jdbc-url=jdbc:mysql://localhost:3306/ds2
spring.shardingsphere.datasource.ds2.username=root
spring.shardingsphere.datasource.ds2.password=
```

### Using JNDI datasource

```properties
# configure actual datasource
spring.shardingsphere.datasource.names=ds1,ds2
# configure the first datasource
spring.shardingsphere.datasource.ds1.jndi-name=java:comp/env/jdbc/ds1
# configure the second datasource
spring.shardingsphere.datasource.ds2.jndi-name=java:comp/env/jdbc/ds2
```
