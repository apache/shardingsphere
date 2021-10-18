+++
pre = "<b>3.4. </b>"
title = "DistSQL"
weight = 4
chapter = true
+++

## Background

DistSQL（Distributed SQL）is Apache ShardingSphere specific SQL, which provide added-on operation capability beside standard SQL.

## Challenges

When using ShardingSphere-Proxy, developers can operate data just like using database, but they need to configure resources and rules through YML file (or registry center).
However, the format of YAML and habits changed by using registry center are not friendly to the operators.

DistSQL enables users to operate Apache ShardingSphere like a database, transforming it from a framework and middleware for developers to an infrastructure product for operators.

DistSQL is divided into RDL, RQL and RAL.

 - RDL (Resource & Rule Definition Language) responsible for the definition of resources and rules;
 - RQL (Resource & Rule Query Language) responsible for the query of resources and rules;
 - RAL (Resource & Rule Administration Language) responsible for the added-on feature of hint, transaction type switch, sharding execute planning and so on.

## Goal

**It is the design goal of DistSQL to break the boundary between middleware and database and let developers use Apache ShardingSphere just like database.**

## Notice

DistSQL can use for ShardingSphere-Proxy only, not for ShardingSphere-JDBC now.
