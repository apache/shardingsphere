# TDE (Transparent Data Encryption) 算法使用指南

## 概述

TDE（透明数据加密）算法是基于SM4的格式保留加密（FPE）实现，能够在保持原始数据格式的同时提供强加密保护。该算法特别适用于需要保持数据格式不变的场景，如手机号、身份证号、姓名等敏感数据的加密。

## 特性

- **格式保留**: 加密后的数据与原始数据保持相同的格式和长度
- **多字符集支持**: 支持数字、英文字母（大小写）、中文字符、特殊字符
- **SM4加密**: 基于国密SM4算法提供强加密保护
- **异常容错**: 在异常情况下返回原数据，保证系统稳定性

## 配置参数

### 必需参数

- `fpe-key-value`: 加密密钥，字符串格式
- `fpe-number-mapper`: 字符集类型配置，支持多种组合

### 字符集类型

| 类型 | 值 | 字符集 | 说明 |
|------|----|----|------|
| 数字 | 1 | 0123456789 | 纯数字字符集 |
| 小写字母 | 2 | abcdefghijklmnopqrstuvwxyz | 英文小写字母 |
| 大写字母 | 3 | ABCDEFGHIJKLMNOPQRSTUVWXYZ | 英文大写字母 |
| 中文字符 | 4 | Unicode中文字符集 | 常用中文字符 |
| 特殊字符 | 5 | ·!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~ | 常用特殊字符 |

## 配置示例

### 1. 手机号加密（纯数字）

```yaml
encryptors:
  phone_encryptor:
    type: SM4_FPE
    props:
      fpe-key-value: 'MySecretKey12345'
      fpe-number-mapper: '1'  # 仅数字字符集
```

### 2. 身份证号加密（数字+字母）

```yaml
encryptors:
  idcard_encryptor:
    type: SM4_FPE
    props:
      fpe-key-value: 'MySecretKey12345'
      fpe-number-mapper: '1,3'  # 数字+大写字母
```

### 3. 中文姓名加密

```yaml
encryptors:
  name_encryptor:
    type: SM4_FPE
    props:
      fpe-key-value: 'MySecretKey12345'
      fpe-number-mapper: '4'  # 中文字符集
```

### 4. 邮箱地址加密（混合字符集）

```yaml
encryptors:
  email_encryptor:
    type: SM4_FPE
    props:
      fpe-key-value: 'MySecretKey12345'
      fpe-number-mapper: '1,2,3,5'  # 数字+字母+特殊字符
```

## 完整配置示例

```yaml
databaseName: encrypt_db

dataSources:
  encrypt_ds:
    url: jdbc:mysql://127.0.0.1:3306/encrypt_db?serverTimezone=UTC&useSSL=false
    username: root
    password:

rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        phone_number:
          cipher:
            name: phone_number_cipher
            encryptorName: tde_phone
        id_card:
          cipher:
            name: id_card_cipher
            encryptorName: tde_idcard
        name:
          cipher:
            name: name_cipher
            encryptorName: tde_name
  encryptors:
    tde_phone:
      type: SM4_FPE
      props:
        fpe-key-value: 'MySecretKey12345'
        fpe-number-mapper: '1'
    tde_idcard:
      type: SM4_FPE
      props:
        fpe-key-value: 'MySecretKey12345'
        fpe-number-mapper: '1,3'
    tde_name:
      type: SM4_FPE
      props:
        fpe-key-value: 'MySecretKey12345'
        fpe-number-mapper: '4'
```

## 使用注意事项

1. **密钥安全**: 请妥善保管加密密钥，建议使用强密钥并定期更换
2. **字符集匹配**: 确保配置的字符集包含待加密数据的所有字符
3. **性能考虑**: FPE算法相比传统加密算法性能略低，适用于对性能要求不极高的场景
4. **数据长度**: 对于很短的数据（如单个字符），会使用简单字符映射而非完整的FPE算法
5. **异常处理**: 算法内置异常容错机制，加密失败时会返回原数据

## 测试验证

项目提供了测试类来验证算法功能：

- `TDEEncryptAlgorithmTest`: 单元测试
- `TDEAlgorithmDemo`: 功能演示

可以运行这些测试来验证算法的正确性和性能。

## 技术支持

如遇到问题，请检查：

1. 依赖包是否正确引入（BouncyCastle等）
2. 配置参数是否正确
3. 字符集配置是否匹配数据类型
4. 密钥长度和格式是否符合要求