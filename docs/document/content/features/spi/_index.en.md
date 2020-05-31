+++
pre = "<b>3.9. </b>"
title = "Pluggable Architecture"
weight = 9
chapter = true
+++

## Background

In Apache ShardingSphere, many functionality implementation are uploaded through [SPI (Service Provider Interface)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html),
which is a kind of API for the third party to implement or expand, and can be applied in framework expansion or component replacement.

Apache ShardingSphere uses SPI to expand in order to optimize overall architecture design at most. 
It is to enable premier users, who have implemented interfaces provided by Apache ShardingSphere, to dynamically load self-defined implementation types. 
Therefore, it can satisfy their practical requirements for different scenarios, while keeping architecture integrity and functional stabilization.

This chapter has provided all the Apache ShardingSphere functional modules that are loaded through SPI. 
Users with no special requirements can use them to implement corresponding functions after simple configurations. 
Premier users can develop self-defined implementations, referring to interfaces of existing functional modules. 
Welcome to feed your self-defined implementations to the [open-source community](https://github.com/apache/shardingsphere/pulls), benefiting more users.

## Challenges

Pluggable architecture is very difficult to design for the project architecture. 
It needs to make each module independent and imperceptible to each other, and appendable functions in a way of superposition through a pluggable kernel.
Design a architecture to completely isolate each function, which can maximize the vitality of the open source community.

Apache ShardingSphere begin to focus on pluggable architecture from version 5.x, features can be embedded into project flexibility.
Currently, the features such as data sharding, read-write splitting, multi replica, data encrypt, shadow test, and SQL dialects / database protocols such as MySQL, PostgreSQL, SQLServer, Oracle supported are all weaved by plugins.
There are lots of SPI extensions for Apache ShardingSphere now and increase continuously.

## Goal

**It is the design goal of Apache shardingsphere pluggable architecture to enable developers to customize their own unique systems just like building blocks.**
