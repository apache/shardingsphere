/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE IF NOT EXISTS t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_0 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_1 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));

CREATE TABLE IF NOT EXISTS t_order_auto_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_auto_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_auto_0 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_auto_1 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));

CREATE TABLE IF NOT EXISTS t_config (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS t_user_0 (id INT NOT NULL, name VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_user_1 (id INT NOT NULL, name VARCHAR(45) NULL, PRIMARY KEY (id));
