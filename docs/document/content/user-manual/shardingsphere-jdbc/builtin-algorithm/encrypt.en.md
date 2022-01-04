+++
title = "Encryption Algorithm"
weight = 5
+++

## MD5 Encrypt Algorithm

Type: MD5

Attributes: None

## AES Encrypt Algorithm

Type: AES

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| aes-key-value | String     | AES KEY       |

## RC4 Encrypt Algorithm

Type: RC4

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| rc4-key-value | String     | RC4 KEY       |

## SM3 Encrypt Algorithm

Type: SM3

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| sm3-salt      | String     | SM3 SALT (should be blank or 8 bytes long)      |

## SM4 Encrypt Algorithm

Type: SM4

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| sm4-key       | String     | SM4 KEY (should be 16 bytes) |
| sm4-mode      | String     | SM4 MODE (should be CBC or ECB) |
| sm4-iv        | String     | SM4 IV (should be specified on CBC, 16 bytes long)|
| sm4-padding   | String     | SM4 PADDING (should be PKCS5Padding or PKCS7Padding, NoPadding excepted)|