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

DROP DATABASE IF EXISTS expected_dataset;
CREATE DATABASE expected_dataset;

GRANT ALL PRIVILEGES ON DATABASE expected_dataset TO test_user;

\c expected_dataset;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_merchant;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_product_detail;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;

CREATE TABLE t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level SMALLINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);

CREATE INDEX order_index_t_order ON t_order (order_id);
