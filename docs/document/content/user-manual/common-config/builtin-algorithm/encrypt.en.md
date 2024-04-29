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
          assistedQuery:
            name: assisted_username
            encryptorName: assisted_encryptor
  encryptors:
    name_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    assisted_encryptor:
      type: MD5
      props:
        salt: 123456
```

## Related References
- [Core Feature: Data Encrypt](/en/features/encrypt/)
- [Developer Guide: Data Encrypt](/en/dev-manual/encrypt/)
