+++
title = "Shadow DB"
weight = 5
+++

## Background
Under the distributed application architecture based on microservices, the business needs multiple services to be completed through a series of service and middleware calls, so the stress test of a single service can no longer represent the real scenario.
In the test environment, rebuilding a complete set of pressure test environments similar to the production environment would mean an excessively high cost, and often an inability to simulate the complexity and flow of the online environment.
Therefore, enterprises usually select the full link voltage test method, i.e. a pressure test in the production environment, so that the test results can accurately reflect the system's real capacity and performance level.

## Parameters
### Configuration Entry
```xml
<shadow:rule />
```

###  Configurable Properties:
|  *Name*  |  *Type*  | *Description*  | 
| ------- | -------- | ------- | 
| id | Attribute | Spring Bean Id | 
| data-source(?) | Tag | Shadow data source configuration | 
| shadow-table(?) | Tag | Shadow table configuration | 
| shadow-algorithm(?) | Tag | Shadow table configuration | 
| default-shadow-algorithm-name(?) | Tag | Default shadow algorithm configuration | 

###  Shadow data source configuration:
```xml
<shadow:data-source />
```

|  *Name*  |  *Type*  | *Description*  |
| ------- | -------- | ------- |
| id | Attribute | Spring Bean Id |
| production-data-source-name | Attribute | Production data source name |
| shadow-data-source-name     | Attribute | Shadow data source name     |

###  Shadow table configuration:
```xml
<shadow:shadow-table />
```

|  *Name*  |  *Type*  | *Description*  |
| ------- | -------- | ------- |
| name | Attribute | Shadow table name|
| data-sources | Attribute | Shadow table associated shadow data source name list (multiple values are separated by ",") |
| algorithm (?) | Tag | Shadow table association shadow algorithm configuration |

```xml
<shadow:algorithm />
```

|  *Name*  |  *Type*  | *Description*  |
| ------- | -------- | ------- |
| shadow-algorithm-ref | Attribute | Shadow table association shadow algorithm name |

###  Shadow algorithm configuration:
```xml
<shadow:shadow-algorithm />
```

|  *Name*  |  *Type*  | *Description*  |
| ------- | -------- | ------- |
| id | Attribute | Shadow algorithm name |
| type | Attribute | Shadow algorithm type |
| props (?) | Tag | Shadow algorithm attribute configuration |
Refer to [Builin Shadow Algorithm](/en/user-manual/common-config/builtin-algorithm/shadow/) for details

## Procedure
1. Create production and shadow data sources.
2. Configure shadow rules.
    - Configure shadow data sources.
    - Configure shadow table.
    - Configure shadow algorithm.

## Sample
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

## Related References
- [Feature Description of Shadow DB](/en/features/shadow/)
- [JAVA API: Shadow DB ](/en/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)
- [YAML Configuration: Shadow DB](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)
- [Spring Namespace: Shadow DB](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
- [Dev Guide: Shadow DB](/en/dev-manual/shadow/)
