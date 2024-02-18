+++
title = "Data Masking"
weight = 6
+++

## Background

The data masking Java API rule configuration allows users to directly create ShardingSphereDataSource objects by writing java code. The Java API configuration method is very flexible and can integrate various types of business systems without relying on additional jar packages.

## Parameters

### Root Configuration

Class name: org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration

Attributes:

| *Name*             | *DataType*                               | *Description*                          | *Default Value* |
|--------------------|------------------------------------------|----------------------------------------|-----------------|
| tables (+)         | Collection\<MaskTableRuleConfiguration\> | Mask table rule configurations         |                 |
| maskAlgorithms (+) | Map\<String, AlgorithmConfiguration\>    | Mask algorithm name and configurations |                 |

### Mask Table Rule Configuration

Class name: org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                | *Description*                   |
|-------------|-------------------------------------------|---------------------------------|
| name        | String                                    | Table name                      |
| columns (+) | Collection\<MaskColumnRuleConfiguration\> | Mask column rule configurations |

### Mask Column Rule Configuration

Class name: org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration

Attributes:

| *Name*        | *DataType* | *Description*       |
|---------------|------------|---------------------|
| logicColumn   | String     | Logic column name   |
| maskAlgorithm | String     | Mask algorithm name |

### Mask Algorithm Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

Attributes:

| *Name*     | *DataType* | *Description*             |
|------------|------------|---------------------------|
| name       | String     | Mask algorithm name       |
| type       | String     | Mask algorithm type       |
| properties | Properties | Mask algorithm properties |

Please refer to [Built-in Data Masking Algorithm List](/en/user-manual/common-config/builtin-algorithm/mask) for more details about type of algorithm.

## Procedure

1. Create a real data source mapping relationship, where key is the logical name of the data source and value is the datasource object.
2. Create the data masking rule object MaskRuleConfiguration, and initialize the mask table object MaskTableRuleConfiguration, mask algorithm and other parameters in the object.
3. Call createDataSource of ShardingSphereDataSourceFactory to create  ShardingSphereDataSource.

## Sample

```java
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

public final class MaskDatabasesConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        MaskColumnRuleConfiguration passwordColumn = new MaskColumnRuleConfiguration("password", "md5_mask");
        MaskColumnRuleConfiguration emailColumn = new MaskColumnRuleConfiguration("email", "mask_before_special_chars_mask");
        MaskColumnRuleConfiguration telephoneColumn = new MaskColumnRuleConfiguration("telephone", "keep_first_n_last_m_mask");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("t_user", Arrays.asList(passwordColumn, emailColumn, telephoneColumn));
        Map<String, AlgorithmConfiguration> maskAlgorithmConfigs = new LinkedHashMap<>(3, 1);
        maskAlgorithmConfigs.put("md5_mask", new AlgorithmConfiguration("MD5", new Properties()));
        Properties beforeSpecialCharsProps = new Properties();
        beforeSpecialCharsProps.put("special-chars", "@");
        beforeSpecialCharsProps.put("replace-char", "*");
        maskAlgorithmConfigs.put("mask_before_special_chars_mask", new AlgorithmConfiguration("MASK_BEFORE_SPECIAL_CHARS", beforeSpecialCharsProps));
        Properties keepFirstNLastMProps = new Properties();
        keepFirstNLastMProps.put("first-n", "3");
        keepFirstNLastMProps.put("last-m", "4");
        keepFirstNLastMProps.put("replace-char", "*");
        maskAlgorithmConfigs.put("keep_first_n_last_m_mask", new AlgorithmConfiguration("KEEP_FIRST_N_LAST_M", keepFirstNLastMProps));
        MaskRuleConfiguration maskRuleConfig = new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), maskAlgorithmConfigs);
        return ShardingSphereDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(maskRuleConfig), new Properties());
    }
}
```

## Related References

- [The feature description of Data Masking](/en/features/mask/ )
- [Dev Guide of Data Masking](/en/dev-manual/mask/)
