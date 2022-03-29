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

CREATE USER 'root'@'%' IDENTIFIED BY '';
GRANT All privileges ON *.* TO 'root'@'%';

DROP DATABASE IF EXISTS write_dataset;
CREATE DATABASE write_dataset;

CREATE TABLE write_dataset.t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE write_dataset.t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE write_dataset.t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE write_dataset.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

CREATE INDEX order_index_t_order ON write_dataset.t_order (order_id);


DROP DATABASE IF EXISTS read_dataset;
CREATE DATABASE read_dataset;

CREATE TABLE read_dataset.t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE read_dataset.t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE read_dataset.t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE TABLE read_dataset.t_broadcast_table (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

CREATE INDEX order_index_t_order ON read_dataset.t_order (order_id);
