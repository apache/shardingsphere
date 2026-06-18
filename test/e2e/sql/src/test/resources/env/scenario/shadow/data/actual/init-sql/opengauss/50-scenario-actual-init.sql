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

CREATE DATABASE db;
CREATE DATABASE shadow_db;

GRANT ALL PRIVILEGES ON DATABASE db TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db TO test_user;

\c db

DROP TABLE IF EXISTS t_shadow;
DROP TABLE IF EXISTS t_merchant;

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

CREATE TABLE t_shadow (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name VARCHAR(32) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);

\c shadow_db

DROP TABLE IF EXISTS t_shadow;
DROP TABLE IF EXISTS t_merchant;

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

CREATE TABLE t_shadow (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name VARCHAR(32) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));
CREATE TABLE t_merchant (merchant_id INT PRIMARY KEY, country_id SMALLINT NOT NULL, merchant_name VARCHAR(50) NOT NULL, business_code VARCHAR(50) NOT NULL, telephone CHAR(11) NOT NULL, creation_date DATE NOT NULL);
