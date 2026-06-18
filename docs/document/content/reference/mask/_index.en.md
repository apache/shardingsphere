+++
pre = "<b>7.8. </b>"
title = "Mask"
weight = 8
+++

Apache ShardingSphere achieves the desensitization of the original data by parsing the SQL queried by users and masking the SQL execution results according to the desensitization rules provided by users. 

### Overall Architecture

![1](https://shardingsphere.apache.org/document/current/img/mask/1_en.png)

The desensitization module intercepts the SQL initiated by the user, analyzes and executes it through the SQL syntax parser. It then masks the query results by finding out the fields to be desensitized and the desensitization algorithm to be used according to the rules passed specified by the user, and returns to the client.

### Mask Rules

Before explaining the whole process in detail, we need to first understand the desensitization rules and configuration, which is the basis of understanding the whole process. 

Desensitization configuration is mainly divided into three parts: data source configuration, desensitization algorithm configuration, desensitization table configuration:

![2](https://shardingsphere.apache.org/document/current/img/mask/2_en.png)

**Data source configuration**: the configuration of the data source.

**Mask algorithm configuration**: currently, ShardingSphere has a variety of built-in desensitization algorithms: MD5, KEEP_FIRST_N_LAST_M, KEEP_FROM_X_TO_Y , MASK_FIRST_N_LAST_M, MASK_FROM_X_TO_Y, MASK_BEFORE_SPECIAL_CHARS, MASK_AFTER_SPECIAL_CHARS and GENERIC_TABLE_RANDOM_REPLACE. 

Users can also implement a set of desensitization algorithms by implementing the interface provided by ShardingSphere.

**Mask table configuration**: used to tell ShardingSphere which column in the data table is used for data desensitization and which algorithm is used for desensitization.

**The mask rule takes effect after it is created**

Query attribute configuration: if both plaintext and ciphertext data are stored in the underlying database table, this attribute can be used to determine whether to query the plaintext data in the database table and return it directly, or query the ciphertext data and return it after decryption through Apache ShardingSphere. 

This attribute can be configured at the table level and the entire rule level. The table-level has the highest priority.

### Mask Process

For example, if there is a table in the database called `t_user and` there is a field in the table called `phone_number` that uses `MASK_FROM_X_TO_Y`, Apache ShardingSphere does not change the data store. 

It'll only mask the result according to the desensitization algorithm, to achieve the desensitization effect. 
 
As shown in the picture below:

![3](https://shardingsphere.apache.org/document/current/img/mask/3_en.png)

