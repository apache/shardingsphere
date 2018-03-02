+++
icon = "<b>0. </b>"
date = "2017-04-12T16:06:17+08:00"
title = "Overview"
weight = 0
prev = "/03-design/roadmap/"
next = "/00-overview/intro/"
chapter = true

+++

# Overview

[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg?style=social&label=Release)](https://github.com/shardingjdbc/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/shardingjdbc/sharding-jdbc.svg?style=social&label=Star)](https://github.com/shardingjdbc/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/shardingjdbc/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/shardingjdbc/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/shardingjdbc/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/shardingjdbc/sharding-jdbc/watchers)

Sharding-JDBC is an open source and micro-service-oriented distributed database base access library, which is always targeted at the cloud-native base development suite.

Sharding-JDBC is a lightweight java framework, using the java-jdbc-client to connect database, providing services all-in-jar, no middle layer is used, no other dependence, DBA also don't need to change the original dev mode, can be understood as a enhanced version of the JDBC driver, migrate legacy code almost zero costs.

Sharding-JDBC fully implements sharding databases and tables, read-write splitting, distributed primary key, and B.A.S.E transaction. Since 2016, it has accumulated enough inside information after several refinements and stability polishing of the overall architecture, we believe that it can be a reference for developers to choose technology components.

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.svg?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/shardingjdbc/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# Feature List

## 1. Sharding Databases And Tables
* Perfect SQL parsing，which supports aggregation, grouping, sorting, LIMIT, TOP and other queries, and supports cascading tables and Cartesian product table queries
* Supports Inner Join and Outer Join
* Flexible sharding strategy, which support =, BETWEEN, IN, multiple sharding-columns and customized sharding strategy
* Supports Hint-based sharding

## 2. Read-write splitting
* Independent use of read and write separation support SQL transmission
* Support one master and multiple slaves for the read and write splitting
* Hint-based forced master routing

## 3. B.A.S.E Transaction
* Best Effort Delivery
* Try Confirm Cancel(TBD)

## 4. Distributed Primary Key
* Built-in distributed primary key generator
* Customized distributed primary key generator
* Compatibility with JDBC

## 5. Compatibility
* Suitable for any java ORM frameworks, such as: JPA, Hibernate, Mybatis or JDBC directly
* Suitable for any database connection pool, such as: DBCP, C3P0, BoneCP, etc
* In theory, any database that implements the JDBC specification can be supported.Support MySQL, Oracle, SQLServer and PostgreSQL

## 6. Flexible And Diverse Configurations
* Java
* YAML
* Inline Expression
* Spring Namespace
* Spring boot starter

## 7. Distributed Governance Capability(2.0 New Feature)

* Configuration is centralized and dynamic，support dynamically switching of datasources, tables and sharding policies(2.0.0.M1)
* Client database governance, datasource automatic switching when failure(2.0.0.M2)
* Information Output based on Open Tracing protocol(2.0.0.M3)

# Communication And Participation

 - **Please Join Our QQ group(it’s full now, please join QQ group2)：** 532576663(Only discuss topics related to Sharding-JDBC. We want you to read the document carefully before entering the group. Read the bulletin and modify group business CARDS after entering the group. Thank you for your cooperation)
 - **QQ group2：** 459894627
 - **Source Code Communicate QQ group：** 659205143(Only discuss topics related to Sharding-JDBC source implementation. We welcome you here to communicate with us about Sharding-JDBC architecture design, code implementation, and future line planning. This group needs to have an early understanding of Sharding-JDBC. Membership: please post an article on the source code analysis for Sharding-JDBC and send it to us through official communication.)
 - Report identified bugs, submit enhancements and submit patches, etc.，please read [how do you contribute](/00-overview/contribution).
 
 **If you use sharding-JDBC, please leave the company and website at your convenience.** https://github.com/shardingjdbc/sharding-jdbc/issues/234
