+++
title = "影子库"
weight = 5
+++

## 背景信息
如果您只想使用 XML 配置文件方式配置使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释
### 配置入口

```xml
<shadow:rule />
```

###  可配置属性：
|  *名称*  |  *类型*  | *说明*  | 
| ------- | -------- | ------- | 
| id | 属性 | Spring Bean Id | 
| data-source(?) | 标签 | 影子库数据源映射配置 | 
| shadow-table(?) | 标签 | 影子表配置 | 
| shadow-algorithm(?) | 标签 | 影子表配置 | 
| default-shadow-algorithm-name(?) | 标签 | 默认影子算法名称 | 

###  影子数据源配置：
```xml
<shadow:data-source />
```

|  *名称*  |  *类型*  | *说明*  |
| ------- | -------- | ------- |
| id | 属性 | Spring Bean Id |
| production-data-source-name | 属性 | 生产数据源名称 |
| shadow-data-source-name | 属性 | 影子数据源名称 |

###  影子表配置：
```xml
<shadow:shadow-table />
```

|  *名称*  |  *类型*  | *说明*  |
| ------- | -------- | ------- |
| name | 属性 | 影子表名称 |
| data-sources | 属性 | 影子表关联影子数据源名称列表（多个值用","隔开） |
| algorithm (?) | 标签 | 影子表关联影子算法配置 |

```xml
<shadow:algorithm />
```

|  *名称*  |  *类型*  | *说明*  |
| ------- | -------- | ------- |
| shadow-algorithm-ref | 属性 | 影子表关联影子算法名称 |

###  影子算法配置：
```xml
<shadow:shadow-algorithm />
```

|  *名称*  |  *类型*  | *说明*  |
| ------- | -------- | ------- |
| id | 属性 | 影子算法名称 |
| type | 属性 | 影子算法类型 |
| props (?) | 标签 | 影子算法属性配置 |

详情请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)

## 操作步骤
1. 创建生产和影子数据源。
2. 配置影子规则
    - 配置影子数据源
    - 配置影子表
    - 配置影子算法

## 配置示例

```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:shadow="http://shardingsphere.apache.org/schema/shardingsphere/shadow" xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/shadow
                           http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow.xsd
                           ">
    <shadow:shadow-algorithm id="user-id-insert-match-algorithm" type="VALUE_MATCH">
        <props>
            <prop key="operation">insert</prop>
            <prop key="column">user_id</prop>
            <prop key="value">1</prop>
        </props>
    </shadow:shadow-algorithm>

    <shadow:rule id="shadowRule">
        <shadow:data-source id="shadow-data-source" production-data-source-name="ds" shadow-data-source-name="ds_shadow"/>
        <shadow:shadow-table name="t_user" data-sources="shadow-data-source">
        <shadow:algorithm shadow-algorithm-ref="user-id-insert-match-algorithm" />
        </shadow:shadow-table>
    </shadow:rule>
</beans>
```

## 相关参考
- [影子库的特性描述](/cn/features/shadow/)
- [JAVA API：影子库的配置 ](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML 配置：影子库的配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring 命名空间：影子库的配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [开发者指南：影子库的接口和示例](/cn/dev-manual/shadow/)
