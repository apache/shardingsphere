/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

DROP DATABASE IF EXISTS demo_ds;
DROP DATABASE IF EXISTS demo_ds_0;
DROP DATABASE IF EXISTS demo_ds_1;
DROP DATABASE IF EXISTS demo_ds_2;

DROP DATABASE IF EXISTS demo_write_ds;
DROP DATABASE IF EXISTS demo_read_ds_0;
DROP DATABASE IF EXISTS demo_read_ds_1;

DROP DATABASE IF EXISTS demo_write_ds_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_1;
DROP DATABASE IF EXISTS demo_write_ds_1;
DROP DATABASE IF EXISTS demo_write_ds_1_read_0;
DROP DATABASE IF EXISTS demo_write_ds_1_read_1;

DROP DATABASE IF EXISTS shadow_demo_ds;
DROP DATABASE IF EXISTS shadow_demo_ds_0;
DROP DATABASE IF EXISTS shadow_demo_ds_1;

DROP DATABASE IF EXISTS demo_shadow_write_ds;
DROP DATABASE IF EXISTS demo_shadow_read_ds;
DROP DATABASE IF EXISTS demo_read_ds;

CREATE DATABASE demo_ds;
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;

CREATE DATABASE demo_write_ds;
CREATE DATABASE demo_read_ds_0;
CREATE DATABASE demo_read_ds_1;

CREATE DATABASE demo_write_ds_0;
CREATE DATABASE demo_write_ds_0_read_0;
CREATE DATABASE demo_write_ds_0_read_1;
CREATE DATABASE demo_write_ds_1;
CREATE DATABASE demo_write_ds_1_read_0;
CREATE DATABASE demo_write_ds_1_read_1;

CREATE DATABASE shadow_demo_ds;
CREATE DATABASE shadow_demo_ds_0;
CREATE DATABASE shadow_demo_ds_1;

CREATE DATABASE demo_shadow_write_ds;
CREATE DATABASE demo_shadow_read_ds;
CREATE DATABASE demo_read_ds;

-- Execute SQL Script through logical-database connection , eg: sharding_db. Docker samples configuration in the 'conf/database-sharding.yaml' file
-- CREATE TABLE t_order ("order_id" serial4, "user_id" int4 NOT NULL, PRIMARY KEY ("order_id"));
-- CREATE TABLE t_order_item ("order_item_id" serial4, "order_id" int4 NOT NULL, "user_id" int4 NOT NULL, "status" varchar(50), PRIMARY KEY ("order_item_id"));

USE demo_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_ds_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_ds_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_ds_2;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_read_ds_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_read_ds_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_0_read_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_0_read_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_1_read_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_write_ds_1_read_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE shadow_demo_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE shadow_demo_ds_0;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE shadow_demo_ds_1;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_shadow_write_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_shadow_read_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));

USE demo_read_ds;
CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)<#if feature?contains("encrypt")>, status_assisted VARCHAR(50)</#if>, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));
CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));
