+++
title = "YAML 配置"
weight = 1
chapter = true
+++

## 简介

YAML 提供通过配置文件的方式与 ShardingSphere-JDBC 交互。
配合治理模块一同使用时，持久化在配置中心的配置均为 YAML 格式。

YAML 配置是最常见的配置方式，可以省略编程的复杂度，简化用户配置。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 YAML

ShardingSphere-JDBC 的 YAML 配置文件通过 Database 名称、运行模式、数据源集合、规则集合以及属性配置组成。

```yaml
# JDBC 逻辑库名称。在集群模式中，使用该参数来联通 ShardingSphere-JDBC 与 ShardingSphere-Proxy。
# 默认值：logic_db
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

模式详情请参见[模式配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/mode)。

数据源详情请参见[数据源配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/data-source)。

规则详情请参见[规则配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules)。


### 构建数据源

通过 YamlShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java

File yamlFile = // 指定 YAML 文件路径
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### 使用数据源

使用方式同 Java API。

## 语法说明

`!!` 表示实例化该类

`!` 表示自定义别名

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
