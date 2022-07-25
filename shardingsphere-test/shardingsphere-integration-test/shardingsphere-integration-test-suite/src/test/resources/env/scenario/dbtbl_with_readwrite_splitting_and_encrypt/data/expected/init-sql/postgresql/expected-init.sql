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

DROP DATABASE IF EXISTS write_dataset;
CREATE DATABASE write_dataset;

GRANT ALL PRIVILEGES ON DATABASE write_dataset TO test_user;

\c write_dataset;

DROP TABLE IF EXISTS t_single_table;
DROP TABLE IF EXISTS t_order_federate;
DROP TABLE IF EXISTS t_order_item_federate;
DROP TABLE IF EXISTS t_order_federate_sharding;
DROP TABLE IF EXISTS t_order_item_federate_sharding;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_user_item;
DROP TABLE IF EXISTS t_user_encrypt_federate;
DROP TABLE IF EXISTS t_user_encrypt_federate_sharding;
DROP TABLE IF EXISTS t_user_info;

CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE t_order_federate (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE t_order_item_federate (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE t_user (user_id INT NOT NULL, address_id INT NOT NULL, pwd VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_encrypt_federate (user_id INT NOT NULL, pwd VARCHAR(45) NULL, username VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_encrypt_federate_sharding (user_id INT NOT NULL, pwd VARCHAR(45) NULL, username VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_info (user_id INT NOT NULL, information VARCHAR(45) NULL, PRIMARY KEY (user_id));

CREATE INDEX user_index_t_user ON t_user (user_id);


DROP DATABASE IF EXISTS read_dataset;
CREATE DATABASE read_dataset;

GRANT ALL PRIVILEGES ON DATABASE read_dataset TO test_user;

\c read_dataset;

DROP TABLE IF EXISTS t_single_table;
DROP TABLE IF EXISTS t_order_federate;
DROP TABLE IF EXISTS t_order_item_federate;
DROP TABLE IF EXISTS t_order_federate_sharding;
DROP TABLE IF EXISTS t_order_item_federate_sharding;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_user_item;
DROP TABLE IF EXISTS t_user_encrypt_federate;
DROP TABLE IF EXISTS t_user_encrypt_federate_sharding;
DROP TABLE IF EXISTS t_user_info;

CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE t_order_federate (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE t_order_item_federate (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE t_order_federate_sharding (order_id_sharding INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id_sharding));
CREATE TABLE t_order_item_federate_sharding (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, remarks VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE t_user (user_id INT NOT NULL, address_id INT NOT NULL, pwd VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_encrypt_federate (user_id INT NOT NULL, pwd VARCHAR(45) NULL, username VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_encrypt_federate_sharding (user_id INT NOT NULL, pwd VARCHAR(45) NULL, username VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_info (user_id INT NOT NULL, information VARCHAR(45) NULL, PRIMARY KEY (user_id));

CREATE INDEX user_index_t_user ON t_user (user_id);
