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

DROP DATABASE IF EXISTS rdl_ds_0;
DROP DATABASE IF EXISTS rdl_ds_1;
DROP DATABASE IF EXISTS rdl_ds_2;

CREATE DATABASE rdl_ds_0;
CREATE DATABASE rdl_ds_1;
CREATE DATABASE rdl_ds_2;

DROP TABLE IF EXISTS rdl_ds_0.t_user_0;
DROP TABLE IF EXISTS rdl_ds_0.t_user_1;
DROP TABLE IF EXISTS rdl_ds_0.t_user_2;
DROP TABLE IF EXISTS rdl_ds_0.t_user_3;
DROP TABLE IF EXISTS rdl_ds_0.t_user_4;

DROP TABLE IF EXISTS rdl_ds_0.t_user_item_0;
DROP TABLE IF EXISTS rdl_ds_0.t_user_item_1;
DROP TABLE IF EXISTS rdl_ds_0.t_user_item_2;
DROP TABLE IF EXISTS rdl_ds_0.t_user_item_3;
DROP TABLE IF EXISTS rdl_ds_0.t_user_item_4;

DROP TABLE IF EXISTS rdl_ds_2.t_product_item_0;
DROP TABLE IF EXISTS rdl_ds_2.t_product_item_1;

DROP TABLE IF EXISTS rdl_ds_2.t_product_0;
DROP TABLE IF EXISTS rdl_ds_2.t_product_1;

CREATE TABLE rdl_ds_0.t_user_0 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
CREATE TABLE rdl_ds_0.t_user_1 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
CREATE TABLE rdl_ds_0.t_user_2 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
CREATE TABLE rdl_ds_0.t_user_3 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
CREATE TABLE rdl_ds_0.t_user_4 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));

CREATE TABLE rdl_ds_0.t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE rdl_ds_0.t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE rdl_ds_0.t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE rdl_ds_0.t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE rdl_ds_0.t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));

CREATE TABLE rdl_ds_2.t_product_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE rdl_ds_2.t_product_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (item_id));

CREATE TABLE rdl_ds_2.t_product_0 (product_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (product_id));
CREATE TABLE rdl_ds_2.t_product_1 (product_id INT NOT NULL, user_id INT NOT NULL, count INT NOT NULL, PRIMARY KEY (product_id));