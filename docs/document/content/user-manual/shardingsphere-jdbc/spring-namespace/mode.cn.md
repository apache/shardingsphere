+++
title = "模式配置"
weight = 1
+++

## 背景信息

缺省配置为使用单机模式。

## 参数解释

### 单机模式

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.1.1.xsd)

<standalone:repository />

| *名称*     | *类型* | *说明*             |
| --------- | ------ | ----------------- |
| id        | 属性   | 持久化仓库 Bean 名称 |
| type      | 属性   | 持久化仓库类型       |
| props (?) | 标签   | 持久化仓库所需属性    |

### 集群模式 (推荐)

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.1.1.xsd)

<cluster:repository />

| *名称*         | *类型* | *说明*             |
| ------------- | ------ | ----------------- |
| id            | 属性   | 持久化仓库 Bean 名称 |
| type          | 属性   | 持久化仓库类型       |
| namespace     | 属性   | 注册中心命名空间     |
| server-lists  | 属性   | 注册中心连接地址     |
| props (?)     | 标签   | 持久化仓库所需属性    |

## 注意事项

1. 生产环境建议使用集群模式部署。
1. 集群模式部署推荐使用 `ZooKeeper` 注册中心。

## 操作步骤

引入 MAVEN 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> 注意：请将 `${latest.release.version}` 更改为实际的版本号。

## 配置示例

### 单机模式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:standalone="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                  http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository.xsd">
    <standalone:repository id="standaloneRepository" type="File">
        <props>
            <prop key="path">.shardingsphere</prop>
        </props>
    </standalone:repository>

    <shardingsphere:data-source id="ds" database-name="foo_db" data-source-names="..." rule-refs="..." >
        <shardingsphere:mode type="Standalone" repository-ref="standaloneRepository" overwrite="false" />
    </shardingsphere:data-source>
</beans>
```

### 集群模式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd">
    <cluster:repository id="clusterRepository" type="Zookeeper" namespace="regCenter" server-lists="localhost:3182">
        <props>
            <prop key="max-retries">3</prop>
            <prop key="operation-timeout-milliseconds">1000</prop>
        </props>
    </cluster:repository>
    
    <shardingsphere:data-source id="ds" database-name="foo_db" data-source-names="..." rule-refs="...">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="false" />
    </shardingsphere:data-source>
</beans>
```

## 相关参考

- [ZooKeeper 注册中心安装与使用](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- 持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)。
