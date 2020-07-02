+++
title = "Encryption"
weight = 3
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd)

\<encrypt:rule />

| *Name*    | *Type*    | *Description*               |
| --------- | --------- | --------------------------- |
| id        | Attribute | Spring Bean Id              |
| table (+) | Tag       | Encrypt table configuration |

\<encrypt:table />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| name       | Attribute | Encrypt table name           |
| column (+) | Tag       | Encrypt column configuration |

\<encrypt:column />

| *Name*                    | *Type*     | *Description*              |
| ------------------------- | ---------- | -------------------------- |
| logic-column              | Attribute  | Column logic name          |
| cipher-column             | Attribute  | Cipher column name         |
| assisted-query-column (?) | Attribute  | Assisted query column name |
| plain-column (?)          | Attribute  | Plain column name          |
| encrypt-algorithm-ref     | Attribute  | Encrypt algorithm name     |

\<encrypt:encrypt-algorithm />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| id        | Attribute  | Encrypt algorithm name       |
| type      | Attribute  | Encrypt algorithm type       |
| props (?) | Tag        | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.
