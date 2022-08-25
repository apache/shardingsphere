+++
title = "影子库"
weight = 6
+++

## 背景信息
如果您想在 ShardingSphere-Proxy 中使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释

```yaml
rules:
- !SHADOW
  dataSources:
    shadowDataSource:
      productionDataSourceName: # 生产数据源名称
      shadowDataSourceName: # 影子数据源名称
  tables:
    <table-name>:
      dataSourceNames: # 影子表关联影子数据源名称列表
        - <shadow-data-source>
      shadowAlgorithmNames: # 影子表关联影子算法名称列表
        - <shadow-algorithm-name>
  defaultShadowAlgorithmName: # 默认影子算法名称（选配项）
  shadowAlgorithms:
    <shadow-algorithm-name> (+): # 影子算法名称
      type: # 影子算法类型
      props: # 影子算法属性配置
```

详情请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)

## 操作步骤

1. 在 YAML 文件中配置影子库规则，包含数据源、影子库规则、全局属性等配置项；
2. 调用 YamlShardingSphereDataSourceFactory 对象的 createDataSource 方法，根据 YAML 文件中的配置信息创建 ShardingSphereDataSource。

## 配置示例

```yaml
dataSources:
   ds:
      url: jdbc:mysql://127.0.0.1:3306/ds?serverTimezone=UTC&useSSL=false
      username: root
      password:
      connectionTimeoutMilliseconds: 30000
      idleTimeoutMilliseconds: 60000
      maxLifetimeMilliseconds: 1800000
      maxPoolSize: 50
      minPoolSize: 1
   shadow_ds:
      url: jdbc:mysql://127.0.0.1:3306/shadow_ds?serverTimezone=UTC&useSSL=false
      username: root
      password:
      connectionTimeoutMilliseconds: 30000
      idleTimeoutMilliseconds: 60000
      maxLifetimeMilliseconds: 1800000
      maxPoolSize: 50
      minPoolSize: 1

rules:
- !SHADOW
  dataSources:
    shadowDataSource:
      productionDataSourceName: ds
      shadowDataSourceName: shadow_ds
  tables:
    t_order:
      dataSourceNames: 
        - shadowDataSource
      shadowAlgorithmNames: 
        - user-id-insert-match-algorithm
        - simple-hint-algorithm
  shadowAlgorithms:
    user-id-insert-match-algorithm:
      type: REGEX_MATCH
      props:
        operation: insert
        column: user_id
        regex: "[1]"
    simple-hint-algorithm:
      type: SIMPLE_HINT
      props:
        foo: bar
```

## 相关参考

- [影子库的核心特性](/cn/features/shadow/)
- [JAVA API：影子库配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [Spring Boot Starter：影子库配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)
- [Spring 命名空间：影子库配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
