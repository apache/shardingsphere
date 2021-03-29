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

DELETE FROM t_order_0;
DELETE FROM t_order_1;
DELETE FROM t_user;

INSERT INTO t_order_0 VALUES(10000, 10, 'init');
INSERT INTO t_order_1 VALUES(10001, 10, 'init');
INSERT INTO t_order_0 VALUES(10002, 10, 'init');

INSERT INTO t_order_item_0 VALUES(1000, 10000, 10, 'init', 't_order_item_calcite_sharding');
INSERT INTO t_order_item_1 VALUES(1010, 10000, 10, 'init', 't_order_item_calcite_sharding');

INSERT INTO t_user VALUES(0, 'plain passwordA', 'Rachel');
INSERT INTO t_user VALUES(2, 'plain passwordC', 'Phoebe');
INSERT INTO t_user VALUES(4, 'plain passwordE', 'Chandler');
INSERT INTO t_user VALUES(10, 'plain passwordE', 'Chandler');
