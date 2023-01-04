+++
title = "Data Masking Algorithm"
weight = 9
+++

## Background

Data masking algorithms are by the mask features of Apache ShardingSphere. A variety of algorithms are built-in to make it easy for users to fully leverage the feature.

## Parameters

### Hash Data Masking Algorithm

#### MD5 Data Masking Algorithm

Type: MD5

Attributes:

| *Name* | *DataType* | *Description*         |
|--------| ---------- |-----------------------|
| salt   | String     | Salt value (optional) |

### Mask Data Masking Algorithm

#### Keep First N Last M Data Masking Algorithm

Type: KEEP_FIRST_N_LAST_M

Attributes:

| *Name* | *DataType* | *Description*     |
|--------------|--------|-------------------|
| first-n      | int    | first n substring |
| last-m       | int    | last m substring  |
| replace-char | String | replace char      |

#### Keep From X To Y Data Masking Algorithm

Type: KEEP_FROM_X_TO_Y

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|-------------------------|
| from-x       | int    | start position (from 0) |
| to-y         | int    | end position (from 0)   |
| replace-char | String | replace char            |

#### Mask First N Last M Data Masking Algorithm

Type: MASK_FIRST_N_LAST_M

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|----------------|
| first-n      | int    | first n substring |
| last-m       | int    | last m substring |
| replace-char | String | replace char  |

#### Mask From X To Y Data Masking Algorithm

Type: MASK_FROM_X_TO_Y

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|----------------|
| from-x       | int    | start position (from 0) |
| to-y         | int    | end position (from 0)   |
| replace-char | String | replace char            |

#### Mask Before Special Chars Data Masking Algorithm

Type: MASK_BEFORE_SPECIAL_CHARS

Attributes:

| *Name* | *DataType* | *Description*                    |
|--------------|--------|----------------------------------|
| special-chars       | String | Special chars (first appearance) |
| replace-char | String | replace char                             |

#### Mask After Special Chars Data Masking Algorithm

Type: MASK_AFTER_SPECIAL_CHARS

Attributes:

| *Name* | *DataType* | *Description*                    |
|--------------|--------|----------------------------------|
| special-chars       | String | Special chars (first appearance) |
| replace-char | String | replace char                     |

### Replace Data Masking Algorithm

#### Personal Identity Number Random Replace Data Masking Algorithm

Type: PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE

Attributes:

| *Name*                      | *DataType* | *Description*                                             |
|-----------------------------|--------|-----------------------------------------------------------|
| alpha-two-country-area-code | String | alpha two country area code (Optional, default value: CN) |

#### Military Identity Number Random Replace Data Masking Algorithm

类型：MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE

可配置属性：

| *Name*                        | *DataType* | *Description*                                              |
|-------------------------------|------------|------------------------------------------------------------|
| type-codes                    | String     | military identity number type codes (separate with comma)  |

#### Telephone Random Replace Data Masking Algorithm

Type: TELEPHONE_RANDOM_REPLACE

Attributes:

| *Name*          | *DataType* | *Description*                         |
|-----------------|--------|---------------------------------------|
| network-numbers | String | Network numbers (separate with comma) |

## Operating Procedure
1. Configure maskAlgorithms in a mask rule.
2. Use relevant algorithm types in maskAlgorithms.

## Configuration Examples
```yaml
rules:
- !MASK
  tables:
    t_user:
      columns:
        password:
          maskAlgorithm: md5_mask
        email:
          maskAlgorithm: mask_before_special_chars_mask
        telephone:
          maskAlgorithm: keep_first_n_last_m_mask

  maskAlgorithms:
    md5_mask:
      type: MD5
    mask_before_special_chars_mask:
      type: MASK_BEFORE_SPECIAL_CHARS
      props:
        special-chars: '@'
        replace-char: '*'
    keep_first_n_last_m_mask:
      type: KEEP_FIRST_N_LAST_M
      props:
        first-n: 3
        last-m: 4
        replace-char: '*'
```

## Related References
- [Core Feature: Data Masking](/en/features/mask/)
- [Developer Guide: Data Masking](/en/dev-manual/mask/)
