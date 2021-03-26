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

DELETE FROM t_order_item_0;
DELETE FROM t_order_item_1;
DELETE FROM t_user;
DELETE FROM t_order_0;
DELETE FROM t_order_1;

INSERT INTO t_order_0 VALUES(10001, 11, 'init');
INSERT INTO t_order_item_0 VALUES(1001, 10002, 11, 'init', 't_order_item_calcite_sharding');
INSERT INTO t_order_item_1 VALUES(1011, 10003, 11, 'init', 't_order_item_calcite_sharding');

INSERT INTO t_user VALUES(1, 'plain passwordB', 'Monica');
INSERT INTO t_user VALUES(3, 'plain passwordD', 'Ross');
INSERT INTO t_user VALUES(5, 'plain passwordF', 'Joey');
