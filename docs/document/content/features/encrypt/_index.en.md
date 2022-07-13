+++
pre = "<b>4.9. </b>"
title = "Encryption"
weight = 9
chapter = true
+++

## Definition

Data encryption refers to the modification of some sensitive information through encryption rules in order to offer reliable protection to sensitive private data. Data related to customer security or some sensitive commercial data, such as ID number, mobile phone number, card number, customer number, and other personal information, shall be encrypted according to the regulations of respective regulations.

## Related Concepts

### Logic column

It is used to calculate the encryption and decryption columns and it is the logical identifier of the column in SQL. Logical columns contain ciphertext columns (mandatory), query-helper columns (optional), and plaintext columns (optional).

### Cipher column

Encrypted data columns.

### Query assistant column

It is a helper column used for queries. For some non-idempotent encryption algorithms with higher security levels, irreversible idempotent columns are provided for queries.

### Plain column

The column is used to store plaintext and provide services during the migration of encrypted data. It can be deleted after the data cleansing is complete.

## Impact on the system

In real business scenarios, service development teams need to implement and maintain a set of encryption and decryption systems based on the requirements of the security department. When the encryption scenario changes, the self-maintained encryption system often faces the risk of reconstruction or modification. Additionally, for services that have been launched, it is relatively complicated to achieve seamless encrypted transformation in a transparent and secure manner without modifying business logic and SQL.

## Limitations

- You need to process the original data on stocks in the database by yourself.
- The case-insensitive queries are not supported for encrypted fields.
- Comparison operations are not supported for encrypted fields, such as GREATER THAN, LESS THAN, ORDER BY, BETWEEN, LIKE.
- Calculation operations are not supported for encrypted fields, such as AVG, SUM, and computation expressions.

## How it works

Apache ShardingSphere parses the SQL entered by users and rewrites the SQL according to the encryption rules provided by users, to encrypt the source data and store the source data (optional) and ciphertext data in the underlying database.
When a user queries data, it only retrieves ciphertext data from the database, decrypts it, and finally returns the decrypted source data to the user. Apache ShardingSphere achieves a transparent and automatic data encryption process. Users can use encrypted data as normal data without paying attention to the implementation details of data encryption. 

### Overall architecture

![1](https://shardingsphere.apache.org/document/current/img/encrypt/1_en.png)

The encrypted module intercepts the SQL initiated by the user and parses and understands the SQL behavior through the SQL grammar parser. Then it finds out the fields to be encrypted and the encryption and decryption algorithm according to the encryption rules introduced by the user and interacts with the underlying database.
Apache ShardingSphere will encrypt the plaintext requested by users and store it in the underlying database. When the user queries, the ciphertext is extracted from the database, decrypted, and returned to the terminal user. By shielding the data encryption process, users do not need to operate the SQL parsing process, data encryption, and data decryption.

### Encryption rules

Before explaining the whole process, we need to understand the encryption rules and configuration. Encryption configuration is mainly divided into four parts: data source configuration, encryptor configuration, encryption table configuration and query attribute configuration, as shown in the figure below:

![2](https://shardingsphere.apache.org/document/current/img/encrypt/2_en.png)

**Data source configuration**: literally the configuration of the data source.

**Encryptor configuration**: refers to the encryption algorithm used for encryption and decryption. Currently, ShardingSphere has three built-in encryption and decryption algorithms: AES, MD5 and RC4. Users can also implement a set of encryption and decryption algorithms by implementing the interfaces provided by ShardingSphere.

**Encryption table configuration**: it is used to tell ShardingSphere which column in the data table is used to store ciphertext data (cipherColumn), which column is used to store plaintext data (plainColumn), and which column the user would like to use for SQL writing (logicColumn).

> What does it mean by "which column the user would like to use for SQL writing (logicColumn)"?
> 
> We have to know first why the encrypted module exists. The goal of the encrypted module is to shield the underlying data encryption process, which means we don't want users to know how data is encrypted and decrypted, and how to store plaintext data into plainColumn and ciphertext data into cipherColumn. In other words, we don't want users to know there is a plainColumn and cipherColumn or how they are used. Therefore, we need to provide the user with a conceptual column that can be separated from the real column in the underlying database. It may or may not be a real column in the database table so that users can change the column names of plainColumn and cipherColumn of the underlying database at will. Or we can delete plainColumn and never store plaintext, only ciphertext. The only thing we have to ensure is that the user's SQL is written towards the logical column, and the correct mapping relation between logicColumn, plainColumn and cipherColumn can be seen in the encryption rules.

**Query attribute configuration**: if both plaintext and ciphertext data are stored in the underlying database table, this attribute can be used to determine whether to query the plaintext data in the database table and return it directly, or query the ciphertext data and return it after decryption through Apache ShardingSphere. This attribute can be configured at the table level and the entire rule level. The table-level has the highest priority.

### Encryption process

For example, if there is a table named `t_user` in the database, and there actually are two fields in the table: `pwd_plain` for storing plaintext data and `pwd_cipher` for storing ciphertext data, and logicColumn is defined as `pwd`, then users should write SQL for logicColumn, that is `INSERT INTO t_user SET pwd = '123'`. Apache ShardingSphere receives the SQL and finds that the `pwd` is the logicColumn based on the encryption configuration provided by the user. Therefore, it encrypts the logical column and its corresponding plaintext data.
**Apache ShardingSphere transforms the column names and data encryption mapping between the logical columns facing users and the plaintext and ciphertext columns facing the underlying database**. As shown in the figure below:

![3](https://shardingsphere.apache.org/document/current/img/encrypt/3_en.png)

The user's SQL is separated from the underlying data table structure according to the encryption rules provided by the user so that the user's SQL writing does not depend on the real database table structure.
The connection, mapping, and transformation between the user and the underlying database are handled by Apache ShardingSphere.
The picture below shows the processing flow and conversion logic when the encryption module is used to add, delete, change and check, as shown in the figure below.

![4](https://shardingsphere.apache.org/document/current/img/encrypt/4_en.png)

## Related References

- [Configuration: Data Encryption](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)
- [Developer Guide: Data Encryption](/en/dev-manual/encrypt/)
