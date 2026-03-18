--
-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SET character_set_database='utf8';
SET character_set_server='utf8';

DROP DATABASE IF EXISTS mask_encrypt_ds_0;
DROP DATABASE IF EXISTS mask_encrypt_ds_1;
DROP DATABASE IF EXISTS mask_encrypt_ds_2;
DROP DATABASE IF EXISTS mask_encrypt_ds_3;
DROP DATABASE IF EXISTS mask_encrypt_ds_4;
DROP DATABASE IF EXISTS mask_encrypt_ds_5;
DROP DATABASE IF EXISTS mask_encrypt_ds_6;
DROP DATABASE IF EXISTS mask_encrypt_ds_7;
DROP DATABASE IF EXISTS mask_encrypt_ds_8;
DROP DATABASE IF EXISTS mask_encrypt_ds_9;

CREATE DATABASE mask_encrypt_ds_0;
CREATE DATABASE mask_encrypt_ds_1;
CREATE DATABASE mask_encrypt_ds_2;
CREATE DATABASE mask_encrypt_ds_3;
CREATE DATABASE mask_encrypt_ds_4;
CREATE DATABASE mask_encrypt_ds_5;
CREATE DATABASE mask_encrypt_ds_6;
CREATE DATABASE mask_encrypt_ds_7;
CREATE DATABASE mask_encrypt_ds_8;
CREATE DATABASE mask_encrypt_ds_9;

CREATE TABLE mask_encrypt_ds_0.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_1.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_2.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_3.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_4.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_5.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_6.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_7.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_8.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE mask_encrypt_ds_9.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, creation_date DATE NOT NULL);
