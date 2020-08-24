+++
title = "Shadow DB"
weight = 4
+++

## Root Configuration

Class name: org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

Attributes:

| *Name*          | *DataType*            | *Description*                                                                                                                                                                    |
| --------------- | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| column          | String                | Shadow field name in SQL, SQL with a value of true will be routed to the shadow database for execution                                                                           |
| shadowMappings  | Map\<String, String\> | Mapping relationship between production database and shadow database, key is the name of the production database, and value is the name of the shadow database |

