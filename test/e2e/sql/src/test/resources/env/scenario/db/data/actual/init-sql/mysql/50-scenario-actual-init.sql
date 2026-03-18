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

DROP DATABASE IF EXISTS db_0;
DROP DATABASE IF EXISTS db_1;
DROP DATABASE IF EXISTS db_2;
DROP DATABASE IF EXISTS db_3;
DROP DATABASE IF EXISTS db_4;
DROP DATABASE IF EXISTS db_5;
DROP DATABASE IF EXISTS db_6;
DROP DATABASE IF EXISTS db_7;
DROP DATABASE IF EXISTS db_8;
DROP DATABASE IF EXISTS db_9;

CREATE DATABASE db_0;
CREATE DATABASE db_1;
CREATE DATABASE db_2;
CREATE DATABASE db_3;
CREATE DATABASE db_4;
CREATE DATABASE db_5;
CREATE DATABASE db_6;
CREATE DATABASE db_7;
CREATE DATABASE db_8;
CREATE DATABASE db_9;

CREATE TABLE db_0.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_product (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE db_0.t_single (ID BIGINT PRIMARY KEY, NAME VARCHAR(50));
CREATE INDEX order_index ON db_0.t_order (order_id);

CREATE TABLE db_1.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_1.t_order (order_id);

CREATE TABLE db_2.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_2.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_2.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_2.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_2.t_order (order_id);

CREATE TABLE db_3.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_3.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_3.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_3.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_3.t_order (order_id);

CREATE TABLE db_4.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_4.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_4.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_4.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_4.t_order (order_id);

CREATE TABLE db_5.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_5.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_5.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_5.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_5.t_order (order_id);

CREATE TABLE db_6.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_6.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_6.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_6.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_6.t_order (order_id);

CREATE TABLE db_7.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_7.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_7.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_7.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_7.t_order (order_id);

CREATE TABLE db_8.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_8.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_8.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_8.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_8.t_order (order_id);

CREATE TABLE db_9.t_order (order_id BIGINT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_9.t_order_item (item_id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_9.t_product_category (category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level TINYINT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_9.t_country (country_id SMALLINT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE INDEX order_index ON db_9.t_order (order_id);
