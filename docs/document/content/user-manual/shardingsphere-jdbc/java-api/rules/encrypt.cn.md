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

| *名称*                      | *数据类型*                                      | *说明*                              | *默认值* |
|---------------------------|---------------------------------------------|-----------------------------------|-------|
| tables (+)                | Collection\<EncryptTableRuleConfiguration\> | 加密表规则配置                           |       |
| encryptors (+)            | Map\<String, AlgorithmConfiguration\>       | 加解密算法名称和配置                        |       |

### 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

可配置属性：

| *名称*                      | *数据类型*                                       | *说明*          |
|---------------------------|----------------------------------------------|---------------|
| name                      | String                                       | 表名称           |
| columns (+)               | Collection\<EncryptColumnRuleConfiguration\> | 加密列规则配置列表     |

### 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

可配置属性：

| *名称*              | *数据类型*  | *说明*        |
|-------------------|---------|-------------|
| name              | String  | 逻辑列名称       |
| cipher            | EncryptColumnItemRuleConfiguration  | 密文列配置       |
| assistedQuery (?) | EncryptColumnItemRuleConfiguration  | 查询辅助列配置     |
| likeQuery (?)     | EncryptColumnItemRuleConfiguration  | 模糊查询列配置     |

### 加密列属性规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration

可配置属性：

| *名称*            | *数据类型*                             | *说明*    |
|-----------------|------------------------------------|---------|
| name            | String                             | 加密列属性名称 |
| encryptorName   | String                             | 加密列算法名称 |

### 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

可配置属性：

| *名称*       | *数据类型*     | *说明*      |
|------------|------------|-----------|
| name       | String     | 加解密算法名称   |
| type       | String     | 加解密算法类型   |
| properties | Properties | 加解密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/common-config/builtin-algorithm/encrypt)。

## 操作步骤

1. 创建真实数据源映射关系，key 为数据源逻辑名称，value 为 DataSource 对象；
2. 创建加密规则对象 EncryptRuleConfiguration，并初始化对象中的加密表对象 EncryptTableRuleConfiguration、加密算法等参数；
3. 调用 ShardingSphereDataSourceFactory 对象的 createDataSource 方法，创建 ShardingSphereDataSource。

## 配置示例

```java
public final class EncryptDatabasesConfiguration {
    
    public DataSource getDataSource() throws SQLException {
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
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);
        return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(encryptRuleConfig), props);
    }
}
```

## 相关参考

- [数据加密的核心特性](/cn/features/sharding/ )
- [数据加密的开发者指南](/cn/dev-manual/encrypt/)