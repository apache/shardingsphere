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

-- ClickHouse does not support `AUTO_INCREMENT`, refer to https://github.com/ClickHouse/ClickHouse/issues/56228 .
-- TODO The `shardingsphere-parser-sql-engine-clickhouse` module needs to be fixed to use SQL like `create table`, `truncate table` and `drop table`.
create table IF NOT EXISTS t_order (
    order_id   Int64 NOT NULL,
    order_type Int32,
    user_id    Int32 NOT NULL,
    address_id Int64 NOT NULL,
    status     VARCHAR(50)
) engine = MergeTree
    primary key (order_id)
    order by (order_id);
create table IF NOT EXISTS t_order_item (
    order_item_id Int64 NOT NULL,
    order_id      Int64 NOT NULL,
    user_id       Int32 NOT NULL,
    phone         VARCHAR(50),
    status        VARCHAR(50)
) engine = MergeTree
    primary key (order_item_id)
    order by (order_item_id);
CREATE TABLE IF NOT EXISTS t_address (
    address_id   BIGINT NOT NULL,
    address_name VARCHAR(100) NOT NULL,
    PRIMARY      KEY (address_id)
);
TRUNCATE TABLE t_order;
TRUNCATE TABLE t_order_item;
TRUNCATE TABLE t_address;
