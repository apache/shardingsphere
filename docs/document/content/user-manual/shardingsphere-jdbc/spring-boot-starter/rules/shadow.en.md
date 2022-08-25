+++
title = "Shadow DB"
weight = 5
+++

## Background
If you want to use the ShardingSphere Shadow DB feature in the Spring Boot environment, please refer to the following configuration.

## Parameters

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.production-data-source-name= # Production data source name
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name= # Shadow data source name

spring.shardingsphere.rules.shadow.tables.<table-name>.data-source-names= # Shadow table location shadow data source names (multiple values are separated by ",")
spring.shardingsphere.rules.shadow.tables.<table-name>.shadow-algorithm-names= # Shadow table location shadow algorithm names (multiple values are separated by ",")

spring.shardingsphere.rules.shadow.defaultShadowAlgorithmName= # Default shadow algorithm name, optional item.

spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type= # Shadow algorithm type
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx= # Shadow algorithm property configuration
```

For details, see [list of built-in shadow algorithms](/en/user-manual/common-config/builtin-algorithm/shadow/)

## Procedure
1. Configure the shadow library rules in the SpringBoot file, including configuration items such as data sources, shadow rules, and global properties.
2. Start the SpringBoot program, the configuration will be loaded automatically, and the ShardingSphereDataSource will be initialized.

## Sample
```properties
spring.shardingsphere.datasource.names=ds,shadow-ds

spring.shardingsphere.datasource.ds.jdbc-url=jdbc:mysql://localhost:3306/ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.ds.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds.username=root
spring.shardingsphere.datasource.ds.password=

spring.shardingsphere.datasource.shadow-ds.jdbc-url=jdbc:mysql://localhost:3306/shadow_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
spring.shardingsphere.datasource.shadow-ds.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.shadow-ds.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.shadow-ds.username=root
spring.shardingsphere.datasource.shadow-ds.password=

spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.production-data-source-name=ds
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name=shadow-ds

spring.shardingsphere.rules.shadow.tables.t_user.data-source-names=shadow-data-source
spring.shardingsphere.rules.shadow.tables.t_user.shadow-algorithm-names=user-id-insert-match-algorithm,simple-hint-algorithm

spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.type=VALUE_MATCH
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.operation=insert
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.column=user_id
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.value=1

spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.type=SIMPLE_HINT
spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.props.shadow=true
spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.props.foo=bar
```

## Related References
- [Feature Description of Shadow DB](/en/features/shadow/)
- [JAVA API: Shadow DB ](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML Configuration: Shadow DB](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring Namespace: Shadow DB](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [Dev Guide: Shadow DB](/en/dev-manual/shadow/)
