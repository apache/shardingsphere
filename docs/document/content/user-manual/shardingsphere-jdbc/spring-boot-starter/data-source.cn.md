+++
title = "数据源配置"
weight = 2
chapter = true
+++

## 使用本地数据源

### 配置项说明

```properties
spring.shardingsphere.datasource.names= # 真实数据源名称，多个数据源用逗号区分

# <actual-data-source-name> 表示真实数据源名称
spring.shardingsphere.datasource.<actual-data-source-name>.type= # 数据库连接池全类名
spring.shardingsphere.datasource.<actual-data-source-name>.driver-class-name= # 数据库驱动类名，以数据库连接池自身配置为准
spring.shardingsphere.datasource.<actual-data-source-name>.jdbc-url= # 数据库 URL 连接，以数据库连接池自身配置为准
spring.shardingsphere.datasource.<actual-data-source-name>.username= # 数据库用户名，以数据库连接池自身配置为准
spring.shardingsphere.datasource.<actual-data-source-name>.password= # 数据库密码，以数据库连接池自身配置为准
spring.shardingsphere.datasource.<actual-data-source-name>.<xxx>= # ... 数据库连接池的其它属性
```

### 配置示例

示例的数据库驱动为 MySQL，连接池为 HikariCP，可以更换为其他数据库驱动和连接池。

```properties
# 配置真实数据源
spring.shardingsphere.datasource.names=ds1,ds2

# 配置第 1 个数据源
spring.shardingsphere.datasource.ds1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=

# 配置第 2 个数据源
spring.shardingsphere.datasource.ds2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds2.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds2.jdbc-url=jdbc:mysql://localhost:3306/ds2
spring.shardingsphere.datasource.ds2.username=root
spring.shardingsphere.datasource.ds2.password=
```

## 使用 JNDI 数据源

如果计划使用 JNDI 配置数据库，在应用容器（如 Tomcat）中使用 ShardingSphere-JDBC 时，
可使用 `spring.shardingsphere.datasource.${datasourceName}.jndiName` 来代替数据源的一系列配置。

### 配置项说明

```properties
spring.shardingsphere.datasource.names= # 真实数据源名称，多个数据源用逗号区分

# <actual-data-source-name> 表示真实数据源名称
spring.shardingsphere.datasource.<actual-data-source-name>.jndi-name= # 数据源 JNDI
```

### 配置示例

```properties
# 配置真实数据源
spring.shardingsphere.datasource.names=ds1,ds2

# 配置第 1 个数据源
spring.shardingsphere.datasource.ds1.jndi-name=java:comp/env/jdbc/ds1
# 配置第 2 个数据源
spring.shardingsphere.datasource.ds2.jndi-name=java:comp/env/jdbc/ds2
```
