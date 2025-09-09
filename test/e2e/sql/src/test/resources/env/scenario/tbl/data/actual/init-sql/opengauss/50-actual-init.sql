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

CREATE DATABASE tbl DBCOMPATIBILITY 'B';

GRANT ALL PRIVILEGES ON DATABASE tbl TO test_user;

\c tbl

DROP TABLE IF EXISTS t_order_0;
DROP TABLE IF EXISTS t_order_item_0;
DROP TABLE IF EXISTS t_order_1;
DROP TABLE IF EXISTS t_order_item_1;
DROP TABLE IF EXISTS t_order_2;
DROP TABLE IF EXISTS t_order_item_2;
DROP TABLE IF EXISTS t_order_3;
DROP TABLE IF EXISTS t_order_item_3;
DROP TABLE IF EXISTS t_order_4;
DROP TABLE IF EXISTS t_order_item_4;
DROP TABLE IF EXISTS t_order_5;
DROP TABLE IF EXISTS t_order_item_5;
DROP TABLE IF EXISTS t_order_6;
DROP TABLE IF EXISTS t_order_item_6;
DROP TABLE IF EXISTS t_order_7;
DROP TABLE IF EXISTS t_order_item_7;
DROP TABLE IF EXISTS t_order_8;
DROP TABLE IF EXISTS t_order_item_8;
DROP TABLE IF EXISTS t_order_9;
DROP TABLE IF EXISTS t_order_item_9;

CREATE TABLE t_order_0(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_0(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_0 ON t_order_0 (order_id);

CREATE TABLE t_order_1(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_1(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_1 ON t_order_1 (order_id);

CREATE TABLE t_order_2(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_2(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_2 ON t_order_2 (order_id);

CREATE TABLE t_order_3(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_3(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_3 ON t_order_3 (order_id);

CREATE TABLE t_order_4(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_4(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_4 ON t_order_4 (order_id);

CREATE TABLE t_order_5(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_5(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_5 ON t_order_5 (order_id);

CREATE TABLE t_order_6(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_6(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_6 ON t_order_6 (order_id);

CREATE TABLE t_order_7(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_7(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_7 ON t_order_7 (order_id);

CREATE TABLE t_order_8(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_8(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_8 ON t_order_8 (order_id);

CREATE TABLE t_order_9(order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item_9(item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order_9 ON t_order_9 (order_id);

CREATE TABLE t_product_0 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_1 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_2 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_3 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_4 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_5 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_6 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_7 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_8 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_9 (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
