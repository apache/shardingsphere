+++
title = "Mode Configuration"
weight = 1
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd)

\<shardingsphere:mode />

| *Name*             | *Type*      | *Description*                                                            | *Default Value* |
| ------------------ | ----------- | ------------------------------------------------------------------------ | --------------- |
| type               | Attribute   | Type of mode configuration. Values could be: Memory, Standalone, Cluster |                 |
| repository-ref (?) | Attribute   | Persist repository configuration. Memory type does not need persist      |                 |
| overwrite (?)      | Attribute   | Whether overwrite persistent configuration with local configuration      | false           |

### Memory Mode

It is the default value.

#### Example

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

### Standalone Mode

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/standalone/repository-5.0.0.xsd)

<standalone:repository />

| *Name*    | *Type*    | *Description*                    |
| --------- | --------- | -------------------------------- |
| id        | Attribute | Name of persist repository bean  |
| type      | Attribute | Type of persist repository       |
| props (?) | Tag       | Properties of persist repository |

#### Example

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

### Cluster Mode

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository-5.0.0.xsd)

<cluster:repository />

| *Name*        | *Type*    | *Description*                    |
| ------------- | --------- | -------------------------------- |
| id            | Attribute | Name of persist repository bean  |
| type          | Attribute | Type of persist repository       |
| namespace     | Attribute | Namespace of registry center     |
| server-lists  | Attribute | Server lists of registry center  |
| props (?)     | Tag       | Properties of persist repository |

#### Example

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

Please refer to [Builtin Persist Repository List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/) for more details about type of repository.
