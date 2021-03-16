+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

```properties
spring.shardingsphere.governance.name= # Governance name
spring.shardingsphere.governance.registry-center.type= # Governance instance type. Example:Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.governance.registry-center.server-lists= # The list of servers that connect to governance instance, including IP and port number; use commas to separate
spring.shardingsphere.governance.registry-center.props= # Other properties
spring.shardingsphere.governance.overwrite= # Whether to overwrite local configurations with config center configurations; if it can, each initialization should refer to local configurations
```
