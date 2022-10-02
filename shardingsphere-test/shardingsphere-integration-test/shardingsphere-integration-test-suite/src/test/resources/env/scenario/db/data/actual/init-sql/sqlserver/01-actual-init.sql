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

CREATE TABLE db_0.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_0.t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE db_0.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE db_0.t_order_federate (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE db_0.t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE db_0.t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE INDEX order_index_t_order ON db_0.t_order (order_id);

CREATE TABLE db_1.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_1.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE db_1.t_order_item_federate (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE db_1.t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE db_1.t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE INDEX order_index_t_order ON db_1.t_order (order_id);

CREATE TABLE db_2.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_2.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_2.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_2.t_order (order_id);

CREATE TABLE db_3.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_3.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_3.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_3.t_order (order_id);

CREATE TABLE db_4.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_4.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_4.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_4.t_order (order_id);

CREATE TABLE db_5.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_5.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_5.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_5.t_order (order_id);

CREATE TABLE db_6.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_6.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_6.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_6.t_order (order_id);

CREATE TABLE db_7.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_7.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_7.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_7.t_order (order_id);

CREATE TABLE db_8.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_8.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_8.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_8.t_order (order_id);

CREATE TABLE db_9.t_order(order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(50) NOT NULL, merchant_id INT NOT NULL, remark VARCHAR(50) NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_9.t_order_item(item_id INT PRIMARY KEY, order_id INT NOT NULL, user_id INT NOT NULL, product_id INT NOT NULL, quantity INT NOT NULL, creation_date DATE NOT NULL);
CREATE TABLE db_9.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE INDEX order_index_t_order ON db_9.t_order (order_id);
