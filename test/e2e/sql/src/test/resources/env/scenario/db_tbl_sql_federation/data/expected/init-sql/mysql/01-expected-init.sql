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

DROP DATABASE IF EXISTS sql_federation;
CREATE DATABASE sql_federation;

CREATE TABLE sql_federation.t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_merchant (merchant_id INT PRIMARY KEY, country_id INT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_product (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE VIEW sql_federation.t_order_item_join_view AS SELECT o.order_id, o.user_id, i.item_id FROM sql_federation.t_order o INNER JOIN sql_federation.t_order_item i ON o.order_id = i.order_id ORDER BY o.order_id, i.item_id;
CREATE VIEW sql_federation.t_order_subquery_view AS SELECT * FROM sql_federation.t_order o WHERE o.order_id IN (SELECT i.order_id FROM sql_federation.t_order_item i INNER JOIN sql_federation.t_product p ON i.product_id = p.product_id WHERE p.product_id = 10);
CREATE VIEW sql_federation.t_order_aggregation_view AS SELECT MAX(p.price) AS max_price, MIN(p.price) AS min_price, SUM(p.price) AS sum_price, AVG(p.price) AS avg_price, COUNT(1) AS count FROM sql_federation.t_order o INNER JOIN sql_federation.t_order_item i ON o.order_id = i.order_id INNER JOIN sql_federation.t_product p ON i.product_id = p.product_id GROUP BY o.order_id HAVING SUM(p.price) > 10000 ORDER BY max_price;
CREATE VIEW sql_federation.t_order_union_view AS SELECT * FROM sql_federation.t_order WHERE order_id > 2000 UNION SELECT * FROM sql_federation.t_order WHERE order_id > 1500;

CREATE INDEX order_index_t_order ON sql_federation.t_order (order_id);
