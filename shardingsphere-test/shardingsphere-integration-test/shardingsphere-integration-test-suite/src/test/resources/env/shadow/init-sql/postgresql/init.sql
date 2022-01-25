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

GRANT ALL PRIVILEGES ON DATABASE db TO root;
GRANT ALL PRIVILEGES ON DATABASE shadow_db TO root;

\c db

DROP TABLE IF EXISTS t_order;

CREATE TABLE t_order (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name VARCHAR(32) NOT NULL, type_decimal DECIMAL(18,2) NOT NULL, type_char char(1) NOT NULL, type_date DATE NOT NULL,
                      type_time time(0) NOT NULL, type_timestamp timestamp(0) without time zone, PRIMARY KEY (order_id));

\c shadow_db

DROP TABLE IF EXISTS t_order;

CREATE TABLE t_order (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name VARCHAR(32) NOT NULL, type_decimal DECIMAL(18,2) NOT NULL, type_char char(1) NOT NULL, type_date DATE NOT NULL,
                      type_time time(0) NOT NULL, type_timestamp timestamp(0) without time zone, PRIMARY KEY (order_id));
