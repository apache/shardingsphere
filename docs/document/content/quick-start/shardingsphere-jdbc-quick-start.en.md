+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## Scenarios

There are two ways you can configure Apache ShardingSphere: `Java` and `YAML`. 
Developers can choose the preferred method according to their requirements. 

## Limitations

Currently only Java language is supported.

## Requirements

The development environment requires Java JRE 8 or later.

## Procedure

1. Rules configuration.

Please refer to [User Manual](/en/user-manual/shardingsphere-jdbc/) for more details.

2. Import Maven dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> Notice: Please change `${latest.release.version}` to the actual version.

3. Create YAML configuration file

```yaml
# JDBC database name. In cluster mode, use this parameter to connect ShardingSphere-JDBC and ShardingSphere-Proxy.
# Default：logic_db
databaseName (?):

mode:

dataSources:

rules:
- !FOO_XXX
    ...
- !BAR_XXX
    ...

props:
  key_1: value_1
  key_2: value_2
```

4. Take `spring boot` as an example, edit `application.properties`.

```properties
# Configuring DataSource Drivers
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# Specify a YAML configuration file
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

For details, see [Spring Boot](/en/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/spring-boot/).
