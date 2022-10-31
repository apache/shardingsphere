+++
title = "影子库"
weight = 5
+++

## 背景信息
如果您想在 Spring Boot 环境中使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释

```properties
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.production-data-source-name= # 生产数据源名称
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name= # 影子数据源名称

spring.shardingsphere.rules.shadow.tables.<table-name>.data-source-names= # 影子表关联影子数据源名称列表（多个值用","隔开）
spring.shardingsphere.rules.shadow.tables.<table-name>.shadow-algorithm-names= # 影子表关联影子算法名称列表（多个值用","隔开）

spring.shardingsphere.rules.shadow.defaultShadowAlgorithmName= # 默认影子算法名称，选配项

spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type= # 影子算法类型
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx= # 影子算法属性配置
```

详情请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)

## 操作步骤

1. 在 SpringBoot 文件中配置影子库规则，包含数据源、影子规则、全局属性等配置项。
2. 启动 SpringBoot 程序，会自动加载配置，并初始化 ShardingSphereDataSource。

## 配置示例
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

## 相关参考
- [影子库的特性描述](/cn/features/shadow/)
- [JAVA API：影子库的配置 ](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML 配置：影子库的配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring 命名空间：影子库的配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [开发者指南：影子库的接口和示例](/cn/dev-manual/shadow/)
