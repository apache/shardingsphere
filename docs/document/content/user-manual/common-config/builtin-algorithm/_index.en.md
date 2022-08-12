+++
title = "Builtin Algorithm"
weight = 2
chapter = true
+++

## Introduction

Apache ShardingSphere allows developers to implement algorithms via SPI;
At the same time, Apache ShardingSphere also provides a couple of builtin algorithms for simplify developers.

## Usage

The builtin algorithms are configured by type and props. 
Type is defined by the algorithm in SPI, and props is used to deliver the customized parameters of the algorithm.

No matter which configuration type is used, the configured algorithm is named and passed to the corresponding rule configuration.
This chapter distinguishes and lists all the builtin algorithms of Apache ShardingSphere according to its functions for developers' reference.
