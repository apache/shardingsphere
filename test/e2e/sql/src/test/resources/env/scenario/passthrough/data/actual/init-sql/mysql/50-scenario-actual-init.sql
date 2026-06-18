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

DROP DATABASE IF EXISTS passthrough;

CREATE DATABASE passthrough;
CREATE TABLE passthrough.t_data_type_integer (id INT PRIMARY KEY, col_bigint BIGINT NOT NULL, col_int INT NOT NULL, col_mediumint MEDIUMINT NOT NULL, col_smallint SMALLINT NOT NULL, col_tinyint TINYINT NOT NULL);
CREATE TABLE passthrough.t_data_type_integer_unsigned (id INT PRIMARY KEY, col_bigint_unsigned BIGINT UNSIGNED NOT NULL, col_int_unsigned INT UNSIGNED NOT NULL, col_mediumint_unsigned MEDIUMINT UNSIGNED NOT NULL, col_smallint_unsigned SMALLINT UNSIGNED NOT NULL, col_tinyint_unsigned TINYINT UNSIGNED NOT NULL);
CREATE TABLE passthrough.t_data_type_floating_point (id INT PRIMARY KEY, col_float REAL NOT NULL, col_double DOUBLE PRECISION NOT NULL);
CREATE TABLE passthrough.t_with_generated_id (id INT AUTO_INCREMENT PRIMARY KEY, val VARCHAR(100) NOT NULL);
CREATE TABLE passthrough.t_data_type_money (id INT PRIMARY KEY, val NUMERIC(16, 2));
CREATE TABLE passthrough.t_data_type_bytea (id INT PRIMARY KEY, val BLOB NOT NULL);
CREATE TABLE passthrough.t_data_type_date (id INT PRIMARY KEY, creation_date DATE NOT NULL, update_date DATETIME NOT NULL);
CREATE TABLE passthrough.t_data_type_uuid (id INT PRIMARY KEY, val VARCHAR(36) NOT NULL);
CREATE TABLE passthrough.t_data_type_binary (id INT PRIMARY KEY, val BINARY(10) NOT NULL);
CREATE TABLE passthrough.t_data_type_varbinary (id INT PRIMARY KEY, val VARBINARY(10) NOT NULL);
CREATE TABLE passthrough.t_data_type_longblob (id INT PRIMARY KEY, val LONGBLOB NOT NULL);
