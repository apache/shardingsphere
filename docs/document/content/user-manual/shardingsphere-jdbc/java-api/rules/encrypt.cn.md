+++
title = "数据加密"
weight = 5
+++

## 背景信息

数据加密 Java API 规则配置允许用户直接通过编写 Java 代码的方式，完成 ShardingSphereDataSource 对象的创建，Java API 的配置方式非常灵活，不需要依赖额外的 jar 包 就能够集成各种类型的业务系统。

## 参数解释

### 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*                                   | *说明*                                                 | *默认值* |
| ------------------------- | ------------------------------------------- | ----------------------------------------------------- | ------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\> | 加密表规则配置                                           |        |
| encryptors (+)            | Map\<String, AlgorithmConfiguration\>       | 加解密算法名称和配置                                      |        |
| queryWithCipherColumn (?) | boolean                                     | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 | true   |

### 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

可配置属性：

| *名称*       | *数据类型*                                    | *说明*           |
| ----------- | -------------------------------------------- | --------------- |
| name        | String                                       | 表名称           |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | 加密列规则配置列表 |
| queryWithCipherColumn (?) | boolean                                             | 该表是否使用加密列进行查询 |

### 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

可配置属性：

| *名称*                    | *数据类型* | *说明*        |
| ------------------------- | -------- | ------------- |
| logicColumn               | String   | 逻辑列名称     |
| cipherColumn              | String   | 密文列名称     |
| assistedQueryColumn (?)   | String   | 查询辅助列名称 |
| likeQueryColumn (?)      | String   | 模糊查询列名称 |
| plainColumn (?)           | String   | 原文列名称     |
| encryptorName             | String   | 密文列加密算法名称   |
| assistedQueryEncryptorName| String   | 查询辅助列加密算法名称   |
| likeQueryEncryptorName   | String   | 模糊查询列加密算法名称   |
| queryWithCipherColumn (?) | boolean                                             | 该列是否使用加密列进行查询 |

### 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*           |
| ---------- | ---------- | ---------------- |
| name       | String     | 加解密算法名称     |
| type       | String     | 加解密算法类型     |
| properties | Properties | 加解密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/common-config/builtin-algorithm/encrypt)。

## 操作步骤

1. 创建真实数据源映射关系，key 为数据源逻辑名称，value 为 DataSource 对象；
1. 创建加密规则对象 EncryptRuleConfiguration，并初始化对象中的加密表对象 EncryptTableRuleConfiguration、加密算法等参数；
1. 调用 ShardingSphereDataSourceFactory 对象的 createDataSource 方法，创建 ShardingSphereDataSource。

## 配置示例

```java
public final class EncryptDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("username", "username", "", "", "username_plain", "name_encryptor", null);
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "like_pwd", "", "pwd_encryptor", null);
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest), null);
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
        encryptAlgorithmConfigs.put("name_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("pwd_encryptor", new AlgorithmConfiguration("assistedTest", props));
        encryptAlgorithmConfigs.put("like_encryptor", new AlgorithmConfiguration("CHAR_DIGEST_LIKE", new Properties()));
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
        try {
            return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(encryptRuleConfig), props);
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
```

## 相关参考

- [数据加密的核心特性](/cn/features/sharding/ )
- [数据加密的开发者指南](/cn/dev-manual/encryption/)