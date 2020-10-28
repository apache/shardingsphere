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

DROP SCHEMA IF EXISTS `replica_ds_1`;
CREATE SCHEMA `replica_ds_1`;
DROP TABLE IF EXISTS `t_order`;
DROP TABLE IF EXISTS `t_order_item`;
DROP TABLE IF EXISTS `t_config`;
CREATE TABLE `t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE `t_order_item` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE `t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));
