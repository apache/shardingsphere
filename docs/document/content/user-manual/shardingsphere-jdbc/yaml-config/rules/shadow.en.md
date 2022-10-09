+++
title = "Shadow DB"
weight = 6
+++

## Background
Please refer to the following configuration in order to use the ShardingSphere shadow DB feature in ShardingSphere-Proxy.

## Parameters

```yaml
rules:
- !SHADOW
  dataSources:
    shadowDataSource:
      productionDataSourceName: # production data source name
      shadowDataSourceName: # shadow data source name
  tables:
    <table-name>:
      dataSourceNames: # shadow table associates shadow data source name list
        - <shadow-data-source>
      shadowAlgorithmNames: # shadow table associates shadow algorithm name list
        - <shadow-algorithm-name>
  defaultShadowAlgorithmName: # default shadow algorithm name (option)
  shadowAlgorithms:
    <shadow-algorithm-name> (+): # shadow algorithm name
      type: # shadow algorithm type
      props: # shadow algorithm attribute configuration
```

Please refer to [Built-in shadow algorithm list](/en/user-manual/common-config/builtin-algorithm/shadow) for more details.

## Procedure

1. Configure shadow DB rules in the YAML file, including data sources, shadow library rules, global properties and other configuration items;
2. Call the `createDataSource()` method of the `YamlShardingSphereDataSourceFactory` object to create a ShardingSphereDataSource based on the configuration information in the YAML file.

## Sample

The YAML configuration sample of shadow DB is as follows:

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

## Related References
- [Core Features of Shadow DB](/en/features/shadow/)
- [JAVA API: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [Spring Boot Starter: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)
- [Spring Namespace: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
