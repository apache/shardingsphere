+++
title = "影子库"
weight = 5
+++

## 背景信息
如果您想在 Spring Boot 环境中使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释
### 配置入口

```properties
spring.shardingsphere.rules.shadow
```

###  可配置属性：
| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| data-sources | 影子库逻辑数据源映射配置列表 | 无 |
| tables | 影子表配置列表 | 无 |
| shadowAlgorithms | 影子算法配置列表 | 无 |
| default-shadow-algorithm-name | 默认影子算法名称 | 无，选配项 |

### 影子数据源配置
| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| source-data-source-name | 生产数据源名称 | 无 |
| shadow-data-source-name | 影子数据源名称 | 无 |

### 影子表配置
| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| data-source-names | 影子表关联影子库逻辑数据源名称列表 | 无 |
| shadow-algorithm-names | 影子表关联影子算法名称列表 | 无 |

### 影子算法配置
| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| type | 影子算法类型 | 无 |
| props | 影子算法配置 | 无 |

详情请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)

## 操作步骤
1. 创建生产和影子数据源。
2. 配置影子规则
    - 配置影子数据源
    - 配置影子表
    - 配置影子算法

## 配置示例
```properties
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.source-data-source-name= # 生产数据源名称
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name= # 影子数据源名称

spring.shardingsphere.rules.shadow.tables.<table-name>.data-source-names= # 影子表关联影子数据源名称列表（多个值用","隔开）
spring.shardingsphere.rules.shadow.tables.<table-name>.shadow-algorithm-names= # 影子表关联影子算法名称列表（多个值用","隔开）

spring.shardingsphere.rules.shadow.defaultShadowAlgorithmName= # 默认影子算法名称，选配项

spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type= # 影子算法类型
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx= # 影子算法属性配置
```

## 相关参考
- [影子库的特性描述](/cn/features/shadow/)
- [JAVA API：影子库的配置 ](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML 配置：影子库的配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring 命名空间：影子库的配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [开发者指南：影子库的接口和示例](/cn/dev-manual/shadow/)
