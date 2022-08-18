+++
title = "Shadow DB"
weight = 6
+++

## Background
Please refer to the following configuration in order to use the ShardingSphere shadow DB feature in ShardingSphere-Proxy.

## Parameters
### Configuration entry

```yaml
rules:
- !SHADOW
```

###  Configurable attributes

| *Name*  | *Description*  | *Default*  |
| ------- | ------ | ----- |
| dataSources | shadow DB logical data source mapping the configuration list | none |
| tables | shadow table configuration list | none |
| defaultShadowAlgorithmName | name of default shadow algorithm | none, option |
| shadowAlgorithms | shadow algorithm configuration list | none |

### Shadow data source configuration

| *Name*  | *Description*  | *Default*  |
| ------- | ------ | ----- |
| dataSourceName | shadow DB logical data source name | 无 |
| sourceDataSourceName | production data source name | 无 |
| shadowDataSourceName | shadow data source name | 无 |

### Shadow table configuration

| *Name*  | *Description*  | *Default*  |
| ------- | ------ | ----- |
| dataSourceNames | shadow table associates shadow DB logical data source name list | 无 |
| shadowAlgorithmNames | shadow table associates shadow algorithm name list | 无 |

### Shadow algorithm configuration

| *Name*  | *Description*  | *Default*  |
| ------- | ------ | ----- |
| type | shadow algorithm type | none |
| props | shadow algorithm configuration | none |

Please refer to [Built-in shadow algorithm list](/en/user-manual/common-config/builtin-algorithm/shadow) for more details.

## Procedure
1. Create production and shadow data sources.
2. Configure shadow rules.
    - Configure the shadow data source.
    - Configure the shadow table.
    - Configure the shadow algorithm.

## Sample

```yaml
rules:
- !SHADOW
  dataSources:
    shadowDataSource:
      sourceDataSourceName: # production data source name
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

## Related References
- [Core Features of Shadow DB](/en/features/shadow/)
- [JAVA API: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [Spring Boot Starter: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)
- [Spring Namespace: Shadow DB Configuration](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
