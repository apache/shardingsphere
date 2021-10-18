+++
pre = "<b>3.4. </b>"
title = "Pluggable Architecture"
weight = 4
chapter = true
+++

## Background

In Apache ShardingSphere, many functionality implementations are uploaded through [SPI (Service Provider Interface)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html),
which is a kind of API for the third party to implement or expand, and can be applied in framework expansion or component replacement.

## Challenges

Pluggable architecture is very difficult to design for the project architecture. 
It needs to make each module decouple to independent and imperceptible to each other totally, and enables appendable functions in a way of superposition through a pluggable kernel.
Design an architecture to completely isolate each function, not only can stimulate the enthusiasm of the open source community, but also can guarantee the quality of the project.

Apache ShardingSphere begin to focus on pluggable architecture from version 5.x, features can be embedded into project flexibility.
Currently, the features such as data sharding, replica query, data encrypt, shadow database,
and SQL dialects / database protocols such as MySQL, PostgreSQL, SQLServer, Oracle supported are all weaved by plugins.
Developers can customize their own ShardingSphere just like building lego blocks.
There are lots of SPI extensions for Apache ShardingSphere now and increase continuously.

## Goal

**It is the design goal of Apache shardingsphere pluggable architecture to enable developers to customize their own unique systems just like building blocks.**

![Pluggable Platform](https://shardingsphere.apache.org/document/current/img/pluggable_platform.png)

## Implementation

The pluggable architecture of Apache ShardingSphere are composed by L1 Kernel Layer, L2 Feature Layer and L3 Ecosystem Layer.

### L1 Kernel Layer

An abstraction of basic capabilities of database. 
All components are required and the specific implementation can be replaced by pluggable way.
It includes query optimizer, distributed transaction engine, distributed execution engine, authority engine and scheduling engine.

### L2 Feature Layer

Used to provide enhanced capability. 
All components are optional and can contain zero or multiple components. 
Components isolate each other and multiple components can be used together superimposed.
It includes data sharding, readwrite-splitting, database highly availability, data encryption, shadow database and so on.
The user-defined feature can be fully customized and extended for the top-level interface defined by Apache ShardingSphere without changing kernel codes.

### L3 Ecosystem Layer

Used to integrate into the current database ecosystem.
It includes database protocol, SQL parser and storage adapter. 

