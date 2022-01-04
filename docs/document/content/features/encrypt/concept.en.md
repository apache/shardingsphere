+++
title = "Core Concept"
weight = 1
+++

## Logic Column

Column name used to encryption, it is the logical column identification in SQL.
It includes cipher column(required), query assistant column(optional) and plain column(optional).

## Cipher Column

Encrypted data column.

## Query Assistant Column

Column used to assistant for query.
For non-idempotent encryption algorithms with higher security level, irreversible idempotent columns provided for query.

## Plain Column

Column used to persist plain column, for service provided during data encrypting.
Should remove them after data clean.
