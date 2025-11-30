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

CREATE DATABASE mask_ds_0;
CREATE DATABASE mask_ds_1;
CREATE DATABASE mask_ds_2;
CREATE DATABASE mask_ds_3;
CREATE DATABASE mask_ds_4;
CREATE DATABASE mask_ds_5;
CREATE DATABASE mask_ds_6;
CREATE DATABASE mask_ds_7;
CREATE DATABASE mask_ds_8;
CREATE DATABASE mask_ds_9;

GRANT ALL PRIVILEGES ON DATABASE mask_ds_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE mask_ds_9 TO test_user;

\c mask_ds_0

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_1

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_2

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_3

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_4

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_5

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_6

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_7

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_8

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c mask_ds_9

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
