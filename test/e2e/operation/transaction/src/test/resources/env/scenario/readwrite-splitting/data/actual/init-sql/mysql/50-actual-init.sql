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

DROP DATABASE IF EXISTS write_ds;
DROP DATABASE IF EXISTS read_ds_0;
DROP DATABASE IF EXISTS read_ds_1;
DROP DATABASE IF EXISTS read_ds_error;

CREATE DATABASE write_ds;
CREATE DATABASE read_ds_0;
CREATE DATABASE read_ds_1;
CREATE DATABASE read_ds_error;

CREATE TABLE write_ds.`t_order` (`order_id` INT PRIMARY KEY, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL);
CREATE TABLE write_ds.`t_order_item` (`item_id` INT PRIMARY KEY, `order_id` int NOT NULL, `user_id` int NOT NULL, `status` varchar(50) DEFAULT NULL);
CREATE TABLE write_ds.`account`(`id` INT PRIMARY KEY, `balance` FLOAT, `transaction_id` INT);
CREATE TABLE write_ds.`t_address` (`id` INT PRIMARY KEY, `code` VARCHAR(36) DEFAULT NULL, `address` VARCHAR(36) DEFAULT NULL);

CREATE TABLE read_ds_0.`t_order` (`order_id` INT PRIMARY KEY, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL);
CREATE TABLE read_ds_0.`t_order_item` (`item_id` INT PRIMARY KEY, `order_id` int NOT NULL, `user_id` int NOT NULL, `status` varchar(50) DEFAULT NULL);
CREATE TABLE read_ds_0.`account`(`id` INT PRIMARY KEY, `balance` FLOAT, `transaction_id` INT);
CREATE TABLE read_ds_0.`t_address` (`id` INT PRIMARY KEY, `code` VARCHAR(36) DEFAULT NULL, `address` VARCHAR(36) DEFAULT NULL);

CREATE TABLE read_ds_1.`t_order` (`order_id` INT PRIMARY KEY, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL);
CREATE TABLE read_ds_1.`t_order_item` (`item_id` INT PRIMARY KEY, `order_id` int NOT NULL, `user_id` int NOT NULL, `status` varchar(50) DEFAULT NULL);
CREATE TABLE read_ds_1.`account`(`id` INT PRIMARY KEY, `balance` FLOAT, `transaction_id` INT);
CREATE TABLE read_ds_1.`t_address` (`id` INT PRIMARY KEY, `code` VARCHAR(36) DEFAULT NULL, `address` VARCHAR(36) DEFAULT NULL);
