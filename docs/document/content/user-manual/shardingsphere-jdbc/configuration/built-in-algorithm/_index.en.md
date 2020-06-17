+++
title = "Built-in Algorithm"
weight = 5
chapter = true
+++

## Introduction

Apache ShardingSphere allows users to implement algorithms via SPI;
At the same time, Apache ShardingSphere also provides a couple of built-in algorithms for simplify developers.

## Usage

The built-in algorithms are configured by type and props. 
Type is defined by the algorithm in SPI, and props is used to deliver the customized parameters of the algorithm.

No matter which configuration type is used, it is configured through type and props, and name the configured algorithm and pass it to the corresponding rule configuration.
This chapter distinguishes and lists all the built-in algorithms of Apache ShardingSphere according to its functions for developers' reference.
