#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
mysql -uroot -h127.0.0.1 -p123456 -e 'DROP DATABASE IF EXISTS demo_ds_0;DROP DATABASE IF EXISTS demo_ds_1;DROP DATABASE IF EXISTS demo_ds_2;CREATE DATABASE demo_ds_0;CREATE DATABASE demo_ds_1;CREATE DATABASE demo_ds_2;'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_0;CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_0;CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_0;CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_1;CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_2;CREATE TABLE IF NOT EXISTS t_order(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_1;CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_2;CREATE TABLE IF NOT EXISTS t_order_item(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_1;CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));'
mysql -uroot -h127.0.0.1 -p123456 -e 'USE demo_ds_2;CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id));'
