+++
title = "Key Generate Algorithm"
weight = 2
+++

## Snowflake

Type: SNOWFLAKE

Attributes:

| *Name*                                        | *DataType* | *Description*                                                                | *Default Value* |
| --------------------------------------------- | ---------- | ---------------------------------------------------------------------------- | --------------- |
| worker-id (?)                                 | long       | The unique ID for working machine                                            | 0               |
| max-tolerate-time-difference-milliseconds (?) | long       | The max tolerate time for different server's time difference in milliseconds | 10 milliseconds |
| max-vibration-offset (?)                      | int        | The max upper limit value of vibrate number, range `[0, 4096)`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1`| 1 |

## UUID

Type: UUID

Attributes: None
