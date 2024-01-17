+++
title = "数据脱敏"
weight = 6
+++

## 背景信息

数据脱敏 Java API 规则配置允许用户直接通过编写 Java 代码的方式，完成 ShardingSphereDataSource 对象的创建，Java API 的配置方式非常灵活，不需要依赖额外的 jar 包 就能够集成各种类型的业务系统。

## 参数解释

### 配置入口

类名称：org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration

可配置属性：

| *名称*               | *数据类型*                                   | *说明*      | *默认值* |
|--------------------|------------------------------------------|-----------|-------|
| tables (+)         | Collection\<MaskTableRuleConfiguration\> | 脱敏表规则配置   |       |
| maskAlgorithms (+) | Map\<String, AlgorithmConfiguration\>    | 脱敏算法名称和配置 |       |

### 脱敏表规则配置

类名称：org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration

可配置属性：

| *名称*        | *数据类型*                                    | *说明*      |
|-------------|-------------------------------------------|-----------|
| name        | String                                    | 表名称       |
| columns (+) | Collection\<MaskColumnRuleConfiguration\> | 脱敏列规则配置列表 |

### 脱敏列规则配置

类名称：org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration

可配置属性：

| *名称*          | *数据类型* | *说明*   |
|---------------|--------|--------|
| logicColumn   | String | 逻辑列名称  |
| maskAlgorithm | String | 脱敏算法名称 |

### 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

可配置属性：

| *名称*       | *数据类型*     | *说明*     |
|------------|------------|----------|
| name       | String     | 脱敏算法名称   |
| type       | String     | 脱敏算法类型   |
| properties | Properties | 脱敏算法属性配置 |

算法类型的详情，请参见[内置脱敏算法列表](/cn/user-manual/common-config/builtin-algorithm/mask)。

## 操作步骤

1. 创建真实数据源映射关系，key 为数据源逻辑名称，value 为 DataSource 对象；
2. 创建脱敏规则对象 MaskRuleConfiguration，并初始化对象中的脱敏表对象 MaskTableRuleConfiguration、脱敏算法等参数；
3. 调用 ShardingSphereDataSourceFactory 对象的 createDataSource 方法，创建 ShardingSphereDataSource。

## 配置示例

```java
import java.sql.SQLException;
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

## 相关参考

- [数据脱敏的核心特性](/cn/features/mask/ )
- [数据脱敏的开发者指南](/cn/dev-manual/mask/)