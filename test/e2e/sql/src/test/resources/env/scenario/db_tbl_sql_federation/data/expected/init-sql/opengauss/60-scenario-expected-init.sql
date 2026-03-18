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

DROP DATABASE IF EXISTS sql_federation;
CREATE DATABASE sql_federation DBCOMPATIBILITY 'B';

GRANT ALL PRIVILEGES ON DATABASE sql_federation TO test_user;

\c sql_federation;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_merchant;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_product_detail;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
-- TODO open this comment when refresh metadata support view without push down execute
-- DROP VIEW IF EXISTS t_order_item_join_view;
-- DROP VIEW IF EXISTS t_order_subquery_view;
-- DROP VIEW IF EXISTS t_order_aggregation_view;
-- DROP VIEW IF EXISTS t_order_union_view;

CREATE TABLE t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id INT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
-- CREATE VIEW t_order_item_join_view AS SELECT o.order_id, o.user_id, i.item_id FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id ORDER BY o.order_id, i.item_id;
-- CREATE VIEW t_order_subquery_view AS SELECT * FROM t_order o WHERE o.order_id IN (SELECT i.order_id FROM t_order_item i INNER JOIN t_product p ON i.product_id = p.product_id WHERE p.product_id = 10);
-- CREATE VIEW t_order_aggregation_view AS SELECT MAX(p.price) AS max_price, MIN(p.price) AS min_price, SUM(p.price) AS sum_price, AVG(p.price) AS avg_price, COUNT(1) AS count FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id INNER JOIN t_product p ON i.product_id = p.product_id GROUP BY o.order_id HAVING SUM(p.price) > 10000 ORDER BY max_price;
-- CREATE VIEW t_order_union_view AS SELECT * FROM t_order WHERE order_id > 2000 UNION SELECT * FROM t_order WHERE order_id > 1500;

CREATE INDEX order_index_t_order ON t_order (order_id);
