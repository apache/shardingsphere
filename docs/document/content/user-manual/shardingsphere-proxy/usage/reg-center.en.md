title = "Using Registry Center"
weight = 2
+++

If users want to use the database orchestration function of ShardingSphere-Proxy, they need to implement instance disabling and slave database disabling functions in the registry center. 
Please refer to [Available Registry Centers](/en/features/orchestration/supported-registry-repo/) for more details.

## Zookeeper

1. ShardingSphere-Proxy has provided the registry center solution of Zookeeper in default. 
Users only need to follow [Configuration Rules](/en/user-manual/shardingsphere-proxy/configuration/) to set the registry center and use it.

## Other Third Party Registry Center

1. Delete `shardingsphere-orchestration-reg-zookeeper-curator-${shardingsphere.version}.jar` under the lib folder of ShardingSphere-Proxy.
1. Use SPI methods in logic coding and put the generated jar package to the lib folder of ShardingSphere-Proxy.
1. Follow [Configuration Rules](/en/user-manual/shardingsphere-proxy/configuration/) to set the registry center and use it.
