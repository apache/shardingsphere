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

DROP DATABASE IF EXISTS rdl_ds_0;
DROP DATABASE IF EXISTS rdl_ds_1;
DROP DATABASE IF EXISTS rdl_ds_2;

CREATE DATABASE rdl_ds_0;
CREATE DATABASE rdl_ds_1;
CREATE DATABASE rdl_ds_2;

DROP TABLE IF EXISTS rdl_ds_0.t_user;

DROP TABLE IF EXISTS rdl_ds_2.t_product_category;
DROP TABLE IF EXISTS rdl_ds_2.t_country;

DROP TABLE IF EXISTS rdl_ds_2.t_order_0;
DROP TABLE IF EXISTS rdl_ds_2.t_order_1;
DROP TABLE IF EXISTS rdl_ds_2.t_order_2;
DROP TABLE IF EXISTS rdl_ds_2.t_order_3;

DROP TABLE IF EXISTS rdl_ds_2.t_order_item_0;
DROP TABLE IF EXISTS rdl_ds_2.t_order_item_1;
DROP TABLE IF EXISTS rdl_ds_2.t_order_item_2;
DROP TABLE IF EXISTS rdl_ds_2.t_order_item_3;

CREATE TABLE rdl_ds_0.t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);

CREATE TABLE rdl_ds_2.t_product_category ( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);

CREATE TABLE rdl_ds_2.t_order_0 (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_1 (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_2 (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_3 (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);

CREATE TABLE rdl_ds_2.t_order_item_0 (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_item_1 (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_item_2 (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE rdl_ds_2.t_order_item_3 (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
