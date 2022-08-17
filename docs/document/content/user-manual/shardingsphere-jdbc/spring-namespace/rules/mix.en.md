+++
title = "Mixed Rules"
weight = 8
+++

## Background

ShardingSphere provides a variety of features, such as data sharding, read/write splitting, high availability, and data decryption. These features can be used independently or in combination. 

Below, you will find the configuration samples based on Spring Namespace.

## Samples

```xml
<!-- Sharding configuration -->
<sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="inlineStrategyShardingAlgorithm" />
<sharding:sharding-algorithm id="inlineStrategyShardingAlgorithm" type="INLINE">
    <props>
        <prop key="algorithm-expression">replica_ds_${user_id % 2}</prop>
    </props>
</sharding:sharding-algorithm>
<sharding:key-generate-algorithm id="snowflakeAlgorithm" type="SNOWFLAKE">
</sharding:key-generate-algorithm>
<sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
<sharding:rule id="shardingRule">
    <sharding:table-rules>
        <sharding:table-rule logic-table="t_order" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="orderKeyGenerator" />
    </sharding:table-rules>
</sharding:rule>

<!-- Dynamic read/write splitting configuration -->
<readwrite-splitting:rule id="readWriteSplittingRule">
    <readwrite-splitting:data-source-rule id="replica_ds_0">
        <readwrite-splitting:dynamic-strategy id="dynamicStrategy" auto-aware-data-source-name="readwrite_ds_0" />
    </readwrite-splitting:data-source-rule>
    <readwrite-splitting:data-source-rule id="replica_ds_1">
        <readwrite-splitting:dynamic-strategy id="dynamicStrategy" auto-aware-data-source-name="readwrite_ds_1" />
    </readwrite-splitting:data-source-rule>
</readwrite-splitting:rule>

<!-- Database discovery configuration -->
<database-discovery:rule id="mgrDatabaseDiscoveryRule">
    <database-discovery:data-source-rule id="readwrite_ds_0" data-source-names="ds_0,ds_1,ds_2" discovery-heartbeat-name="mgr-heartbeat" discovery-type-name="mgr" />
    <database-discovery:data-source-rule id="readwrite_ds_1" data-source-names="ds_3,ds_4,ds_5" discovery-heartbeat-name="mgr-heartbeat" discovery-type-name="mgr" />
    <database-discovery:discovery-heartbeat id="mgr-heartbeat">
        <props>
            <prop key="keep-alive-cron" >0/5 * * * * ?</prop>
        </props>
    </database-discovery:discovery-heartbeat>
</database-discovery:rule>
<database-discovery:discovery-type id="mgr" type="MySQL.MGR">
    <props>
        <prop key="group-name">558edd3c-02ec-11ea-9bb3-080027e39bd2</prop>
    </props>
</database-discovery:discovery-type>

<!-- Data decryption configuration -->
<encrypt:encrypt-algorithm id="name_encryptor" type="AES">
    <props>
        <prop key="aes-key-value">123456</prop>
    </props>
</encrypt:encrypt-algorithm>
<encrypt:encrypt-algorithm id="pwd_encryptor" type="assistedTest" />

<encrypt:rule id="encryptRule">
    <encrypt:table name="t_user">
        <encrypt:column logic-column="username" cipher-column="username" plain-column="username_plain" encrypt-algorithm-ref="name_encryptor" />
        <encrypt:column logic-column="pwd" cipher-column="pwd" assisted-query-column="assisted_query_pwd" encrypt-algorithm-ref="pwd_encryptor" />
    </encrypt:table>
</encrypt:rule>
```
