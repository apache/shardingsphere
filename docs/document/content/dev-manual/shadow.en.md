+++
pre = "<b>6.13. </b>"
title = "Shadow DB"
weight = 13
chapter = true
+++

## SPI Interface

| *SPI Name*       | *Description*   |
|---------------- |------------ |
| ShadowAlgorithm | shadow routing algorithm |

## Sample

### ShadowAlgorithm

| *Implementation Class* | *Description* |
|-------------------------------- |----------------------- |
| ColumnValueMatchShadowAlgorithm | Match shadow algorithms based on field values     |
| ColumnRegexMatchShadowAlgorithm | Regular matching shadow algorithm based on field value  |
| SimpleHintShadowAlgorithm    | Simple match shadow algorithm based on Hint |
