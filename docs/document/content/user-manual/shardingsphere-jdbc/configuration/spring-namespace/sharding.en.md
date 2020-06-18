+++
title = "Sharding"
weight = 1
+++

## Configuration Item Explanation

TODO

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding) and [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen) for more details about type of algorithm.

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.
