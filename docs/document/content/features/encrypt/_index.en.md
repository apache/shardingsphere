+++
pre = "<b>3.7. </b>"
title = "Encryption"
weight = 7
chapter = true
+++

## Background

Security control has always been a crucial link of data governance, data encryption falls into this category. For both Internet enterprises and traditional sectors, data security has always been a highly valued and sensitive topic. Data encryption refers to transforming some sensitive information through encrypt rules to safely protect the private data. Data involves client’s security or business sensibility, such as ID number, phone number, card number, client number and other personal information, requires data encryption according to relevant regulations.

For data encryption requirements, there are the following situations in realistic business scenarios:

* When the new business start to launch, and the security department stipulates that the sensitive information related to users, such as banks and mobile phone numbers, should be encrypted and stored in the database, and then decrypted when used.

## Challenges

In the real business scenario, the relevant business development team often needs to implement and maintain a set of encryption and decryption system according to the needs of the company’s security department. When the encryption scenario changes, the encryption system often faces the risk of reconstruction or modification. In addition, for the online business system, it is relatively complex to realize seamless encryption transformation with transparency, security and low risk without modifying the business logic and SQL.

## Goal

Provides a security and transparent data encryption solution, which is the main design goal of Apache ShardingSphere data encryption module.

## Application Scenarios

For scenarios requiring the quick launch of new services while respecting encryption regulations. The ShardingSphere encryption feature can be used to quickly achieve compliant data encryption, without requiring users to develop complex encryption systems. 

At the same time, its flexibility can also help users avoid complex rebuilding and modification risks caused by encryption scenario changes.

## Related References

- [Configuration: Data Encryption](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
