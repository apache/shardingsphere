/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

DELETE FROM t_order_item_calcite;
DELETE FROM t_order_item_calcite_sharding_0;
DELETE FROM t_order_item_calcite_sharding_1;

INSERT INTO t_order_item_calcite VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item_calcite VALUES(100001, 1000, 10, 'init');
INSERT INTO t_order_item_calcite VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item_calcite VALUES(100101, 1001, 10, 'init');

INSERT INTO t_order_item_calcite_sharding_0 VALUES(1000, 10000, 10, 'init', 't_order_item_calcite_sharding');
INSERT INTO t_order_item_calcite_sharding_1 VALUES(1001, 10001, 11, 'init', 't_order_item_calcite_sharding');
INSERT INTO t_order_item_calcite_sharding_0 VALUES(1010, 10001, 10, 'init', 't_order_item_calcite_sharding');
INSERT INTO t_order_item_calcite_sharding_1 VALUES(1011, 10001, 10, 'init', 't_order_item_calcite_sharding');
