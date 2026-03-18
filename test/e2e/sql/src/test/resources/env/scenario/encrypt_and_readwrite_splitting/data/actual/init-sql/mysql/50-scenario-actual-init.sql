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

DROP DATABASE IF EXISTS encrypt_write_ds;
DROP DATABASE IF EXISTS encrypt_read_ds;

CREATE DATABASE encrypt_write_ds;
CREATE DATABASE encrypt_read_ds;

CREATE TABLE encrypt_write_ds.t_user (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds.t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX user_index_t_user ON encrypt_write_ds.t_user (user_id);

CREATE TABLE encrypt_read_ds.t_user (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds.t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX user_index_t_user ON encrypt_read_ds.t_user (user_id);
