+++
title = "Encryption"
weight = 5
+++

## Background

The data encryption Java API rule configuration allows users to directly create ShardingSphereDataSource objects by writing java code. The Java API configuration method is very flexible and can integrate various types of business systems without relying on additional jar packages.

## Parameters

### Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*                    | *DataType*                                  | *Description*                                                                                  | *Default Value* |
| ------------------------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------- | --------------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\> | Encrypt table rule configurations                                                              |                 |
| encryptors (+)            | Map\<String, AlgorithmConfiguration\>       | Encrypt algorithm name and configurations                                                      |                 |
| queryWithCipherColumn (?) | boolean                                     | Whether query with cipher column for data encrypt. User you can use plaintext to query if have | true            |

### Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

Attributes:

| *Name*                    | *DataType*                                   | *Description*                      |
| ------------------------- | -------------------------------------------- | ---------------------------------- |
| name                      | String                                       | Table name                         |
| columns (+)               | Collection\<EncryptColumnRuleConfiguration\> | Encrypt column rule configurations |
| queryWithCipherColumn (?) | boolean                                      | The current table whether query with cipher column for data encrypt |

### Encrypt Column Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

Attributes:

| *Name*                     | *DataType* | *Description*              |
| -------------------------- | ---------- | -------------------------- |
| logicColumn                | String     | Logic column name          |
| cipherColumn               | String     | Cipher column name         |
| assistedQueryColumn (?)    | String     | Assisted query column name |
| plainColumn (?)            | String     | Plain column name          |
| encryptorName              | String     | Encrypt algorithm name     |
| assistedQueryEncryptorName | String     | Assisted query encrypt algorithm name |
| queryWithCipherColumn (?)  | boolean    | The current column whether query with cipher column for data encrypt |

### Encrypt Algorithm Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

Attributes:

| *Name*     | *DataType* | *Description*                |
| ---------- | ---------- | ---------------------------- |
| name       | String     | Encrypt algorithm name       |
| type       | String     | Encrypt algorithm type       |
| properties | Properties | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/common-config/builtin-algorithm/encrypt) for more details about type of algorithm.

## Procedure

1. Create a real data source mapping relationship, where key is the logical name of the data source and value is the datasource object.
1. Create the encryption rule object EncryptRuleConfiguration, and initialize the encryption table object EncryptTableRuleConfiguration, encryption algorithm and other parameters in the object.
1. Call createDataSource of ShardingSphereDataSourceFactory to create  ShardingSphereDataSource.

## Sample

```java
public final class EncryptDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() {
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("username", "username", "", "username_plain", "name_encryptor", null);
        EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "", "pwd_encryptor", null);
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest), null);
        Map<String, AlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
        encryptAlgorithmConfigs.put("name_encryptor", new AlgorithmConfiguration("AES", props));
        encryptAlgorithmConfigs.put("pwd_encryptor", new AlgorithmConfiguration("assistedTest", props));
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

## Related References

- [The feature description of Data Encryption](/en/features/encrypt/ )
- [Dev Guide of Data Encryption](/en/dev-manual/encryption/)
