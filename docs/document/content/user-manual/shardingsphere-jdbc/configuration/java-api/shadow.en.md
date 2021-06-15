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
| sourceDataSourceNames | List\<String\> | Source data source names |
| shadowDataSourceNames | List\<String\> | Shadow data source names |