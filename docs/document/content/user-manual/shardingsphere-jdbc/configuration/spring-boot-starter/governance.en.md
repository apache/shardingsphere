+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```properties
spring.shardingsphere.orchestration.name= # Orchestration name
spring.shardingsphere.orchestration.registry-center.type= # Orchestration instance type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.orchestration.registry-center.server-lists= # The list of servers that connect to orchestration instance, including IP and port number; use commas to separate
spring.shardingsphere.orchestration.registry-center.props= # Other properties
spring.shardingsphere.orchestration.additional-config-center.type= # Additional config center type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.orchestration.additional-config-center.server-lists= # Additional config center server list. including IP and port number; use commas to separate
spring.shardingsphere.orchestration.additional-config-center.props= # Additional config center other properties
spring.shardingsphere.orchestration.overwrite= # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
