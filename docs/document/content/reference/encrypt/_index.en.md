+++
pre = "<b>7.7. </b>"
title = "Encryption"
weight = 7
+++

Apache ShardingSphere parses the SQL entered by users and rewrites the SQL according to the encryption rules provided by users, to encrypt the source data and store the source data (optional) and ciphertext data in the underlying database. 

When a user queries data, it only retrieves ciphertext data from the database, decrypts it, and finally returns the decrypted source data to the user. Apache ShardingSphere achieves a transparent and automatic data encryption process. Users can use encrypted data as normal data without paying attention to the implementation details of data encryption. 

### Overall Architecture

![1](https://shardingsphere.apache.org/document/current/img/encrypt/1_en.png)

The encrypted module intercepts the SQL initiated by the user and parses and understands the SQL behavior through the SQL syntactic parser. Then it finds out the fields to be encrypted and the encryption and decryption algorithm according to the encryption rules introduced by the user and interacts with the underlying database. 

Apache ShardingSphere will encrypt the plaintext requested by users and store it in the underlying database. When the user queries, the ciphertext is extracted from the database, decrypted, and returned to the terminal user. By shielding the data encryption process, users do not need to operate the SQL parsing process, data encryption, and data decryption.

### Encryption Rules

Before explaining the whole process, we need to understand the encryption rules and configuration. Encryption configuration is mainly divided into four parts: data source configuration, encryptor configuration, encryption table configuration, and query attribute configuration, as shown in the figure below:

![2](https://shardingsphere.apache.org/document/current/img/encrypt/2_en.png)

Data source configuration: the configuration of the data source.

Encryptor configuration: refers to the encryption algorithm used for encryption and decryption. Currently, ShardingSphere has three built-in encryption and decryption algorithms: AES, MD5, and RC4. Users can also implement a set of encryption and decryption algorithms by implementing the interfaces provided by ShardingSphere.

Encryption table configuration: it is used to tell ShardingSphere which column in the data table is used to store ciphertext data (`cipherColumn`), which column is used to store plaintext data (`plainColumn`), and which column the user would like to use for SQL writing (`logicColumn`).

> What does it mean by "which column the user would like to use for SQL writing (logicColumn)"?
We have to know first why the encrypted module exists. The goal of the encrypted module is to shield the underlying data encryption process, which means we don't want users to know how data is encrypted and decrypted, and how to store plaintext data into `plainColumn` and ciphertext data into `cipherColumn`. In other words, we don't want users to know there is a `plainColumn` and `cipherColumn` or how they are used. Therefore, we need to provide the user with a conceptual column that can be separated from the real column in the underlying database. It may or may not be a real column in the database table so that users can change the column names of `plainColumn` and `cipherColumn` of the underlying database at will. Or we can delete `plainColumn` and never store plaintext, only ciphertext. The only thing we have to ensure is that the user's SQL is written towards the logical column, and the correct mapping relation between `logicColumn`, `plainColumn`, and `cipherColumn` can be seen in the encryption rules.
>

Query attribute configuration: if both plaintext and ciphertext data are stored in the underlying database table, this attribute can be used to determine whether to query the plaintext data in the database table and return it directly, or query the ciphertext data and return it after decryption through Apache ShardingSphere. This attribute can be configured at the table level and the entire rule level. The table-level has the highest priority.

### Encryption Process

For example, if there is a table named `t_user` in the database, and they're two fields in the table: `pwd_plain` for storing plaintext data and `pwd_cipher` for storing ciphertext data, and logicColumn is defined as `pwd`, then users should write SQL for `logicColumn`, that is `INSERT INTO t_user SET pwd = '123'`. Apache ShardingSphere receives the SQL and finds that the `pwd` is the `logicColumn` based on the encryption configuration provided by the user. Therefore, it encrypts the logical column and its corresponding plaintext data. 

Apache ShardingSphere transforms the column names and data encryption mapping between the logical columns facing users and the plain and cipher columns facing the underlying database. As shown in the figure below:

![3](https://shardingsphere.apache.org/document/current/img/encrypt/3_en.png)

The user's SQL is separated from the underlying data table structure according to the encryption rules provided by the user so that the user's SQL writing does not depend on the real database table structure. 

The connection, mapping, and transformation between the user and the underlying database are handled by Apache ShardingSphere.

The picture below shows the processing flow and conversion logic when the encryption module is used to add, delete, change and check, as shown in the figure below.

![4](https://shardingsphere.apache.org/document/current/img/encrypt/4_en.png)

## Detailed Solution

After understanding Apache ShardingSphere's encryption process, you can combine the encryption configuration and encryption process according to your scenario. The entire design & development was conceived to address the pain points encountered in business scenarios. So, how to use Apache ShardingSphere to meet the business requirements mentioned before?

### New Business

Business scenario analysis: the newly launched business is relatively simple because it starts from scratch and there's no need to clean up historical data.

Solution description: after selecting the appropriate encryption algorithm, such as AES, you only need to configure the logical column (write SQL for users) and the ciphertext column (the data table stores the ciphertext data). The logical columns and ciphertext columns can also be different. The following configurations are recommended (in YAML format): 

```yaml
-!ENCRYPT
 encryptors:
  aes_encryptor:
   type: AES
   props:
    aes-key-value: 123456abc
 tables:
  t_user:
   columns:
    pwd:
     cipherColumn: pwd_cipher
     encryptorName: aes_encryptor
     assistedQueryColumn: pwd_assisted_query
     assistedQueryEncryptorName: pwd_assisted_query_cipher
     queryWithCipherColumn: true
```

With the above configuration, Apache ShardingSphere only needs to convert `logicColumn`, `cipherColumn`, and `assistedQueryColumn`. 

The underlying data table does not store plaintext, and only ciphertext is stored, which is also the requirement of the security audit. If you want to store both plaintext and ciphertext in the database, add the `plainColumn` configuration. The overall processing flow is shown in the figure below:

![5](https://shardingsphere.apache.org/document/current/img/encrypt/5_en.png)

### Online Business Transformation

Business scenario analysis: as the business is already running, the database will already have stored a large amount of plaintext historical data. The current challenges are how to encrypt and clean up the historical data, how to encrypt and process the incremental data, and how to seamlessly and transparently migrate business between the old and new data systems.

Solution Description: before coming up with a solution, let's brainstorm. 

First, since it is an old business that needs to be encrypted and transformed, it must have stored very important and sensitive information, which is valuable and related to critical businesses. Therefore, it is impossible to suspend business immediately, prohibit writing new data, encrypt and clean all historical data with an encryption algorithm. And then deploy and launch the reconstructed code to encrypt and decrypt the stock and incremental data online. Such a complex solution will definitely not work.

Another relatively safe solution is to build a set of pre-released environments exactly the same as the production environment, and then encrypt the stock original data of the production environment and store it in the pre-released environment through migration and data cleansing tools. 

The new data is encrypted and stored in the database of the pre-released environment through tools such as MySQL primary/secondary replication and self-developed ones by the business side. The reconfigurable code that can be encrypted and decrypted is deployed to the pre-released environment. This way, the production environment takes plaintext as the core used for queries and modifications. 

The pre-released environment is a ciphertext-based environment for encrypted and decrypted queries and modifications. After comparison, the production flow can be transferred to the pre-released environment by nighttime operation. This method is relatively safe and reliable, but time consuming,labor and capital intensive, mainly including building a pre-released environment, modifying production code, developing auxiliary tools, etc.

The most popular solutions for developers are to reduce the capital cost, not change the business code, and be able to migrate the system safely and smoothly. Thus, the encryption function module of ShardingSphere was created. It can be divided into three steps:

1. Before system migration

Assuming that the system needs to encrypt the `pwd` field of `t_user`, the business side uses Apache ShardingSphere to replace the standardized JDBC interface, which basically requires no additional modification (we also provide Spring Boot Starter, Spring Namespace, YAML and other access methods to meet different business requirements). In addition, we would like to demonstrate a set of encryption configuration rules, as follows:

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          plainColumn: pwd
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: pwd_assisted_query
          assistedQueryEncryptorName: pwd_assisted_query_cipher
          queryWithCipherColumn: false
```

According to the above encryption rules, we need to add a field called `pwd_cipher`, namely `cipherColumn`, in the `t_user` table, which is used to store ciphertext data.

At the same time, we set `plainColumn` to `pwd`, which is used to store plaintext data, and `logicColumn` is also set to `pwd`. 

Because the previous SQL was written using `pwd`, the SQL was written for logical columns, and the business code does not need to be changed. Through Apache ShardingSphere, for the incremental data, the plaintext will be written to the `pwd` column and be encrypted and stored in the `pwd_cipher` column.

At this time, because `queryWithCipherColumn` is set to `false`, for business applications, the plaintext column of `pwd` is still used for query and storage, but the ciphertext data of the new data is additionally stored on the underlying database table `pwd_cipher`. The processing flow is shown below:

![6](https://shardingsphere.apache.org/document/current/img/encrypt/6_en.png)

When the new data is inserted, it is encrypted as ciphertext data by Apache ShardingSphere and stored in the `cipherColumn`. Now you need to deal with the historical plaintext stock data. Apache ShardingSphere currently does not provide a migration and data cleansing tool, so you need to encrypt the plaintext data in the `pwd` and store it in the `pwd_cipher`.

2. During system migration

The new ciphertext data is stored in the `cipherColumn` and the new plaintext one is stored in the `plainColumn` by Apache ShardingSphere. After the historical data is encrypted and cleaned by the business side, its ciphertext is also stored in the `cipherColumn`. In other words, the current database stores both plaintext and ciphertext. 

Owing to the configuration item `queryWithCipherColumn = false`, the ciphertext is never used. Now we need to set `queryWithCipherColumn` in the encryption configuration to true in order for the system to query ciphertext data. 

After restarting the system, we found that all system businesses are normal, but Apache ShardingSphere has started to take out and decrypt the cipherColumn data from the database and returned those data to the user. In terms of users' requirements of addition, deletion and modification, the original data is still stored in the `plainColumn`, and the encrypted ciphertext data is stored in the `cipherColumn`.

Although the business system has taken out the data in the `cipherColumn` and returned it after decryption, it will still save a copy of the original data to the `plainColumn`. Why? The answer is: to enable system rollback. 

Because as long as the ciphertext and plaintext always exist at the same time, we can freely switch the business query to `cipherColumn` or `plainColumn` through the configuration of the switch item. 

In other words, if the system is switched to the ciphertext column for query, the system reports an error and needs to be rolled back. Then we only need to set `queryWithCipherColumn = false`, and Apache ShardingSphere will restore and start using `plainColumn` to query again. The processing flow is shown in the following figure:

![7](https://shardingsphere.apache.org/document/current/img/encrypt/7_en.png)

3. After system migration

As required by security audit teams, it is generally impossible for the business system to permanently synchronize the plaintext column and ciphertext column of the database, so we need to delete the plaintext column data after the system is stable.

That is, we need to delete plainColumn (i.e.`pwd`) after system migration. The problem is that now the business code is written for `pwd` SQL, and we delete the pwd that stores plaintext in the underlying data table and use the `pwd_cipher` to decrypt the original data. 

Does that mean that the business side needs to change all SQL, to not use the pwd column to be deleted? No. Remember the core concept of Apache ShardingSphere?

> That is exactly the core concept of Apache ShardingSphere's encryption module. According to the encryption rules provided by the user, the user SQL is separated from the underlying database table structure, so that the user’s SQL writing no longer depends on the actual database table structure. The connection, mapping, and conversion between the user and the underlying database are handled by ShardingSphere.

The existence of the `logicColumn` means that users write SQL for this virtual column. Apache ShardingSphere can map this logical column and the ciphertext column in the underlying data table. So the encryption configuration after the migration is:

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd: # pwd and pwd_cipher transformation mapping
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
          assistedQueryColumn: pwd_assisted_query
          assistedQueryEncryptorName: pwd_assisted_query_cipher
          queryWithCipherColumn: true
```

The processing flow is as follows:

![8](https://shardingsphere.apache.org/document/current/img/encrypt/8_en.png)

4. System migration completed

As required by security audit teams, the business system needs to periodically trigger key modifications or through some emergency events. We need to perform migration data cleansing again, which means using the old key to decrypt and then use the new key to encrypt. 

The problem persists. The plaintext column data has been deleted, and the amount of data in the database table is tens of millions. Additionally, the migration and cleansing take a certain amount of time, during which the cipher column changes. 

Under these circumstances, the system still needs to provide services correctly. What can we do? The answer lies in the auxiliary query column. Because auxiliary query columns generally use algorithms such as irreversible MD5 and SM3. Queries based on auxiliary columns are performed correctly by the system even during the migration and data cleansing process.

So far, the encryption rectification solution for the released business has been completely demonstrated. We provide Java, YAML, Spring Boot Starter, and Spring namespace for users to choose and access to meet different business requirements. This solution has been continuously verified by enterprise users such as JD Technology.

## The advantages of Middleware encryption service

1. Automatic and transparent data encryption process. Encryption implementation details are no longer a concern for users. 
2. It provides a variety of built-in and third-party (AKS) encryption algorithms, which are available through simple configurations.
3. It provides an encryption algorithm API interface. Users can implement the interface to use a custom encryption algorithm for data encryption.
4. It can switch among different encryption algorithms.
5. For businesses already launched, it is possible to store plaintext data and ciphertext data synchronously. And you can decide whether to use plaintext or ciphertext columns for query through configuration. Without changing the business query SQL, the released system can safely and transparently migrate data before and after encryption.

## Solution

Apache ShardingSphere provides an encryption algorithm for data encryption, namely `EncryptAlgorithm`.

On the one hand, Apache ShardingSphere provides users with built-in implementation classes for encryption and decryption, which are available through configurations by users. 

On the other hand, in order to be applicable to different scenarios, we also opened the encryption and decryption interfaces, and users can provide specific implementation classes according to these two types of interfaces. 

After simple configuration, Apache ShardingSphere can call user-defined encryption and decryption schemes for data encryption.

### EncryptAlgorithm

The solution provides two methods, `encrypt()` and `decrypt()`, to encrypt or decrypt data.
When users perform `INSERT`, `DELETE` and `UPDATE` operations, ShardingSphere will parse, rewrite and route SQL according to the configuration. 

It will also use `encrypt()` to encrypt data and store them in the database. When using SELECT, they will decrypt sensitive data from the database with `decrypt()` and finally return the original data to users.

Currently, Apache ShardingSphere provides five types of implementations for this kind of encryption solution, including MD5 (irreversible), AES (reversible), RC4 (reversible), SM3 (irreversible) and SM4 (reversible), which can be used after configuration.
