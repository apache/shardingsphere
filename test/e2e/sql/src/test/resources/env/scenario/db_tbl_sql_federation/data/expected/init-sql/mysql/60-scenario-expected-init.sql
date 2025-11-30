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
CREATE TABLE sql_federation.t_product_extend(extend_id INT PRIMARY KEY, product_id INT NOT NULL, type_int INT NOT NULL, type_smallint SMALLINT NOT NULL, type_decimal DECIMAL NOT NULL, type_float FLOAT NOT NULL, type_double DOUBLE NOT NULL, type_bit BIT(64) NOT NULL, type_tinyint TINYINT NOT NULL, type_mediumint mediumint NOT NULL, type_bigint BIGINT NOT NULL, type_date DATE NOT NULL, type_time TIME NOT NULL, type_datetime DATETIME NOT NULL, type_timestamp TIMESTAMP NOT NULL, type_year YEAR NOT NULL, type_char CHAR NOT NULL, type_text TEXT NOT NULL, type_varchar VARCHAR(50) NOT NULL, type_longtext LONGTEXT NOT NULL, type_longblob LONGBLOB NOT NULL, type_mediumtext mediumtext NOT NULL, type_mediumblob mediumblob NOT NULL, type_binary BINARY(255) NOT NULL, type_varbinary VARBINARY(1024) NOT NULL, type_blob BLOB NOT NULL, type_enum ENUM('spring', 'summer', 'autumn', 'winter') NOT NULL, type_set SET('spring', 'summer', 'autumn', 'winter') NOT NULL, type_json JSON NOT NULL, type_unsigned_int INT UNSIGNED NOT NULL, type_unsigned_bigint BIGINT UNSIGNED NOT NULL, type_unsigned_tinyint TINYINT UNSIGNED NOT NULL, type_unsigned_smallint SMALLINT UNSIGNED NOT NULL, type_unsigned_float FLOAT UNSIGNED NOT NULL, type_unsigned_double DOUBLE UNSIGNED NOT NULL, type_unsigned_decimal DECIMAL UNSIGNED NOT NULL);
CREATE TABLE sql_federation.t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE sql_federation.t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
-- TODO open this comment when refresh metadata support view without push down execute
-- CREATE VIEW sql_federation.t_order_item_join_view AS SELECT o.order_id, o.user_id, i.item_id FROM sql_federation.t_order o INNER JOIN sql_federation.t_order_item i ON o.order_id = i.order_id ORDER BY o.order_id, i.item_id;
-- CREATE VIEW sql_federation.t_order_subquery_view AS SELECT * FROM sql_federation.t_order o WHERE o.order_id IN (SELECT i.order_id FROM sql_federation.t_order_item i INNER JOIN sql_federation.t_product p ON i.product_id = p.product_id WHERE p.product_id = 10);
-- CREATE VIEW sql_federation.t_order_aggregation_view AS SELECT MAX(p.price) AS max_price, MIN(p.price) AS min_price, SUM(p.price) AS sum_price, AVG(p.price) AS avg_price, COUNT(1) AS count FROM sql_federation.t_order o INNER JOIN sql_federation.t_order_item i ON o.order_id = i.order_id INNER JOIN sql_federation.t_product p ON i.product_id = p.product_id GROUP BY o.order_id HAVING SUM(p.price) > 10000 ORDER BY max_price;
-- CREATE VIEW sql_federation.t_order_union_view AS SELECT * FROM sql_federation.t_order WHERE order_id > 2000 UNION SELECT * FROM sql_federation.t_order WHERE order_id > 1500;

CREATE INDEX order_index_t_order ON sql_federation.t_order (order_id);
