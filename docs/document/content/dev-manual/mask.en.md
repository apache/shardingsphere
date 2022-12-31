+++
pre = "<b>5.15. </b>"
title = "Data Masking"
weight = 15
chapter = true
+++

## MaskAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.mask.spi.MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/api/src/main/java/org/apache/shardingsphere/mask/spi/MaskAlgorithm.java)

### Definition

Data masking algorithm definition

### Implementation classes

| *Configuration Type*                    | *Description*                                           | *Fully-qualified class name*                                                                                                                                                                                                                                       |
|-----------------------------------------|---------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MD5                                     | Data masking algorithm based on MD5                     | [`org.apache.shardingsphere.mask.algorithm.hash.MD5MaskAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/hash/MD5MaskAlgorithm.java)                 |
| KEEP_FIRST_N_LAST_M                     | Keep first n last m data masking algorithm              | [`org.apache.shardingsphere.mask.algorithm.cover.KEEP_FIRST_N_LAST_M`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFirstNLastMMaskAlgorithm.java)                 |
| KEEP_FROM_X_TO_Y                        | Keep from x to y data masking algorithm                 | [`org.apache.shardingsphere.mask.algorithm.cover.KEEP_FROM_X_TO_Y`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/KeepFromXToYMaskAlgorithm.java)                 |
| MASK_FIRST_N_LAST_M                     | Mask first n last m data masking algorithm              | [`org.apache.shardingsphere.mask.algorithm.cover.MASK_FIRST_N_LAST_M`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFirstNLastMMaskAlgorithm.java)                 |
| MASK_FROM_X_TO_Y                        | Mask from x to y data masking algorithm                 | [`org.apache.shardingsphere.mask.algorithm.cover.MASK_FROM_X_TO_Y`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskFromXToYMaskAlgorithm.java)                 |
| MASK_BEFORE_SPECIAL_CHARS               | Mask before special chars data masking algorithm        | [`org.apache.shardingsphere.mask.algorithm.cover.MASK_BEFORE_SPECIAL_CHARS`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskBeforeSpecialCharsAlgorithm.java)                 |
| MASK_AFTER_SPECIAL_CHARS                | Mask after special chars data masking algorithm         | [`org.apache.shardingsphere.mask.algorithm.cover.MASK_AFTER_SPECIAL_CHARS`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/cover/MaskAfterSpecialCharsAlgorithm.java)                 |
| PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE | Personal identity number random replace data masking algorithm | [`org.apache.shardingsphere.mask.algorithm.replace.PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/PersonalIdentityNumberRandomReplaceAlgorithm.java)                 |
| TELEPHONE_RANDOM_REPLACE                | Telephone random replace data masking algorithm         | [`org.apache.shardingsphere.mask.algorithm.replace.TELEPHONE_RANDOM_REPLACE`](https://github.com/apache/shardingsphere/blob/master/features/mask/core/src/main/java/org/apache/shardingsphere/mask/algorithm/replace/TelephoneRandomReplaceAlgorithm.java)                 |
