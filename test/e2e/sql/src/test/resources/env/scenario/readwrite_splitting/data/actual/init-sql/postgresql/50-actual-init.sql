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

CREATE DATABASE write_ds;
CREATE DATABASE read_0;
CREATE DATABASE read_1;

GRANT ALL PRIVILEGES ON DATABASE write_ds TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_1 TO test_user;

\c write_ds

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;

CREATE TABLE t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order ON t_order (order_id);

\c read_0

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;

CREATE TABLE t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order ON t_order (order_id);

\c read_1

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_order_item;

CREATE TABLE t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE INDEX order_index_t_order ON t_order (order_id);
