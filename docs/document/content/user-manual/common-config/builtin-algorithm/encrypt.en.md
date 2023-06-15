+++
title = "Encryption Algorithm"
weight = 5
+++

## Background

Encryption algorithms are by the encryption features of Apache ShardingSphere. A variety of algorithms are built-in to make it easy for users to fully leverage the feature.

## Parameters

### Standard Encrypt Algorithm

#### AES Encrypt Algorithm

Type: AES

Attributes:

| *Name*                | *DataType* | *Description*                                       |
|-----------------------|------------|-----------------------------------------------------|
| aes-key-value         | String     | AES KEY                                             |
| digest-algorithm-name | String     | AES KEY DIGEST ALGORITHM (optional, default: SHA-1) |

#### RC4 Encrypt Algorithm

Type: RC4

Attributes:

| *Name*        | *DataType* | *Description* |
|---------------|------------|---------------|
| rc4-key-value | String     | RC4 KEY       |

#### SM3 Encrypt Algorithm

Type: SM3

Attributes:

| *Name*   | *DataType* | *Description*                              |
|----------|------------|--------------------------------------------|
| sm3-salt | String     | SM3 SALT (should be blank or 8 bytes long) |

#### SM4 Encrypt Algorithm

Type: SM4

Attributes:

| *Name*      | *DataType* | *Description*                                                            |
|-------------|------------|--------------------------------------------------------------------------|
| sm4-key     | String     | SM4 KEY (should be 16 bytes)                                             |
| sm4-mode    | String     | SM4 MODE (should be CBC or ECB)                                          |
| sm4-iv      | String     | SM4 IV (should be specified on CBC, 16 bytes long)                       |
| sm4-padding | String     | SM4 PADDING (should be PKCS5Padding or PKCS7Padding, NoPadding excepted) |

### Like Encrypt Algorithm

#### CharDigestLike Encrypt Algorithm

Type：CHAR_DIGEST_LIKE

Attributes：

| *Name* | *DataType* | *Description*                                   |
|--------|------------|-------------------------------------------------|
| delta  | int        | Character Unicode offset（decimal number）        |
| mask   | int        | Character encryption mask（decimal number）       |
| start  | int        | Ciphertext Unicode initial code（decimal number） |
| dict   | String     | Common words                                    |

### Assisted Encrypt Algorithm

#### MD5 Assisted Encrypt Algorithm

Type: MD5

Attributes:

| *Name* | *DataType* | *Description*        |
|--------|------------|----------------------|
| salt   | String     | Salt value(optional) |

## Operating Procedure

1. Configure encryptors in an encryption rule.
2. Use relevant algorithm types in encryptors.

## Configuration Examples
```yaml
rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          cipher:
            name: username
            encryptorName: name_encryptor
          likeQuery:
            name: name_like
            encryptorName: like_encryptor
  encryptors:
    like_encryptor:
      type: CHAR_DIGEST_LIKE
    name_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
```

## Related References
- [Core Feature: Data Encrypt](/en/features/encrypt/)
- [Developer Guide: Data Encrypt](/en/dev-manual/encrypt/)
