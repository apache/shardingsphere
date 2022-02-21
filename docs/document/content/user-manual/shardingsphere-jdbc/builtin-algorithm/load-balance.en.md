+++
title = "Load Balance Algorithm"
weight = 4
+++

## Round Robin Algorithm

Type: ROUND_ROBIN

Attributes: None

## Random Algorithm

Type: RANDOM

Attributes: None

## Weight Algorithm

Type: WEIGHT

Attributes: 

> All read data in use must be configured with weights

| *Name*                 | *DataType* | *Description*                              |
| ---------------------------------- | ---------- | ---------------------------------------------- |
| \- <read-data_source-name> (+) | double     | The attribute name uses the read database name, and the parameter fills in the weight value corresponding to the read database.The minimum value of the weight parameter range>0,the total <=Double.MAX_VALUE. |

