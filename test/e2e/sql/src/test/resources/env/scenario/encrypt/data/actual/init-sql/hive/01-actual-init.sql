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

<<<<<<< HEAD
SET character_set_database='utf8';
SET character_set_server='utf8';

DROP DATABASE IF EXISTS encrypt;
CREATE DATABASE IF NOT EXISTS encrypt;

CREATE TABLE encrypt.t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE encrypt.t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE encrypt.t_user (user_id INT PRIMARY KEY, user_name_cipher VARCHAR(50) NOT NULL, user_name_like VARCHAR(50) NOT NULL, password_cipher VARCHAR(50) NOT NULL, email_cipher VARCHAR(50) NOT NULL, user_telephone_cipher CHAR(50) NOT NULL, user_telephone_like CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE encrypt.t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code_cipher VARCHAR(50) NOT NULL, business_code_like VARCHAR(50) NOT NULL, merchant_telephone_cipher CHAR(50) NOT NULL, merchant_telephone_like CHAR(11) NOT NULL, creation_date DATE NOT NULL);

=======
DROP DATABASE IF EXISTS encrypt CASCADE;
CREATE DATABASE IF NOT EXISTS encrypt;

CREATE TABLE encrypt.t_order (
  order_id INT,
  user_id INT,
  status STRING,
  merchant_id INT,
  remark STRING,
  creation_date DATE
);

CREATE TABLE encrypt.t_order_item (
  item_id INT,
  order_id INT,
  user_id INT,
  product_id INT,
  quantity INT,
  creation_date DATE
);

CREATE TABLE encrypt.t_user (
  user_id INT,
  user_name_cipher STRING,
  user_name_like STRING,
  password_cipher STRING,
  email_cipher STRING,
  user_telephone_cipher STRING,
  user_telephone_like STRING,
  creation_date DATE
);

CREATE TABLE encrypt.t_merchant (
  merchant_id INT,
  country_id SMALLINT,
  merchant_name STRING,
  business_code_cipher STRING,
  business_code_like STRING,
  merchant_telephone_cipher STRING,
  merchant_telephone_like STRING,
  creation_date DATE
);
>>>>>>> 8a0888a0219
