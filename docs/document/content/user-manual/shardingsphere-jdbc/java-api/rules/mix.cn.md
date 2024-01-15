+++
title = "混合规则"
weight = 10
+++

## 背景信息

ShardingSphere 涵盖了很多功能，例如，分库分片、读写分离、数据加密等。这些功能用户可以单独进行使用，也可以配合一起使用，下面是基于 JAVA API 的配置示例。

## 配置示例

```java
// 分片配置
private ShardingRuleConfiguration createShardingRuleConfiguration() {
    ShardingRuleConfiguration result = new ShardingRuleConfiguration();
    result.getTables().add(getOrderTableRuleConfiguration());
    result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
    result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "standard_test_tbl"));
    Properties props = new Properties();
    props.setProperty("algorithm-expression", "demo_ds_${user_id % 2}");
    result.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", props));
    result.getShardingAlgorithms().put("standard_test_tbl", new AlgorithmConfiguration("STANDARD_TEST_TBL", new Properties()));
    result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
    return result;
}

private ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
    ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "demo_ds_${0..1}.t_order_${[0, 1]}");
    result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
    return result;
}

// 读写分离配置
private static ReadwriteSplittingRuleConfiguration createReadwriteSplittingConfiguration() {
    ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration1 = new ReadwriteSplittingDataSourceRuleConfiguration("replica_ds_0", Arrays.asList("readwrite_ds_0"), true), "");
    ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration2 = new ReadwriteSplittingDataSourceRuleConfiguration("replica_ds_1", Arrays.asList("readwrite_ds_1"), true), "");
    Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
    dataSources.add(dataSourceRuleConfiguration1);
    dataSources.add(dataSourceRuleConfiguration2);
    return new ReadwriteSplittingRuleConfiguration(dataSources, Collections.emptyMap());
}

// 数据加密配置
private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
    Properties props = new Properties();
    props.setProperty("aes-key-value", "123456");
    EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("username", new EncryptColumnItemRuleConfiguration("username", "name_encryptor"));
    EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd", "pwd_encryptor"));
    columnConfigTest.setAssistedQuery(new EncryptColumnItemRuleConfiguration("assisted_query_pwd", "pwd_encryptor"));
    columnConfigTest.setLikeQuery(new EncryptColumnItemRuleConfiguration("like_pwd", "like_encryptor"));
    EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest));
    Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new HashMap<>();
    encryptAlgorithmConfigs.put("name_encryptor", new AlgorithmConfiguration("AES", props));
    encryptAlgorithmConfigs.put("pwd_encryptor", new AlgorithmConfiguration("assistedTest", props));
    encryptAlgorithmConfigs.put("like_encryptor", new AlgorithmConfiguration("CHAR_DIGEST_LIKE", new Properties()));
    return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
}
```
