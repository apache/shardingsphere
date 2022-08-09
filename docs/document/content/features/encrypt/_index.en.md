+++
pre = "<b>3.8. </b>"
title = "Encryption"
weight = 8
chapter = true
+++

## Definition

Data encryption refers to the modification of some sensitive information through encryption rules in order to offer reliable protection to sensitive private data. Data related to customer security or some sensitive commercial data, such as ID number, mobile phone number, card number, customer number, and other personal information, shall be encrypted according to the regulations of respective regulations.

## Impact on the system

In real business scenarios, service development teams need to implement and maintain a set of encryption and decryption systems based on the requirements of the security department. When the encryption scenario changes, the self-maintained encryption system often faces the risk of reconstruction or modification. Additionally, for services that have been launched, it is relatively complicated to achieve seamless encrypted transformation in a transparent and secure manner without modifying business logic and SQL.

## Related References

- [Configuration: Data Encryption](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
