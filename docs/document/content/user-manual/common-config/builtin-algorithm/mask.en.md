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

| *Name* | *DataType* | *Description*        |
|--------| ---------- |----------------------|
| salt   | String     | Salt value(optional) |

### Mask Data Masking Algorithm

#### KEEP_FIRST_N_LAST_M Data Masking Algorithm

Type: KEEP_FIRST_N_LAST_M

Attributes:

| *Name* | *DataType* | *Description*     |
|--------------|--------|-------------------|
| first-n      | int    | first n substring |
| last-m       | int    | last m substring  |
| replace-char | String | replace char      |

#### KEEP_FROM_X_TO_Y Data Masking Algorithm

Type: KEEP_FROM_X_TO_Y

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|-------------------------|
| from-x       | int    | start position (from 0) |
| to-y         | int    | end position (from 0)   |
| replace-char | String | replace char            |

#### MASK_FIRST_N_LAST_M Data Masking Algorithm

Type: MASK_FIRST_N_LAST_M

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|----------------|
| first-n      | int    | first n substring |
| last-m       | int    | last m substring |
| replace-char | String | replace char  |

#### MASK_FROM_X_TO_Y Data Masking Algorithm

Type: MASK_FROM_X_TO_Y

Attributes:

| *Name* | *DataType* | *Description*           |
|--------------|--------|----------------|
| from-x       | int    | start position (from 0) |
| to-y         | int    | end position (from 0)   |
| replace-char | String | replace char            |

#### MASK_BEFORE_SPECIAL_CHARS Data Masking Algorithm

Type: MASK_BEFORE_SPECIAL_CHARS

Attributes:

| *Name* | *DataType* | *Description*                    |
|--------------|--------|----------------------------------|
| special-chars       | String | Special chars (first appearance) |
| replace-char | String | replace char                             |

#### MASK_AFTER_SPECIAL_CHARS Data Masking Algorithm

Type: MASK_AFTER_SPECIAL_CHARS

Attributes:

| *Name* | *DataType* | *Description*                    |
|--------------|--------|----------------------------------|
| special-chars       | String | Special chars (first appearance) |
| replace-char | String | replace char                     |

### Replace Data Masking Algorithm

TODO

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
