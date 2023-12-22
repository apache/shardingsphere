+++
title = "SHOW MASK ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### Description

The `SHOW MASK ALGORITHM IMPLEMENTATIONS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.mask.spi.MaskAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showMaskAlgorithmImplementations ::=
  'SHOW' 'MASK' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns     | Description                           |
|-------------|---------------------------------------|
| name        | class name of the implementation      |
| type        | type of the implementation            |
| class_path  | full class name of the implementation |

### Example

- Query all the implementations for `org.apache.shardingsphere.mask.spi.MaskAlgorithm` interface

```sql
SHOW MASK ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW MASK ALGORITHM IMPLEMENTATIONS;
+------------------------------------+------------------------------+-------------------------------------------------------------------------------------+
| name                               | type                         | class_path                                                                          |
+------------------------------------+------------------------------+-------------------------------------------------------------------------------------+
| MD5MaskAlgorithm                   | MD5                          | org.apache.shardingsphere.mask.algorithm.hash.MD5MaskAlgorithm                      |
| KeepFirstNLastMMaskAlgorithm       | KEEP_FIRST_N_LAST_M          | org.apache.shardingsphere.mask.algorithm.cover.KeepFirstNLastMMaskAlgorithm         |
| KeepFromXToYMaskAlgorithm          | KEEP_FROM_X_TO_Y             | org.apache.shardingsphere.mask.algorithm.cover.KeepFromXToYMaskAlgorithm            |
| MaskAfterSpecialCharsAlgorithm     | MASK_AFTER_SPECIAL_CHARS     | org.apache.shardingsphere.mask.algorithm.cover.MaskAfterSpecialCharsAlgorithm       |
| MaskBeforeSpecialCharsAlgorithm    | MASK_BEFORE_SPECIAL_CHARS    | org.apache.shardingsphere.mask.algorithm.cover.MaskBeforeSpecialCharsAlgorithm      |
| MaskFirstNLastMMaskAlgorithm       | MASK_FIRST_N_LAST_M          | org.apache.shardingsphere.mask.algorithm.cover.MaskFirstNLastMMaskAlgorithm         |
| MaskFromXToYMaskAlgorithm          | MASK_FROM_X_TO_Y             | org.apache.shardingsphere.mask.algorithm.cover.MaskFromXToYMaskAlgorithm            |
| GenericTableRandomReplaceAlgorithm | GENERIC_TABLE_RANDOM_REPLACE | org.apache.shardingsphere.mask.algorithm.replace.GenericTableRandomReplaceAlgorithm |
+------------------------------------+------------------------------+-------------------------------------------------------------------------------------+
8 rows in set (0.13 sec)
```

### Reserved word

`SHOW`, `MASK`, `ALGORITHM`, `IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
