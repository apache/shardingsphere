+++
pre = "<b>5.7. </b>"
title = "Data Masking"
weight = 7
chapter = true
+++

## MaskAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.mask.spi.MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/api/src/main/java/org/apache/shardingsphere/mask/spi/MaskAlgorithm.java)

### Definition

Data masking algorithm definition

### Implementation classes

| *Configuration Type*                    | *Description*                                                  | *Fully-qualified class name*                                                                                                                                                                                                                                                                |
|-----------------------------------------|----------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MD5                                     | Data masking algorithm based on MD5                            | [`org.apache.shardingsphere.mask.algorithm.hash.MD5MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/hash/MD5MaskAlgorithm.java)                                                               |
| KEEP_FIRST_N_LAST_M                     | Keep first n last m data masking algorithm                     | [`org.apache.shardingsphere.mask.algorithm.cover.KeepFirstNLastMMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFirstNLastMMaskAlgorithm.java)                                     |
| KEEP_FROM_X_TO_Y                        | Keep from x to y data masking algorithm                        | [`org.apache.shardingsphere.mask.algorithm.cover.KeepFromXToYMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFromXToYMaskAlgorithm.java)                                           |
| MASK_FIRST_N_LAST_M                     | Mask first n last m data masking algorithm                     | [`org.apache.shardingsphere.mask.algorithm.cover.MaskFirstNLastMMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFirstNLastMMaskAlgorithm.java)                                     |
| MASK_FROM_X_TO_Y                        | Mask from x to y data masking algorithm                        | [`org.apache.shardingsphere.mask.algorithm.cover.MaskFromXToYMaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFromXToYMaskAlgorithm.java)                                           |
| MASK_BEFORE_SPECIAL_CHARS               | Mask before special chars data masking algorithm               | [`org.apache.shardingsphere.mask.algorithm.cover.MaskBeforeSpecialCharsAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskBeforeSpecialCharsAlgorithm.java)                               |
| MASK_AFTER_SPECIAL_CHARS                | Mask after special chars data masking algorithm                | [`org.apache.shardingsphere.mask.algorithm.cover.MaskAfterSpecialCharsAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskAfterSpecialCharsAlgorithm.java)                                 |
| PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE | Personal identity number random replace data masking algorithm | [`org.apache.shardingsphere.mask.algorithm.replace.PersonalIdentityNumberRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/PersonalIdentityNumberRandomReplaceAlgorithm.java) |
| MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE | Military identity number random replace data masking algorithm | [`org.apache.shardingsphere.mask.algorithm.replace.MilitaryIdentityNumberRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/MilitaryIdentityNumberRandomReplaceAlgorithm.java) |
| TELEPHONE_RANDOM_REPLACE                | Telephone random replace data masking algorithm                | [`org.apache.shardingsphere.mask.algorithm.replace.TelephoneRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/TelephoneRandomReplaceAlgorithm.java)                           |
| LANDLINE_NUMBER_RANDOM_REPLACE          | Landline number random replace data masking algorithm          | [`org.apache.shardingsphere.mask.algorithm.replace.LandlineNumberRandomAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/LandlineNumberRandomAlgorithm.java)                               |
| GENERIC_TABLE_RANDOM_REPLACE            | Generic table random replace algorithm                         | [`org.apache.shardingsphere.mask.algorithm.replace.GenericTableRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/GenericTableRandomReplaceAlgorithm.java)                     |
| UNIFIED_CREDIT_CODE_RANDOM_REPLACE      | Unified credit code random replace algorithm                   | [`org.apache.shardingsphere.mask.algorithm.replace.UnifiedCreditCodeRandomReplaceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/UnifiedCreditCodeRandomReplaceAlgorithm.java)           |
