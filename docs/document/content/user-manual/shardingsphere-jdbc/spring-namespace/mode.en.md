+++
title = "Mode"
weight = 1
+++

## Background

The default configuration uses standalone mode.

## Parameters Explained

### Standalone Mode

Namespace:[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.1.1.xsd)
<standalone:repository />

| *Name*    | *Type*   | *Description*                                 |
| --------- | -------- | --------------------------------------------- |
| id        | Property | Persistent repository Bean name               |
| type      | Property | Persistent repository Type                    |
| props (?) |Tag       | Properties required for persistent repository |

### Cluster Mode(Recommended)

Namespace：[http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.1.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.1.1.xsd)

<cluster:repository />

| *Name*        | *Type*   | *Description*                                 |
| ------------- | -------- | --------------------------------------------- |
| id            | Property | Persistent repository Bean name               |
| type          | Property | Persistent repository Type                    |
| namespace     | Property | Registry Center namespace                     |
| server-lists  | Property | Registry Center Link                          |
| props (?)     | Tag      | Properties required for persistent repository |

## Tips:

1. For production environments, it is recommended to use cluster mode deployment.
1. For cluster mode deployment, it is recommended to use `ZooKeeper` registry. 

## Operating Procedures

Introduce MAVEN dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> Note: Please change `${latest.release.version}` to the actual version number.

## Configuration Example

### Standalone Mode

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

### Cluster Mode

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

## Relevant References

- [Installation and use of ZooKeeper Registry Center](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- For details about persistent repository, please refer to [List of Built-in repository types](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)
