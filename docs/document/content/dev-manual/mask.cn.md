+++
pre = "<b>5.7 </b>"
title = "数据脱敏"
weight = 7
chapter = true
+++

## MaskAlgorithm

### 全限定类名

[`org.apache.shardingsphere.mask.spi.MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/api/src/main/java/org/apache/shardingsphere/mask/spi/MaskAlgorithm.java)

### 定义

数据脱敏算法

### 已知实现

| *配置标识*                                  | *详细说明*           | *全限定类名*                                                                                                                                                                                                                                                                                     |
|-----------------------------------------|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MD5                                     | 基于 MD5 的数据脱敏算法   | [`org.apache.shardingsphere.mask.algorithm.hash.MD5MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/hash/MD5MaskAlgorithm.java)                                                               |
| KEEP_FIRST_N_LAST_M                     | 保留前 n 后 m 数据脱敏算法 | [`org.apache.shardingsphere.mask.algorithm.cover.KeepFirstNLastMMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFirstNLastMMaskAlgorithm.java)                                     |
| KEEP_FROM_X_TO_Y                        | 保留自 x 至 y 数据脱敏算法 | [`org.apache.shardingsphere.mask.algorithm.cover.KeepFromXToYMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFromXToYMaskAlgorithm.java)                                           |
| MASK_FIRST_N_LAST_M                     | 遮盖前 n 后 m 数据脱敏算法 | [`org.apache.shardingsphere.mask.algorithm.cover.MaskFirstNLastMMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFirstNLastMMaskAlgorithm.java)                                     |
| MASK_FROM_X_TO_Y                        | 遮盖自 x 至 y 数据脱敏算法 | [`org.apache.shardingsphere.mask.algorithm.cover.MaskFromXToYMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFromXToYMaskAlgorithm.java)                                           |
| MASK_BEFORE_SPECIAL_CHARS               | 特殊字符前遮盖数据脱敏算法    | [`org.apache.shardingsphere.mask.algorithm.cover.MaskBeforeSpecialCharsAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskBeforeSpecialCharsAlgorithm.java)                               |
| MASK_AFTER_SPECIAL_CHARS                | 特殊字符后遮盖数据脱敏算法    | [`org.apache.shardingsphere.mask.algorithm.cover.MaskAfterSpecialCharsAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskAfterSpecialCharsAlgorithm.java)                                 |
| PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE | 身份证号随机替换数据脱敏算法   | [`org.apache.shardingsphere.mask.algorithm.replace.PersonalIdentityNumberRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/PersonalIdentityNumberRandomReplaceAlgorithm.java) |
| MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE | 军官证随机替换数据脱敏算法    | [`org.apache.shardingsphere.mask.algorithm.replace.MilitaryIdentityNumberRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/MilitaryIdentityNumberRandomReplaceAlgorithm.java) |
| TELEPHONE_RANDOM_REPLACE                | ⼿机号随机替换数据脱敏算法    | [`org.apache.shardingsphere.mask.algorithm.replace.TelephoneRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/TelephoneRandomReplaceAlgorithm.java)                           |
| LANDLINE_NUMBER_RANDOM_REPLACE          | 座机号码随机替换         | [`org.apache.shardingsphere.mask.algorithm.replace.LandlineNumberRandomAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/LandlineNumberRandomAlgorithm.java)                               |
| GENERIC_TABLE_RANDOM_REPLACE            | 通⽤表格随机替换         | [`org.apache.shardingsphere.mask.algorithm.replace.GenericTableRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/GenericTableRandomReplaceAlgorithm.java)                     |
| UNIFIED_CREDIT_CODE_RANDOM_REPLACE      | 统⼀信⽤码随机替换        | [`org.apache.shardingsphere.mask.algorithm.replace.UnifiedCreditCodeRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/UnifiedCreditCodeRandomReplaceAlgorithm.java)           |
