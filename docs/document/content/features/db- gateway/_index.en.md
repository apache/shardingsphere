+++
pre = "<b>4.2. </b>"
title = "DB Gateway"
weight = 2
chapter = true
+++

## Background

With the trend of database fragmentation, using multiple types of databases together has become the norm. 
The scenario of using one SQL dialect to access all heterogeneous databases is increasing.

## Challenges

The existence of diversified databases makes it difficult to standardize the SQL dialect accessing the database.
Engineers need to use different dialects for different kinds of databases, and there is no unified query platform.

Automatically translate different types of database dialects into the dialects used by the database, 
so that engineers can use any database dialect to access all heterogeneous databases, which can reduce development and maintenance cost greatly.

## Goal

**The goal of database gateway for Apache ShardingSphere is translating SQL automatically among various databases.**

## Current State

SQL translation in Apache ShardingSphere is in the **experimental stage** currently.

It has supported auto translation with MySQL/PostgreSQL, engineers can use the SQL and protocol of MySQL to access PostgreSQL, vice versa.

**Source Codes: https://github.com/apache/shardingsphere/tree/master/shardingsphere-kernel/shardingsphere-sql-translator**
