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

-- TODO open this comment when refresh metadata support view without push down execute
-- DROP VIEW IF EXISTS t_order_item_join_view;
-- DROP VIEW IF EXISTS t_order_subquery_view;
-- DROP VIEW IF EXISTS t_order_aggregation_view;
-- DROP VIEW IF EXISTS t_order_union_view;

-- CREATE VIEW t_order_item_join_view AS SELECT o.order_id, o.user_id, i.item_id FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id ORDER BY o.order_id, i.item_id;
-- CREATE VIEW t_order_subquery_view AS SELECT * FROM t_order o WHERE o.order_id IN (SELECT i.order_id FROM t_order_item i INNER JOIN t_product p ON i.product_id = p.product_id WHERE p.product_id = 10);
-- CREATE VIEW t_order_aggregation_view AS SELECT MAX(p.price) AS max_price, MIN(p.price) AS min_price, SUM(p.price) AS sum_price, AVG(p.price) AS avg_price, COUNT(1) AS count FROM t_order o INNER JOIN t_order_item i ON o.order_id = i.order_id INNER JOIN t_product p ON i.product_id = p.product_id GROUP BY o.order_id HAVING SUM(p.price) > 10000 ORDER BY max_price;
-- CREATE VIEW t_order_union_view AS SELECT * FROM t_order WHERE order_id > 2000 UNION SELECT * FROM t_order WHERE order_id > 1500;
DELETE FROM t_order WHERE order_id = 99999999;
