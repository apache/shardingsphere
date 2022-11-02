+++
pre = "<b>3.8. </b>"
title = "Encryption"
weight = 8
chapter = true
+++

## Background

Security control has always been a crucial link of data governance, data encryption falls into this category. For both Internet enterprises and traditional sectors, data security has always been a highly valued and sensitive topic. Data encryption refers to transforming some sensitive information through encrypt rules to safely protect the private data. Data involves client’s security or business sensibility, such as ID number, phone number, card number, client number and other personal information, requires data encryption according to relevant regulations.

The demand for data encryption is generally divided into two situations in real business scenarios:

1. When the new business start to launch, and the security department stipulates that the sensitive information related to users, such as banks and mobile phone numbers, should be encrypted and stored in the database, and then decrypted when used. Because it is a brand new system, there is no inventory data cleaning problem, so the implementation is relatively simple.

1. For the service has been launched, and plaintext has been stored in the database before. The relevant department suddenly needs to encrypt the data from the on-line business. This scenario generally needs to deal with three issues as followings:

- How to encrypt the historical data, a.k.a.s data clean.
- How to encrypt the newly added data and store it in the database without changing the business SQL and logic; then decrypt the taken out data when use it.
- How to securely, seamlessly and transparently migrate plaintext and ciphertext data between business systems.

## Challenges

In the real business scenario, the relevant business development team often needs to implement and maintain a set of encryption and decryption system according to the needs of the company’s security department. When the encryption scenario changes, the encryption system often faces the risk of reconstruction or modification. In addition, for the online business system, it is relatively complex to realize seamless encryption transformation with transparency, security and low risk without modifying the business logic and SQL.

## Goal

Provides a security and transparent data encryption solution, which is the main design goal of Apache ShardingSphere data encryption module.

## Application Scenarios

### Newly launched services

For scenarios requiring the quick launch of new services while respecting encryption regulations. The ShardingSphere encryption feature can be used to quickly achieve compliant data encryption, without requiring users to develop complex encryption systems. 

At the same time, its flexibility can also help users avoid complex rebuilding and modification risks caused by encryption scenario changes.

### Existing services

For mature services that have already been launched, users need to consider the historical data cleansing and the switchover between old and new features. 

By accessing ShardingSphere encrypt, users can easily complete the encryption transformation of the system, and it can also help users securely and quickly switch between old and new features. Users can transparently use encryption and decryption features without changing any business logic and SQL.

## Related References

- [Configuration: Data Encryption](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
