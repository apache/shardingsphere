+++
title = "Core Concept"
weight = 1
+++

## Logic column

It is used to calculate the encryption and decryption columns and it is the logical identifier of the column in SQL. Logical columns contain ciphertext columns (mandatory), query-helper columns (optional), like-query columns (optional), and plaintext columns (optional).

## Cipher column

Encrypted data columns.

## Assisted query column

It is a helper column used for queries. For some non-idempotent encryption algorithms with higher security levels, irreversible idempotent columns are provided for queries.

## Like query column

It is a helper column used for like queries. 

## Plain column

The column is used to store plaintext and provide services during the migration of encrypted data. It can be deleted after the data cleansing is complete.
