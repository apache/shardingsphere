+++
title = "脱敏算法"
weight = 9
+++

## 背景信息

脱敏算法是 Apache ShardingSphere 的脱敏功能使用的算法，ShardingSphere 内置了多种算法，可以让用户方便使用。

## 参数解释

### 哈希脱敏算法

#### MD5 脱敏算法

类型：MD5

可配置属性：

| *名称*   | *数据类型* | *说明*  |
|--------| --------- |-------|
| salt   | String    | 盐值（可选）|

### 遮盖脱敏算法

#### KEEP_FIRST_N_LAST_M 脱敏算法

类型：KEEP_FIRST_N_LAST_M

可配置属性：

| *名称*         | *数据类型* | *说明*  |
|--------------|--------|-------|
| first-n      | int    | 前 n 位 |
| last-m       | int    | 后 n 位 |
| replace-char | String | 替换字符  |

#### KEEP_FROM_X_TO_Y 脱敏算法

类型：KEEP_FROM_X_TO_Y

可配置属性：

| *名称*         | *数据类型* | *说明*           |
|--------------|--------|----------------|
| from-x       | int    | 起始位置（从 0 开始）   |
| to-y         | int    | 结束位置（从 0 开始）   |
| replace-char | String | 替换字符           |

#### MASK_FIRST_N_LAST_M 脱敏算法

类型：MASK_FIRST_N_LAST_M

可配置属性：

| *名称*         | *数据类型* | *说明*           |
|--------------|--------|----------------|
| first-n      | int    | 前 n 位 |
| last-m       | int    | 后 n 位 |
| replace-char | String | 替换字符  |

#### MASK_FROM_X_TO_Y 脱敏算法

类型：MASK_FROM_X_TO_Y

可配置属性：

| *名称*         | *数据类型* | *说明*           |
|--------------|--------|----------------|
| from-x       | int    | 起始位置（从 0 开始）   |
| to-y         | int    | 结束位置（从 0 开始）   |
| replace-char | String | 替换字符           |

#### MASK_BEFORE_SPECIAL_CHARS 脱敏算法

类型：MASK_BEFORE_SPECIAL_CHARS

可配置属性：

| *名称*         | *数据类型* | *说明*         |
|--------------|--------|--------------|
| special-chars       | String | 特殊字符（首次出现）   |
| replace-char | String | 替换字符         |

#### MASK_AFTER_SPECIAL_CHARS 脱敏算法

类型：MASK_AFTER_SPECIAL_CHARS

可配置属性：

| *名称*         | *数据类型* | *说明*         |
|--------------|--------|--------------|
| special-chars       | String | 特殊字符（首次出现）   |
| replace-char | String | 替换字符         |

### 替换脱敏算法

#### PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE 脱敏算法

类型：PERSONAL_IDENTITY_NUMBER_RANDOM_REPLACE

可配置属性：

| *名称*                        | *数据类型* | *说明*                 |
|------------------------------|----------|----------------------|
| alpha-two-country-area-code  | String   | 两位字母国家/地区编码（可选，默认：CN）|

#### TELEPHONE_RANDOM_REPLACE 脱敏算法

类型：TELEPHONE_RANDOM_REPLACE

可配置属性：

| *名称*            | *数据类型* | *说明*        |
|-----------------|----------|-------------|
| network-numbers | String   | ⽹号（以英文逗号分隔） |

## 操作步骤
1. 在脱敏规则中配置脱敏算法；
2. 为脱敏算法指定脱敏算法类型。

## 配置示例
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

## 相关参考
- [核心特性：数据脱敏](/cn/features/mask/)
- [开发者指南：数据脱敏](/cn/dev-manual/mask/)
