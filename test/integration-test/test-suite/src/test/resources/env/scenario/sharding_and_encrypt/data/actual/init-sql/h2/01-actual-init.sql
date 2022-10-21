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

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;
DROP TABLE IF EXISTS t_user_4;
DROP TABLE IF EXISTS t_user_item_4;
DROP TABLE IF EXISTS t_user_5;
DROP TABLE IF EXISTS t_user_item_5;
DROP TABLE IF EXISTS t_user_6;
DROP TABLE IF EXISTS t_user_item_6;
DROP TABLE IF EXISTS t_user_7;
DROP TABLE IF EXISTS t_user_item_7;
DROP TABLE IF EXISTS t_user_8;
DROP TABLE IF EXISTS t_user_item_8;
DROP TABLE IF EXISTS t_user_9;
DROP TABLE IF EXISTS t_user_item_9;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_4 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_5 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_5 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_6 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_6 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_7 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_7 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_8 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_8 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_9 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_9 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));

CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);
CREATE INDEX user_index_t_user_4 ON t_user_4 (user_id);
CREATE INDEX user_index_t_user_5 ON t_user_5 (user_id);
CREATE INDEX user_index_t_user_6 ON t_user_6 (user_id);
CREATE INDEX user_index_t_user_7 ON t_user_7 (user_id);
CREATE INDEX user_index_t_user_8 ON t_user_8 (user_id);
CREATE INDEX user_index_t_user_9 ON t_user_9 (user_id);
