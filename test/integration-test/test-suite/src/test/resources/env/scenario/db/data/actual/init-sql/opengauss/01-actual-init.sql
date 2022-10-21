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

GRANT ALL PRIVILEGES ON DATABASE db_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_9 TO test_user;

\c db_0;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_single_table;
DROP TABLE IF EXISTS t_broadcast_table;
DROP TABLE IF EXISTS t_order_federate;
DROP TABLE IF EXISTS t_order_federate_sharding;
DROP TABLE IF EXISTS t_order_item_federate_sharding;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product (product_id INT PRIMARY KEY, product_name VARCHAR(50) NOT NULL, category_id INT NOT NULL, price DECIMAL NOT NULL, status VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE t_order_federate (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_1;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_merchant;
DROP TABLE IF EXISTS t_product_detail;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;
DROP TABLE IF EXISTS t_order_item_federate;
DROP TABLE IF EXISTS t_order_federate_sharding;
DROP TABLE IF EXISTS t_order_item_federate_sharding;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id INT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_detail (detail_id INT PRIMARY KEY, product_id INT NOT NULL, description VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE t_order_item_federate (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_2;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_3;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_4;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_5;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_6;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_7;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_8;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);

\c db_9;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_product_category;
DROP TABLE IF EXISTS t_country;
DROP TABLE IF EXISTS t_broadcast_table;

CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item (item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_product_category( category_id INT PRIMARY KEY, category_name VARCHAR(50) NOT NULL, parent_id INT NOT NULL, level INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_country (country_id INT PRIMARY KEY, country_name VARCHAR(50), continent_name VARCHAR(50), creation_date DATE NOT NULL);
CREATE TABLE t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON t_order (order_id);
