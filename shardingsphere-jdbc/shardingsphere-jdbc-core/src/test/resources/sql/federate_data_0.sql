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

DELETE FROM t_order_federate;
DELETE FROM t_user_info;
DELETE FROM t_order_federate_sharding_0;
DELETE FROM t_order_federate_sharding_1;
INSERT INTO t_order_federate VALUES(1000, 10, 'init');
INSERT INTO t_order_federate VALUES(1001, 11, 'init');

INSERT INTO t_user_info VALUES(0, 'description0');
INSERT INTO t_user_info VALUES(1, 'description1');
INSERT INTO t_user_info VALUES(2, 'description2');
INSERT INTO t_user_info VALUES(3, 'description3');
INSERT INTO t_order_federate_sharding_0 VALUES(1010, 10, 'init');
INSERT INTO t_order_federate_sharding_1 VALUES(1011, 11, 'init');
INSERT INTO t_order_federate_sharding_0 VALUES(1100, 10, 'init');
INSERT INTO t_order_federate_sharding_1 VALUES(1101, 11, 'init');
