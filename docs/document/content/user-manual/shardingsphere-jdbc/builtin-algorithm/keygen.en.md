+++
title = "Key Generate Algorithm"
weight = 3
+++

## Snowflake

Type: SNOWFLAKE

Attributes:

| *Name*                                        | *DataType* | *Description*                                                                | *Default Value* |
| --------------------------------------------- | ---------- | ---------------------------------------------------------------------------- | --------------- |
| max-tolerate-time-difference-milliseconds (?) | long       | The max tolerate time for different server's time difference in milliseconds | 10 milliseconds |
| max-vibration-offset (?)                      | int        | The max upper limit value of vibrate number, range `[0, 4096)`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1`| 1 |

## UUID

Type: UUID

Attributes: None

## CosId

Type: COSID

Attributes：

| *Name*    | *DataType* | *Description*                                                                                                                                                                      | *Default Value* |
|-----------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| id-name   | String     | ID generator name                                                                                                                                                                  | `__share__`     |
| as-string | bool       | Whether to generate a string type ID: Convert `long` type ID to Base-62 `String` type (`Long.MAX_VALUE` maximum string length is 11 digits), and ensure the ordering of string IDs | `false`         |

## CosId-Snowflake

Type: COSID_SNOWFLAKE

Attributes：

| *Name*    | *DataType* | *Description*                                                                                                                                                                      | *Default Value* |
|-----------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| epoch     | String     | EPOCH of Snowflake ID Algorithm                                                                                                                                                    | `1477929600000` |
| as-string | bool       | Whether to generate a string type ID: Convert `long` type ID to Base-62 `String` type (`Long.MAX_VALUE` maximum string length is 11 digits), and ensure the ordering of string IDs | `false`         |

