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

--  TODO `shardingsphere-parser-sql-hive` module does not support `CREATE`, `SET`, `TRUNCATE` statements yet,
--   we always need to execute the following Hive Session-level SQL in the current `javax.sql.DataSource`.

-- Fixes `Unsupported Hive type VARCHAR, use string instead or enable automatic type conversion, set 'iceberg.mr.schema.auto.conversion' to true`
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

CREATE TABLE IF NOT EXISTS t_order_item
(
    order_item_id BIGINT,
    order_id      BIGINT NOT NULL,
    user_id       INT    NOT NULL,
    phone         VARCHAR(50),
    status        VARCHAR(50),
    PRIMARY KEY (order_item_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

CREATE TABLE IF NOT EXISTS t_address
(
    address_id   BIGINT       NOT NULL,
    address_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (address_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

TRUNCATE TABLE t_order;
TRUNCATE TABLE t_order_item;
TRUNCATE TABLE t_address;
