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

DROP DATABASE IF EXISTS expected_dataset;
CREATE DATABASE expected_dataset;

GRANT ALL PRIVILEGES ON DATABASE expected_dataset TO test_user;

\c expected_dataset;

DROP TABLE IF EXISTS t_data_type_integer;
CREATE TABLE t_data_type_integer (id INT PRIMARY KEY, col_bigint BIGINT NOT NULL, col_int INT NOT NULL, col_mediumint INT4 NOT NULL, col_smallint SMALLINT NOT NULL, col_tinyint INT2 NOT NULL);
CREATE TABLE t_data_type_integer_unsigned (id INT PRIMARY KEY, col_bigint_unsigned DECIMAL NOT NULL, col_int_unsigned DECIMAL NOT NULL, col_mediumint_unsigned DECIMAL NOT NULL, col_smallint_unsigned DECIMAL NOT NULL, col_tinyint_unsigned DECIMAL NOT NULL);
CREATE TABLE t_data_type_floating_point (id INT PRIMARY KEY, col_float REAL NOT NULL, col_double DOUBLE PRECISION NOT NULL);
CREATE TABLE t_with_generated_id (id SERIAL PRIMARY KEY, val VARCHAR NOT NULL);
CREATE TABLE t_data_type_money (id INT PRIMARY KEY, val money);
CREATE TABLE t_data_type_bytea (id INT PRIMARY KEY, val bytea NOT NULL);
CREATE TABLE t_data_type_date (id INT PRIMARY KEY, creation_date DATE NOT NULL, update_date TIMESTAMP NOT NULL);
CREATE TABLE t_data_type_uuid (id INT PRIMARY KEY, val UUID NOT NULL);
