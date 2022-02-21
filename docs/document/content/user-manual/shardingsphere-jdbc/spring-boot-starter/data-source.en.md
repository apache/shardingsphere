+++
title = "Data Source"
weight = 2
chapter = true
+++

## Use Native Data Source

### Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Actual data source name, multiple split by `,`

# <actual-data-source-name> indicate name of data source name
spring.shardingsphere.datasource.<actual-data-source-name>.type= # Full class name of database connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.driver-class-name= # Class name of database driver, ref property of connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.jdbc-url= # Database URL, ref property of connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.username= # Database username, ref property of connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.password= # Database password, ref property of connection pool
spring.shardingsphere.datasource.<actual-data-source-name>.<xxx>= # ... Other properties for data source pool
```

### Example

In this example, the database driver is MySQL, and connection pool is HikariCP, which can be replaced with other database drivers and connection pools.

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
```

## Use JNDI Data Source

If developer plan to use ShardingSphere-JDBC in Web Server (such as Tomcat) with JNDI data source,
`spring.shardingsphere.datasource.${datasourceName}.jndiName` can be used as an alternative to series of configuration of data source.

### Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Actual data source name, multiple split by `,`

# <actual-data-source-name> indicate name of data source name
spring.shardingsphere.datasource.<actual-data-source-name>.jndi-name= # JNDI of data source
```

### Example

```properties
# Configure actual data sources
spring.shardingsphere.datasource.names=ds1,ds2

# Configure the 1st data source
spring.shardingsphere.datasource.ds1.jndi-name=java:comp/env/jdbc/ds1
# Configure the 2nd data source
spring.shardingsphere.datasource.ds2.jndi-name=java:comp/env/jdbc/ds2
```
