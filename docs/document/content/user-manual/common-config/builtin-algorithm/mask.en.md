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
|--------|------------|-----------------------|
| salt   | String     | Salt value (optional) |

### Mask Data Masking Algorithm

#### Keep First N Last M Data Masking Algorithm

Type: KEEP_FIRST_N_LAST_M

Attributes:

| *Name*       | *DataType* | *Description*     |
|--------------|------------|-------------------|
| first-n      | int        | first n substring |
| last-m       | int        | last m substring  |
| replace-char | String     | replace char      |

#### Keep From X To Y Data Masking Algorithm

Type: KEEP_FROM_X_TO_Y

Attributes:

| *Name*       | *DataType* | *Description*           |
|--------------|------------|-------------------------|
| from-x       | int        | start position (from 0) |
| to-y         | int        | end position (from 0)   |
| replace-char | String     | replace char            |

#### Mask First N Last M Data Masking Algorithm

Type: MASK_FIRST_N_LAST_M

Attributes:

| *Name*       | *DataType* | *Description*     |
|--------------|------------|-------------------|
| first-n      | int        | first n substring |
| last-m       | int        | last m substring  |
| replace-char | String     | replace char      |

#### Mask From X To Y Data Masking Algorithm

Type: MASK_FROM_X_TO_Y

Attributes:

| *Name*       | *DataType* | *Description*           |
|--------------|------------|-------------------------|
| from-x       | int        | start position (from 0) |
| to-y         | int        | end position (from 0)   |
| replace-char | String     | replace char            |

#### Mask Before Special Chars Data Masking Algorithm

Type: MASK_BEFORE_SPECIAL_CHARS

Attributes:

| *Name*        | *DataType* | *Description*                    |
|---------------|------------|----------------------------------|
| special-chars | String     | Special chars (first appearance) |
| replace-char  | String     | replace char                     |

#### Mask After Special Chars Data Masking Algorithm

Type: MASK_AFTER_SPECIAL_CHARS

Attributes:

| *Name*        | *DataType* | *Description*                    |
|---------------|------------|----------------------------------|
| special-chars | String     | Special chars (first appearance) |
| replace-char  | String     | replace char                     |

### Replace Data Masking Algorithm

#### Personal Identity Number Random Replace Data Masking Algorithm

Type: PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE

Attributes:

| *Name*                      | *DataType* | *Description*                                             |
|-----------------------------|------------|-----------------------------------------------------------|
| alpha-two-country-area-code | String     | alpha two country area code (Optional, default value: CN) |

#### Military Identity Number Random Replace Data Masking Algorithm

类型：MILITARY_IDENTITY_NUMBER_RANDOM_REPLACE

可配置属性：

| *Name*                        | *DataType* | *Description*                                              |
|-------------------------------|------------|------------------------------------------------------------|
| type-codes                    | String     | military identity number type codes (separate with comma)  |

#### Telephone Random Replace Data Masking Algorithm

Type: TELEPHONE_RANDOM_REPLACE

Attributes:

| *Name*          | *DataType* | *Description*                                                                                                                                                                                             |
|-----------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| network-numbers | String     | Network numbers (separate with comma, default value: 130,131,132,133,134,135,136,137,138,139,150,151,152,153,155,156,157,158,159,166,170,176,177,178,180,181,182,183,184,185,186,187,188,189,191,198,199) |

#### Landline Number Random Replace Data Masking Algorithm

Type: LANDLINE_NUMBER_RANDOM_REPLACE

Attributes:

| *Name*           | *DataType* | *Description*                          |
|------------------|------------|----------------------------------------|
| landline-numbers | String     | Landline numbers (separate with comma) |

#### Generic table random replace algorithm.

Type: GENERIC_TABLE_RANDOM_REPLACE

Attributes:

| *Name*                 | *DataType* | *Description*                                                                                                    |
|------------------------|------------|------------------------------------------------------------------------------------------------------------------|
| uppercase-letter-codes | String     | Uppercase letter codes (separate with comma, default value: A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z) |
| lowercase-letter-codes | String     | Lowercase-letter codes (separate with comma, default value: a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z) |
| digital-random-codes   | String     | Numbers (separate with comma, default value: 0,1,2,3,4,5,6,7,8,9)                                                |
| special-codes          | String     | Special codes (separate with comma, default value: ~,!,@,#,$,%,^,&,*,:,&lt;,&gt;,&#166;)                         |

#### Unified credit code random replace algorithm

Type: UNIFIED_CREDIT_CODE_RANDOM_REPLACE

Attributes:

| *Name*                        | *DataType* | *Description*                                      |
|-------------------------------|------------|----------------------------------------------------|
| registration-department-codes | String     | Registration department code (separate with comma) |
| category-codes                | String     | Category code (separate with comma)                |
| administrative-division-codes | String     | Administrative division code (separate with comma) |

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
