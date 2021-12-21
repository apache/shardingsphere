+++
title = "HA"
weight = 3
+++

## Configuration Item Explanation

Namespace：[http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.0.0.xsd)

\<database-discovery:rule />

| *Name*                  | *Type*     | *Description*                         |
| ----------------------- | --------   | ------------------------------------- |
| id                      | Attribute   | Spring Bean Id                       |
| data-source-rule (+)    | tag         | Data source rule configuration        |
| discovery-heartbeat (+) | tag         | Detect heartbeat rule configuration   |

\<database-discovery:data-source-rule />

| *Name*                      | *Type*     | *Description*                                                                          |
| --------------------------- | ---------- | --------------------------------------------------------------------------------------- |
| id                          | Attribute  | Data source rule Id                                                                      |
| data-source-names           | Attribute  | Data source names, multiple data source names separated with comma. Such as: ds_0, ds_1  |
| discovery-heartbeat-name    | Attribute  | Detect heartbeat name                                                                    |
| discovery-type-name         | Attribute  | Highly available type name                                                               |

\<database-discovery:discovery-heartbeat />

| *Name*                      | *Type*     | *Description*                                      |
| --------------------------- | ---------- | -------------------------------------------------- |
| id                          | Attribute  | Detect heartbeat Id                                |
| props                       | tag        | Detect heartbeat attribute configuration, keep-alive-cron configuration, cron expression. Such as: '0/5 * * * * ?'  |

\<database-discovery:discovery-type />

| *Name*     | *Type*    | *Description*                                                    |
| --------- | ---------- | ---------------------------------------------------------------- |
| id        | Attribute  | Highly available type Id                                         |
| type      | Attribute  | Highly available type, such as: MGR、openGauss                   |
| props (?) | tag        | Required parameters for high-availability types, such as MGR's group-name   |
