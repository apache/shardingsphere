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

CREATE SCHEMA IF NOT EXISTS public;
SET SCHEMA public;
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY,
    status VARCHAR(32),
    amount INT
);
CREATE TABLE IF NOT EXISTS order_items (
    item_id INT PRIMARY KEY,
    order_id INT,
    sku VARCHAR(64)
);
INSERT INTO orders (order_id, status, amount)
SELECT 1, 'NEW', 10
WHERE NOT EXISTS (
    SELECT 1 FROM orders WHERE order_id = 1
);
INSERT INTO orders (order_id, status, amount)
SELECT 2, 'DONE', 20
WHERE NOT EXISTS (
    SELECT 1 FROM orders WHERE order_id = 2
);
INSERT INTO order_items (item_id, order_id, sku)
SELECT 1, 1, 'sku-1'
WHERE NOT EXISTS (
    SELECT 1 FROM order_items WHERE item_id = 1
);
CREATE VIEW IF NOT EXISTS active_orders AS
SELECT order_id, status FROM orders WHERE status <> 'DONE';
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
