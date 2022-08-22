+++
title = "Shadow DB"
weight = 5
+++

## Background
If you want to use the ShardingSphere Shadow DB feature in the Spring Boot environment, please refer to the following configuration.

## Parameters
### Root Configuration
```properties
spring.shardingsphere.rules.shadow
```

###  Configurable attributes
| *Name*                        | *Description*                                            | *Default Value*  |
| ----------------------------- | -------------------------------------------------------- | ---------------- |
| data-sources                  | Shadow DB logical data source mapping configuration list | none             |
| tables                        | Shadow table configuration list                          | none             |
| shadowAlgorithms              | Shadow algorithm configuration list                      | none             |
| default-shadow-algorithm-name | Default shadow algorithm name                            | none, options    |

### Shadow Data Source Configuration
| *Name*                  | *Description*               | *Default Value*  |
| ----------------------- | --------------------------- | ---------------- |
| source-data-source-name | Production data source name | none             |
| shadow-data-source-name | Shadow data source name     | none             |

### Shadow Table Configuration
| *Name*                 | *Description*                                                         | *Default Value*  |
| ---------------------- | --------------------------------------------------------------------- | ---------------- |
| data-source-names      | Shadow table associated shadow database logical data source name list | none             |
| shadow-algorithm-names | Shadow table associated shadow algorithm name list                    | none             |

### Shadow Algorithm Configuration
| *Name*  | *Description*                  | *Default Value*  |
| ------- | ------------------------------ | ---------------- |
| type    | Shadow algorithm type          | none             |
| props   | Shadow algorithm configuration | none             |

For details, see [list of built-in shadow algorithms](/en/user-manual/common-config/builtin-algorithm/shadow/)

## Procedure
1. Create production and shadow data sources.
2. Configure shadow rules:
    - Configure shadow data sources
    - Configure shadow tables
    - Configure shadow algorithm

## Sample
```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.source-data-source-name= # Production data source name
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name= # Shadow data source name

spring.shardingsphere.rules.shadow.tables.<table-name>.data-source-names= # Shadow table location shadow data source names (multiple values are separated by ",")
spring.shardingsphere.rules.shadow.tables.<table-name>.shadow-algorithm-names= # Shadow table location shadow algorithm names (multiple values are separated by ",")

spring.shardingsphere.rules.shadow.defaultShadowAlgorithmName= # Default shadow algorithm name, optional item.

spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type= # Shadow algorithm type
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx= # Shadow algorithm property configuration
```

## Related References
- [Feature Description of Shadow DB](/en/features/shadow/)
- [JAVA API: Shadow DB ](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML Configuration: Shadow DB](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring Namespace: Shadow DB](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [Dev Guide: Shadow DB](/en/dev-manual/shadow/)
