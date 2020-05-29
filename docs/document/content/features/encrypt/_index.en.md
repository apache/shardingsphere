+++
pre = "<b>3.6. </b>"
title = "Encryption"
weight = 6
chapter = true
+++

## Background

Security control has always been a crucial link of orchestration; data masking falls into this category. For both Internet enterprises and traditional sectors, data security has always been a highly valued and sensitive topic. Data masking refers to transforming some sensitive information through masking rules to safely protect the private data. Data involves client's security or business sensibility, such as ID number, phone number, card number, client number and other personal information, requires data masking according to relevant regulations.

Because of that, ShardingSphere has provided data masking, which stores users' sensitive information in the database after encryption. When users search for them, the information will be decrypted and returned to users in the original form. 

ShardingSphere has made the encryption and decryption processes totally transparent to users, who can store desensitized data and acquire original data without any awareness. In addition, ShardingSphere has provided internal masking algorithms, which can be directly used by users. In the same time, we have also provided masking algorithm related interfaces, which can be implemented by users themselves. After simple configurations, ShardingSphere can use algorithms provided by users to perform encryption, decryption and masking.

## Preface

The data encryption module belongs to the sub-function module under the core function of ShardingSphere distributed governance. It parses the SQL input by the user and rewrites the SQL according to the encryption configuration provided by the user, thereby encrypting the original data and storing the original data and store the original data (optional) and cipher data to database at the same time. When the user queries the data, it takes the cipher data from the database and decrypts it, and finally returns the decrypted original data to the user. Apache ShardingSphere distributed database middleware automates and transparentizes the process of data encryption, so that users do not need to pay attention to the details of data decryption and use decrypted data like ordinary data.  In addition, ShardingSphere can provide a relatively complete set of solutions for the encryption of online services or the encryption function of new services.

## Demand Analysis

The demand for data encryption is generally divided into two situations in real business scenarios:

1. When the new business start to launch, and the security department stipulates that the sensitive information related to users, such as banks and mobile phone numbers, should be encrypted and stored in the database, and then decrypted when used. Because it is a brand new system, there is no inventory data cleaning problem, so the implementation is relatively simple.

2. For the service has been launched, and plaintext has been stored in the database before. The relevant department suddenly needs to encrypt the data from the on-line business. This scenario generally needs to deal with three issues as followings:

   a) How to encrypt the historical data, a.k.a.s clean data.

   b) How to encrypt the newly added data and store it in the database without changing the business SQL and logic; then decrypt the taken out data when use it.

   c) How to securely, seamlessly and transparently migrate plaintext and ciphertext data between business systems

