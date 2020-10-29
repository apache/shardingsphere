+++
title = "Governance"
weight = 6
+++

## Configuration Item Explanation

### Management

```properties
spring.shardingsphere.governance.name= # Governance name
spring.shardingsphere.governance.registry-center.type= # Governance instance type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.governance.registry-center.server-lists= # The list of servers that connect to governance instance, including IP and port number; use commas to separate
spring.shardingsphere.governance.registry-center.props= # Other properties
spring.shardingsphere.governance.additional-config-center.type= # Additional config center type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.governance.additional-config-center.server-lists= # Additional config center server list. including IP and port number; use commas to separate
spring.shardingsphere.governance.additional-config-center.props= # Additional config center other properties
spring.shardingsphere.governance.overwrite= # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
