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

CREATE DATABASE encrypt;

GRANT ALL PRIVILEGES ON DATABASE encrypt TO test_user;

\c encrypt

DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_user_item;
DROP TABLE IF EXISTS t_single_table;
DROP TABLE IF EXISTS t_merchant;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, user_name_like VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, telephone_like CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_user_item (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code_cipher VARCHAR(50) NOT NULL, business_code_like VARCHAR(50) NOT NULL, telephone_cipher CHAR(50) NOT NULL, telephone_like CHAR(11) NOT NULL, creation_date DATE NOT NULL);

CREATE INDEX user_index_t_user ON t_user (user_id);
