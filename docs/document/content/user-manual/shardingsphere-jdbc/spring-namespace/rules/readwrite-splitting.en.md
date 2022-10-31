+++
title = "Readwrite-splitting"
weight = 2
+++

## Background
Spring namespace read/write splitting configuration method is suitable for conventional Spring projects, determine sharding rules and properties through namespace XML configuration files, and let Spring do the creation and management of ShardingSphereDataSource objects, avoiding additional coding work.

## Parameters Explained
Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.2.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.2.1.xsd)

\<readwrite-splitting:rule />

| *Name*               | *Type*    | *Description*                                |
| -------------------- | --------- | -------------------------------------------- |
| id                   | Attribute | Spring Bean Id                               |
| data-source-rule (+) | Tag       | Readwrite-splitting data source rule configuration |

\<readwrite-splitting:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                           |
| -------------------------- | ---------- | ----------------------------------------------------------------------- |
| id                         | Attribute  | Readwrite-splitting data source rule name                               |
| static-strategy            | Tag        | Static Readwrite-splitting type                                         |
| dynamic-strategy           | Tag        | Dynamic Readwrite-splitting type                                        |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                             |


\<readwrite-splitting:static-strategy />

| *Name*                     | *Type* | *Description*                                                          |
| -------------------------- | ----- | ----------------------------------------------------------------------- |
| id                         | Attribute  | Static readwrite-splitting name                                          |
| write-data-source-name     | Attribute  | Write data source name                                                   |
| read-data-source-names     | Attribute  | Read data source names, multiple data source names separated with comma  |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                              |

\<readwrite-splitting:dynamic-strategy />

| *Name*                           | *Type*     | *Description*                                                                                               |
| -------------------------------- | ---------- | ----------------------------------------------------------------------------------------------------------- |
| id                               | Attribute  | Dynamic readwrite-splitting name                                                                            |
| auto-aware-data-source-name      | Attribute  | Database discovery logic data source name                                                                   |
| write-data-source-query-enabled  | Attribute  | All read data source are offline, write data source whether the data source is responsible for read traffic |
| load-balance-algorithm-ref       | Attribute  | Load balance algorithm name                                                                                 |

\<readwrite-splitting:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/common-config/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Read-write splitting-Core features](/en/features/readwrite-splitting/) for more details about query consistent routing.

## Operating Procedures
1. Add read/write splitting data source.
2. Set the load balancing algorithm.
3. Using read/write splitting data sources.

## Configuration Example
```xml
<readwrite-splitting:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    
<readwrite-splitting:rule id="readWriteSplittingRule">
    <readwrite-splitting:data-source-rule id="demo_ds" load-balance-algorithm-ref="randomStrategy">
        <readwrite-splitting:static-strategy id="staticStrategy" write-data-source-name="demo_write_ds" read-data-source-names="demo_read_ds_0, demo_read_ds_1"/>
    </readwrite-splitting:data-source-rule>
</readwrite-splitting:rule>

<shardingsphere:data-source id="readWriteSplittingDataSource" data-source-names="demo_write_ds, demo_read_ds_0, demo_read_ds_1" rule-refs="readWriteSplittingRule" />
```

## Related References
- [Read-write splitting-Core features](/en/features/readwrite-splitting/)
- [Java API: read-write splitting](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
- [YAML Configuration: read-write splitting](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)
- [Spring Boot Starter: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting/)
