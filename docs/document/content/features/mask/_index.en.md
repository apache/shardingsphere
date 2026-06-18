+++
pre = "<b>3.8. </b>"
title = "Data Masking"
weight = 8
chapter = true
+++

## Background

With the introduction of laws on user data protection, the protection of personal privacy data has risen to the legal level. Traditional application systems generally lack protection measures for personal privacy data.
Data masking can achieve special encryption, masking and replacement of the data returned by the production database according to user-defined masking rules without any changes to the data in the production database to ensure the sensitivity of the production environment data can be protected.

## Challenges

In real business scenarios, relevant DevOps teams often need to implement and maintain a set of masking functions by themselves according to data masking requirements, and the masking functions are often coupled in various business logics. Additionally different business systems are difficult to reuse. When the masking scenario changes, the masking function maintained by itself often faces the risk of refactoring or modification.

## Goal

According to industry needs for data masking and the pain points of business transformation, it provides a complete, safe, transparent, and low transformation cost data masking integration solution, which is the main design goal of the Apache ShardingSphere data masking module.

## Application Scenarios

Whether it is a new business that is launched quickly or a mature business that has already been launched, you can access the data masking function of ShardingSphere to quickly complete the configuration of mask rules. 
Customers can use data masking function transparently without developing a masking function coupled to the business system, and without changing any business logic and SQL.

## Related References

- [Configuration: Data Mask](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/mask/)
- [Developer Guide: Data Mask](/en/dev-manual/mask/)
