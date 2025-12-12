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

DELETE FROM t_order;
DELETE FROM t_order_item;
DELETE FROM t_order_auto;
DELETE FROM t_order_item_auto;
DELETE FROM t_config;

INSERT INTO t_order VALUES(1000, 10, 'init');
INSERT INTO t_order VALUES(1001, 10, 'init');
INSERT INTO t_order VALUES(1100, 11, 'init');
INSERT INTO t_order VALUES(1101, 11, 'init');
INSERT INTO t_order_item VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item VALUES(100001, 1000, 10, 'init');
INSERT INTO t_order_item VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item VALUES(100101, 1001, 10, 'init');
INSERT INTO t_order_item VALUES(110000, 1100, 11, 'init');
INSERT INTO t_order_item VALUES(110001, 1100, 11, 'init');
INSERT INTO t_order_item VALUES(110100, 1101, 11, 'init');
INSERT INTO t_order_item VALUES(110101, 1101, 11, 'init');

INSERT INTO t_order_auto VALUES(1000, 10, 'init');
INSERT INTO t_order_auto VALUES(1100, 11, 'init');
INSERT INTO t_order_item_auto VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item_auto VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item_auto VALUES(110000, 1100, 11, 'init');
INSERT INTO t_order_item_auto VALUES(110100, 1101, 11, 'init');

INSERT INTO t_config VALUES(1, 'init');
