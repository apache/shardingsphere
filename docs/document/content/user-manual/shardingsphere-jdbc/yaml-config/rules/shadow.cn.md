+++
title = "影子库"
weight = 6
+++

## 背景信息
如果您想在 ShardingSphere-Proxy 中使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释
### 配置入口

```yaml
rules:
- !SHADOW
```

### 可配置属性：

| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| dataSources | 影子库逻辑数据源映射配置列表 | 无 |
| tables | 影子表配置列表 | 无 |
| defaultShadowAlgorithmName | 默认影子算法名称 | 无，选配项 |
| shadowAlgorithms | 影子算法配置列表 | 无 |

### 影子数据源配置

| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| dataSourceName | 影子库逻辑数据源名称 | 无 |
| sourceDataSourceName | 生产数据源名称 | 无 |
| shadowDataSourceName | 影子数据源名称 | 无 |

### 影子表配置

| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| dataSourceNames | 影子表关联影子库逻辑数据源名称列表 | 无 |
| shadowAlgorithmNames | 影子表关联影子算法名称列表 | 无 |

### 影子算法配置
| *名称*  | *说明*  | *默认值*  |
| ------- | ------ | ----- |
| type | 影子算法类型 | 无 |
| props | 影子算法配置 | 无 |

详情请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)

## 操作步骤

1. 创建生产和影子数据源
2. 配置影子规则
    - 配置影子数据源
    - 配置影子表
    - 配置影子算法

## 配置示例
```yaml
rules:
- !SHADOW
  dataSources:
    shadowDataSource:
      sourceDataSourceName: # 生产数据源名称
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

## 相关参考

- [影子库的核心特性](/cn/features/shadow/)
- [JAVA API：影子库配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [Spring Boot Starter：影子库配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)
- [Spring 命名空间：影子库配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
