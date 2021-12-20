+++
title = "模式配置"
weight = 1
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd)

\<shardingsphere:mode />

| *名称*              | *类型* | *说明*                                         | *默认值* |
| ------------------ | ------ | --------------------------------------------- | ------- |
| type               | 属性   | 运行模式类型。可选配置：Memory、Standalone、Cluster |         |
| repository-ref (?) | 属性   | 持久化仓库 Bean 引用。Memory 类型无需持久化         |         |
| overwrite (?)      | 属性   | 是否使用本地配置覆盖持久化配置                      | false   |

### 内存模式

缺省配置。

#### 配置示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd">
    
    <shardingsphere:data-source id="ds" schema-name="foo_schema" data-source-names="..." rule-refs="..." />
</beans>
```

### 单机模式

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.0.0.xsd)

<standalone:repository />

| *名称*     | *类型* | *说明*             |
| --------- | ------ | ----------------- |
| id        | 属性   | 持久化仓库 Bean 名称 |
| type      | 属性   | 持久化仓库类型       |
| props (?) | 标签   | 持久化仓库所需属性    |

#### 配置示例

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
            <prop key="path">target</prop>
        </props>
    </standalone:repository>

    <shardingsphere:data-source id="ds" schema-name="foo_schema" data-source-names="..." rule-refs="..." >
        <shardingsphere:mode type="Standalone" repository-ref="standaloneRepository" overwrite="true" />
    </shardingsphere:data-source>
</beans>
```

### 集群模式

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.0.0.xsd)

<cluster:repository />

| *名称*         | *类型* | *说明*             |
| ------------- | ------ | ----------------- |
| id            | 属性   | 持久化仓库 Bean 名称 |
| type          | 属性   | 持久化仓库类型       |
| namespace     | 属性   | 注册中心命名空间     |
| server-lists  | 属性   | 注册中心连接地址     |
| props (?)     | 标签   | 持久化仓库所需属性    |

#### 配置示例

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
    
    <shardingsphere:data-source id="ds" schema-name="foo_schema" data-source-names="..." rule-refs="...">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true" />
    </shardingsphere:data-source>
</beans>
```

持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/)。
